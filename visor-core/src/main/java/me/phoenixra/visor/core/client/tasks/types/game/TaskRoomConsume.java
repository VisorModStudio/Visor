package me.phoenixra.visor.core.client.tasks.types.game;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import net.minecraft.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomConsume extends VisorTask {
    private static final String ID = "room_consume";

    @Getter
    private static TaskRoomConsume instance;

    private static final int DURATION = 2100;
    private static final float MOUTH_DISTANCE = 0.25F;
    private static final int HAPTIC_PULSE_STRENGTH = 700;
    private static final int HAPTIC_DELAY_EAT_DRINK = 2;
    private static final int HAPTIC_DELAY_TOOT_HORN = 1;

    private final EnumMap<ControllerHand, Boolean> consuming = new EnumMap<>(ControllerHand.class);
    private final EnumMap<ControllerHand, Long> eatStartMap = new EnumMap<>(ControllerHand.class);

    private boolean eatingPressed;
    public TaskRoomConsume(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(LocalPlayer player) {
        PoseDataImpl roomPose = ClientContext.player
                .getPose(PoseType.ROOM);
        Vec3 hmdPos = roomPose.getHmd().getPosition();
        Vec3 mouthPos = roomPose
                .getController(ControllerHand.MAIN)
                .getCustomVector(new Vector3f(0, 0, 0))
                .add(hmdPos);

        for (ControllerHand hand : ControllerHand.values()) {
            Vec3 handPos = calculateHandPosition(roomPose, hand);
            if (mouthPos.distanceTo(handPos) >= MOUTH_DISTANCE) {
                consuming.put(hand, false);
                continue;
            }

            InteractionHand interactHand = (hand == ControllerHand.MAIN)
                    ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            ItemStack foodItem = (hand == ControllerHand.MAIN)
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
                  //  ClientContext.inputManager.triggerHapticPulse(hand, HAPTIC_PULSE_STRENGTH);
                }
            }

            // Reset consumption state per hand after the duration has passed
            if (Util.getMillis() - eatStartMap.getOrDefault(hand, 0L) > DURATION) {
                consuming.put(hand, false);
            }
        }

        boolean isEating = consuming.getOrDefault(ControllerHand.MAIN, false)
                || consuming.getOrDefault(ControllerHand.OFFHAND, false);


        ControllerHand activeHand = ClientContext.player.getActiveHand();
        if(isEating){
           /* VRKeyBindings.INSTANCE
                    .getMouseRightClick(activeHand)
                    .press();*/
            eatingPressed = true;
        }else if(eatingPressed){
            /*VRKeyBindings.INSTANCE
                    .getMouseRightClick(activeHand)
                    .release();*/
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
        if (!isEnabled() || MC.gameMode == null || player == null
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

    private Vec3 calculateHandPosition(PoseDataImpl roomPose,
                                       ControllerHand hand) {
        Vec3 basePos = ClientContext.rawPoseHandler.getControllerData(hand)
                .getPositionHistory()
                .averagePosition(0.333);
        Vec3 customOffset = roomPose.getController(hand)
                .getCustomVector(new Vector3f(0.0f, 0.0f, -0.1f));
        Vec3 directionOffset = roomPose.getController(hand)
                .getDirection()
                .scale(0.1D);
        return basePos.add(customOffset).add(directionOffset);
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
