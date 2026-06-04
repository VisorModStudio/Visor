package org.vmstudio.visor.core.client.tasks.types;

import lombok.Getter;
import lombok.Setter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.SwingEntityVREvent;
import org.vmstudio.visor.api.client.events.SwingBlockVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.event.VREvent;
import org.vmstudio.visor.api.common.network.toserver.SwingAttackPayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.SwingBlockPayloadToServer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.common.utils.Vector3fHistory;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.compatibility.BlockClassifier;
import org.vmstudio.visor.compatibility.ItemClassifier;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomClimb;
import org.vmstudio.visor.extensions.common.PlayerExtension;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVisorTask
public class TaskSwing extends VisorTask {
    public static final String ID = "swing";

    @Getter
    private static TaskSwing instance;
    @Getter
    public static class HandSwingData {
        private Vec3 lastNonCollidingPosition = new Vec3(0.0D, 0.0D, 0.0D);
        private boolean lastSwingBlock = false;
        private final Vector3fHistory handHistory = new Vector3fHistory(200);
    }



    private static final float SWORD_LENGTH = 0.6F;
    private static final float TOOL_LENGTH = 0.35F;
    private static final float DEFAULT_ITEM_LENGTH = 0.1F;
    private static final double SWING_SPEED_THRESHOLD = 3.0D;


    private final EnumMap<HandType, HandSwingData> handData = new EnumMap<>(HandType.class);

    @Setter
    private int swingInactiveTicks = 3;

    public TaskSwing(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
        // Initialize per-hand data
        for (HandType hand : HandType.values()) {
            handData.put(hand, new HandSwingData());
        }
    }

