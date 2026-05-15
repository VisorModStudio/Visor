package org.vmstudio.visor.core.client.tasks.types;

import lombok.Getter;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.input.actions.ActionRightMouse;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.EnumMap;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomConsume extends VisorTask {
    private static final String ID = "room_consume";

    @Getter
    private static TaskRoomConsume instance;

    private static final int DURATION = 2100;
    private static final float MOUTH_DISTANCE = 0.25F;
    private static final float HAPTIC_PULSE_DURATION = 0.007f; //in seconds
    private static final int HAPTIC_DELAY_EAT_DRINK = 2;
    private static final int HAPTIC_DELAY_TOOT_HORN = 1;

    private final EnumMap<HandType, Boolean> consuming = new EnumMap<>(HandType.class);
    private final EnumMap<HandType, Long> eatStartMap = new EnumMap<>(HandType.class);

    private boolean eatingPressed;
    public TaskRoomConsume(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        LocalPlayerPose roomPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RELATIVE);
        Vector3fc hmdPos = roomPose.getHmd().getPosition();
        Vector3fc mouthPos = roomPose
                .getHand(HandType.MAIN)
                .getCustomVector(new Vector3f(0, 0, 0))
                .add(hmdPos);

        for (HandType hand : HandType.values()) {
            Vector3fc handPos = calculateHandPosition(roomPose, hand);
            if (mouthPos.distance(handPos) >= MOUTH_DISTANCE) {
                consuming.put(hand, false);
                continue;
            }

            InteractionHand interactHand = (hand == HandType.MAIN)
                    ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack foodItem = (hand == HandType.MAIN)
                    ? player.getMainHandItem() : player.getOffhandItem();

            if (!isConsumable(foodItem)) {
                continue;
            }

            int hapticDelay = switch (foodItem.getUseAnimation()) {
                case EAT, DRINK -> HAPTIC_DELAY_EAT_DRINK;
                case TOOT_HORN -> HAPTIC_DELAY_TOOT_HORN;
                default -> -1;
            };
            if (hapticDelay == -1) continue;

            //start consuming
            if (!consuming.getOrDefault(hand, false)) {
                boolean usedItem = MC.gameMode.useItem(player, interactHand).consumesAction();
                if (usedItem) {
                    MC.gameRenderer.itemInHandRenderer.itemUsed(interactHand);
                    consuming.put(hand, true);
                    eatStartMap.put(hand, Util.getMillis());
                }
            }

            //consume feedback
            if (consuming.getOrDefault(hand, false)) {
                long ticksLeft = player.getUseItemRemainingTicks();
                if (ticksLeft > 0L && ticksLeft % 5L <= hapticDelay) {
                    ClientContext.inputManager.triggerHapticPulse(hand, HAPTIC_PULSE_DURATION);
                }
            }

            // Reset consumption state per hand after the duration has passed
            if (Util.getMillis() - eatStartMap.getOrDefault(hand, 0L) > DURATION) {
                consuming.put(hand, false);
            }
        }

        boolean isEating = consuming.getOrDefault(HandType.MAIN, false)
                || consuming.getOrDefault(HandType.OFFHAND, false);


        if(isEating){
            var actionRightMouse = (ActionRightMouse) ClientContext.inputManager
                    .getActionRightMouse(HandType.MAIN);
            actionRightMouse.forcePress();
            eatingPressed = true;
        }else if(eatingPressed){
            var actionRightMouse = (ActionRightMouse) ClientContext.inputManager
                    .getActionRightMouse(HandType.MAIN);
            actionRightMouse.forceRelease();
            eatingPressed = false;
        }
    }

    @Override
    protected void onClear(LocalPlayer player) {
        consuming.clear();
        eatStartMap.clear();
    }


    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        if(!VRClientSettings.isRoomConsumeEnabled()) return false;

        if (MC.gameMode == null || player == null
                || !player.isAlive() || player.isSleeping() || player.isSpectator()) {
            return false;
        }
        return isConsumable(player.getMainHandItem())
                || isConsumable(player.getOffhandItem());
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    private Vector3fc calculateHandPosition(LocalPlayerPose roomPose,
                                            HandType hand) {
        Vector3fc basePos = ClientContext.rawPoseHandler.getControllerData(hand)
                .getPositionHistory()
                .averagePosition(0.333f);
        Vector3fc customOffset = roomPose.getHand(hand)
                .getCustomVector(new Vector3f(0.0f, 0.0f, -0.1f));
        Vector3fc directionOffset = roomPose.getHand(hand)
                .getDirection()
                .mul(0.1f, new Vector3f());
        return basePos.add(customOffset, new Vector3f()).add(directionOffset);
    }

    private boolean isConsumable(ItemStack item) {
        if (item == ItemStack.EMPTY) {
            return false;
        }
        UseAnim useAnim = item.getUseAnimation();
        return useAnim == UseAnim.EAT
                || useAnim == UseAnim.DRINK
                || useAnim == UseAnim.TOOT_HORN;
    }
}