    @Override
    public void onRun(@Nullable LocalPlayer player) {
        if (player == null) return;

        var relativePose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RELATIVE);
        var tickPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK);
        // Process each controller hand
        for (HandType hand : HandType.values()) {
            final HandSwingData data = handData.get(hand);

            final InteractionHand interactionHand = hand.asInteractionHand();
            final EquipmentSlot equipmentSlot = (hand == HandType.OFFHAND)
                    ? EquipmentSlot.OFFHAND
                    : EquipmentSlot.MAINHAND;

            // Cache frequently accessed values
            final Vec3 handPos = tickPose.getHand(hand).getPositionVec3();
            final Vec3 handDir = tickPose.getHand(hand).getCustomVector3(VRMathUtils.BACK_VECTOR);
            final ItemStack handItemStack = player.getItemInHand(interactionHand);
            final Item handItem = handItemStack.getItem();

            boolean itemRecognized;
            boolean isSword = false;
            if (!ItemClassifier.SWORD.is(handItem) && !ItemClassifier.SPEAR.is(handItem)) {
                itemRecognized = isTool(handItem);
            } else {
                isSword = true;
                itemRecognized = true;
            }

            // Get item properties (length and damage range)
            final ItemProperties properties = getItemProperties(handItemStack, equipmentSlot);
            final float itemLength = properties.itemLength;
            final float damageRange = properties.damageRange;

            // Calculate the swing point based on hand position, direction, and item length
            final Vec3 swingPoint = calculateSwingPoint(handPos, handDir, itemLength);

            // Update hand history for average speed calculation
            final Vec3 controllerPos = relativePose.getHand(hand).getPositionVec3();
            final Vec3 handCustomVector = relativePose.getHand(hand)
                    .getCustomVector3(VRMathUtils.BACK_VECTOR)
                    .scale(0.3D);
            data.handHistory.add(controllerPos.add(handCustomVector).toVector3f());

            final float speed = data.handHistory.averageSpeed(0.33f);
            boolean canSwing = speed > SWING_SPEED_THRESHOLD && !data.lastSwingBlock;

            //----ENTITY ATTACK----
            VREvent event = new SwingEntityVREvent(
                    player, hand,
                    handPos, handDir, swingPoint,
                    itemLength,
                    damageRange,
                    canSwing
            );
            VisorAPI.eventBus().callEvent(event);
            if(event.isCanceled()){
                continue;
            }
            boolean entityHit = handleEntitySwing(
                    player, hand,
                    handPos, handDir,
                    swingPoint,
                    itemLength,
                    damageRange,
                    canSwing
            );

            //----BLOCK MINING----
            // Only allow block swing if not a sword and no entity was hit.
            canSwing = canSwing && !isSword && !entityHit;

            // If climbing and the item isn’t recognized, skip swinging
            if (TaskRoomClimb.getInstance().isGrabbed() && !itemRecognized) {
                continue;
            }

            final BlockPos blockpos = BlockPos.containing(swingPoint);
            final BlockState blockState = MC.level.getBlockState(blockpos);
            final BlockHitResult blockHit = MC.level.clip(new ClipContext(
                    data.lastNonCollidingPosition,
                    swingPoint,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    MC.player
            ));

            if (blockState.isAir() ||
                    blockHit.getType() != HitResult.Type.BLOCK ||
                    data.lastNonCollidingPosition.length() == 0) {
                data.lastNonCollidingPosition = swingPoint;
                data.lastSwingBlock = false;
                continue;
            }

            data.lastSwingBlock = true;
            final boolean sameBlock = blockHit.getBlockPos().equals(blockpos);
            final boolean restrictedBlock = TaskRoomClimb.isClimbableBlock(blockState.getBlock());

            if (!canSwing || restrictedBlock || !sameBlock) {
                continue;
            }
            event = new SwingBlockVREvent(
                    player, hand, handItem, blockState, blockHit, speed
            );
            VisorAPI.eventBus().callEvent(event);
            if(event.isCanceled()){
                continue;
            }

            handleBlockSwing(player, hand, handItem, blockState, blockHit, speed);
        }
    }

    @Override
    public void onClear(@Nullable LocalPlayer player) {
        for (HandType hand : HandType.values()) {
            HandSwingData data = handData.get(hand);
            data.lastNonCollidingPosition = new Vec3(0.0D, 0.0D, 0.0D);
            data.lastSwingBlock = false;
            data.handHistory.clear();
        }
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer p) {
        if (this.swingInactiveTicks > 0) {
            --this.swingInactiveTicks;
            return false;
        }

        return isEnabled()
                && MC.gameMode != null
                && p != null
                && p.isAlive()
                && !p.isSleeping()
                && !p.isCreative()
                && !p.isSpectator()
                && p.getVehicle() == null
                && !TaskRoomClimb.getInstance().isGrabbed()
                && MC.screen == null;
    }

    // Calculates the swing point by adding the scaled hand direction to the hand position.
    private Vec3 calculateSwingPoint(final Vec3 handPos, final Vec3 handDir, final float itemLength) {
        return handPos.add(handDir.scale(itemLength));
    }



    // Computes the effective item length and damage range based on the item type.
    private ItemProperties getItemProperties(final ItemStack itemStack, final EquipmentSlot slot) {
        final boolean isSword = ItemClassifier.SWORD.is(itemStack.getItem()) || ItemClassifier.SPEAR.is(itemStack.getItem());
        final boolean itemRecognized = isSword || isTool(itemStack.getItem());
        float itemLength;
        float damageRange;

        float damageRangeBase = (float) ModLoader.get().getItemEntityReach(3.0, itemStack, slot);
        damageRangeBase = Math.min(damageRangeBase, 6) - 0.5f;

        if (isSword) {
            itemLength = SWORD_LENGTH;
            damageRange = damageRangeBase - itemLength;
        } else if (itemRecognized) {
            itemLength = TOOL_LENGTH;
            damageRange = damageRangeBase * 0.62F - itemLength;
        } else if (!itemStack.isEmpty()) {
            itemLength = DEFAULT_ITEM_LENGTH;
            damageRange = damageRangeBase * 0.16F - itemLength;
        } else {
            itemLength = DEFAULT_ITEM_LENGTH;
            damageRange = 0F;
        }

        itemLength *= ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK).getWorldScale();
        return new ItemProperties(itemLength, damageRange, isSword);
    }

    private boolean handleEntitySwing(final LocalPlayer player,
                                      final HandType hand,
                                      final Vec3 handPos,
                                      final Vec3 handDir,
                                      final Vec3 swingPoint,
                                      final float itemLength,
                                      final float damageRange,
                                      boolean canSwing) {
        boolean canAttack = canSwing;
        if (canAttack) {
            final BlockHitResult blockHitResult = MC.level.clip(new ClipContext(
                    ClientContext.localPlayer
                            .getPoseData(PlayerPoseType.TICK).getHmd().getPositionVec3(),
                    handPos,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    MC.player
            ));
            // Prevent hitting around corners (avoids triggering anticheat)
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                canAttack = false;
            }
        }

        final Vec3 attackPointMobs = restrictToFirstBlock(
                handPos,
                handPos.add(handDir.scale(itemLength + damageRange))
        );
        final AABB damageAreaMobs = new AABB(handPos, attackPointMobs);

        final Vec3 attackPointPlayers = restrictToFirstBlock(handPos, swingPoint);
        final AABB damageAreaPlayers = new AABB(handPos, attackPointPlayers);

        final List<Entity> mobs = MC.level.getEntities(MC.player, damageAreaMobs).stream()
                .filter(entity -> !(entity instanceof Player))
                .collect(Collectors.toList());
        final List<Entity> players = MC.level.getEntities(MC.player, damageAreaPlayers).stream()
                .filter(entity -> entity instanceof Player)
                .toList();

        mobs.addAll(players);

        boolean entityHit = false;
        for (final Entity entity : mobs) {
            if (!entity.isPickable() || entity == MC.getCameraEntity().getVehicle()) {
                continue;
            }
            entityHit = true;
            if (!canAttack) {
                break;
            }
            swingAttack(player, entity, hand);
        }
        return entityHit;
    }

    private void handleBlockSwing(final LocalPlayer player,
                                  final HandType hand,
                                  final Item handItem,
                                  final BlockState blockState,
                                  final BlockHitResult blockHit,
                                  final float speed) {
        var interactionHand = hand.asInteractionHand();
        int totalHits = 3;
        final boolean isFarmItem = ItemClassifier.FARMING_TOOL.is(handItem);
        final boolean isFarmableBlock = isFarmItem &&
                (BlockClassifier.FARMABLE_BLOCK.is(blockState.getBlock()) ||
                        handItem.useOn(new UseOnContext(player, interactionHand, blockHit)).shouldSwing());
        if (isFarmableBlock) {
            MC.gameMode.useItemOn(player, interactionHand, blockHit);
        } else {
            // Swing faster = more damage.
            totalHits = (int) (totalHits + Math.min(speed - SWING_SPEED_THRESHOLD, 4.0D));
            swingMining(blockHit, blockState, totalHits, hand);
        }
        ClientContext.inputManager.triggerHapticPulseMicroSec(hand, 250 * totalHits);
    }

    private void swingAttack(final Player player, final Entity entity, final HandType handType) {
        if (VRServerSettings.isBetterSwinging()) {
            attackBetter(player, entity, handType);
        } else {
            attackVanilla(player, entity);
        }
        ClientContext.inputManager.triggerHapticPulseMicroSec(handType, 1000);
        handData.get(handType).lastSwingBlock = true;
    }

    public static void attackBetter(final Player player, final Entity entity, HandType handType) {
        ClientNetworking.sendVRPacket(new SwingAttackPayloadToServer(entity.getId(), player.isShiftKeyDown(), handType==HandType.MAIN));
        if (MC.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            ((PlayerExtension) player).visor$swingAttack(entity, handType);
            player.resetAttackStrengthTicker();
        }
    }

    private void attackVanilla(final Player player, final Entity entity) {
        MC.gameMode.attack(player, entity);
    }

    private void swingMining(final BlockHitResult blockHit,
                             final BlockState blockState,
                             final int totalHits,
                             final HandType handType) {
        if (VRServerSettings.isBetterSwinging()) {
            mineBetter(blockHit, blockState, totalHits, handType);
        } else {
            mineVanilla(blockHit, totalHits);
        }
        blockDust(
                blockHit.getLocation().x,
                blockHit.getLocation().y,
                blockHit.getLocation().z,
                3 * totalHits,
                blockState,
                0.6F,
                1.0F
        );
    }

    private void mineBetter(final BlockHitResult blockHit,
                            final BlockState blockState,
                            final int totalHits,
                            HandType handType) {
        for (int hit = 0; hit < totalHits; ++hit) {
            startPrediction(MC.level, sequence -> new SwingBlockPayloadToServer(
                    blockHit.getBlockPos(),
                    blockHit.getDirection(),
                    handType == HandType.MAIN,
                    sequence
            ));
        }
        final SoundType soundType = blockState.getSoundType();
        MC.getSoundManager().play(new SimpleSoundInstance(
                soundType.getHitSound(),
                SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 8.0F,
                soundType.getPitch() * 0.5F,
                SoundInstance.createUnseededRandom(),
                blockHit.getBlockPos()
        ));
    }

    private void mineVanilla(final BlockHitResult blockHit, final int totalHits) {
        MC.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
        if (!isHittingBlock()) return;
        for (int hit = 0; hit < totalHits; ++hit) {
            if (MC.gameMode.continueDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection())) {
                MC.particleEngine.crack(blockHit.getBlockPos(), blockHit.getDirection());
            }
            if (!isHittingBlock()) {
                break;
            }
        }
        Minecraft.getInstance().gameMode.destroyDelay = 0;
    }

    private void startPrediction(final ClientLevel clientLevel, final PredictiveVrAction predictiveAction) {
        try (BlockStatePredictionHandler handler = clientLevel.getBlockStatePredictionHandler().startPredicting()) {
            final int sequence = handler.currentSequence();
            final SwingBlockPayloadToServer packet = predictiveAction.predict(sequence);
            ClientNetworking.sendVRPacket(packet);
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean isHittingBlock() {
        return Minecraft.getInstance().gameMode.isDestroying();
    }

    private Vec3 restrictToFirstBlock(final Vec3 start, final Vec3 end) {
        final BlockHitResult hitResult = MC.level.clip(new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                MC.player
        ));
        return hitResult.getType() == HitResult.Type.BLOCK ? hitResult.getLocation() : end;
    }

    private void blockDust(double x, double y, double z,
                           int count,
                           BlockState bs, float scale,
                          float velscale
    ) {

        for (int i = 0; i < count; ++i) {
            TerrainParticle particle = new TerrainParticle(
                    MC.level,
                    x, y, z,
                    0.0D, 0.0D, 0.0D,
                    bs
            );
            particle.setPower(velscale);

            MC.particleEngine.add(particle.scale(scale));
        }
    }

    public static boolean isTool(final Item item) {
        return item instanceof DiggerItem
                || item instanceof ArrowItem
                || item instanceof FishingRodItem
                || item instanceof FoodOnAStickItem
                || item instanceof ShearsItem
                || item == Items.BONE
                || item == Items.BLAZE_ROD
                || item == Items.BAMBOO
                || item == Items.TORCH
                || item == Items.REDSTONE_TORCH
                || item == Items.STICK
                || item instanceof DebugStickItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem
                || item instanceof HoeItem
                || item instanceof AxeItem
                || item instanceof PickaxeItem
                || item instanceof ShovelItem;
    }


    public @NotNull HandSwingData getSwingData(@NotNull HandType hand){
        return handData.get(hand);
    }
    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }




    @FunctionalInterface
    private interface PredictiveVrAction {
        SwingBlockPayloadToServer predict(int sequence);
    }

    private record ItemProperties(float itemLength,
                                  float damageRange,
                                  boolean isSword) { }
}
