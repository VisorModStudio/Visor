package org.vmstudio.visor.core.client.tasks.types;

import org.vmstudio.visor.api.client.input.HapticFeedback;
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
import net.minecraft.util.Mth;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
import org.vmstudio.visor.api.compatibility.BlockClassifier;
import org.vmstudio.visor.api.compatibility.ItemClassifier;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomClimb;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.extensions.common.PlayerExtension;
import org.vmstudio.visor.mixin.client.accessors.BlockAccessor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

//@TODO IT IS PROTOTYPE! REWORK FROM SCRATCH AFTER 0.7.0
@RegisterVisorTask
public class TaskSwing extends VisorTask {
    public static final String ID = "swing";

    @Getter
    private static TaskSwing instance;
    @Getter
    public static class HandSwingData {
        private boolean lastSwingBlock = false;
        private final Vector3fHistory handHistory = new Vector3fHistory(200);

        private List<Entity> lastHitEntities = List.of();

        private Vec3 lastHandPos = null;
        private Quaternionf lastHandRot = null;
        private Vec3 lastSwingPoint = null;
        private Vec3 lastAttackPoint = null;
        private Vec3 lastWeaponTip = null;

        private void resetSwingState() {
            lastSwingBlock = false;
            lastHitEntities = List.of();
            lastHandPos = null;
            lastHandRot = null;
            lastSwingPoint = null;
            lastAttackPoint = null;
            lastWeaponTip = null;
        }
    }



    private static final float SWORD_LENGTH = 0.6F;
    private static final float TOOL_LENGTH = 0.35F;
    private static final float DEFAULT_ITEM_LENGTH = 0.1F;
    private static final float FIST_REACH = 0.3F;
    private static final float TIP_OFFSET = 0.3F;
    private static final int MAX_ARC_SUBDIVISIONS = 8;
    // vanilla GameRenderer.pick treats 3.0 as the entity interaction distance
    private static final double VANILLA_ENTITY_REACH = 3.0D;
    // vanilla Entity.getInputVector rejects a direction below this squared length
    private static final double DEGENERATE_SEGMENT_SQ = 1.0E-7D;
    private static final double DEGENERATE_NUDGE = 0.001D;
    private static final float MAX_OVERSPEED_BONUS = 4.0F;
    private static final int DUST_PER_HIT = 3;
    private static final float DUST_SPEED = 0.6F;
    private static final int VANILLA_MINING_KEEP_TICKS = 20;
    private static final int BREAK_CONFIRM_TICKS = 40;

    private BlockPos vanillaMiningPos = null;
    private long vanillaMiningLastTick;
    //wait for server decision to break block
    private final Map<BlockPos, PendingBreak> pendingBreaks = new HashMap<>();


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
        confirmPendingBreaks();

        var relativePose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.ROOM);
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
            final Vec3 handDir = tickPose.getHand(hand).transformDirectionVec3(VRMathUtils.FORWARD_VECTOR);
            final Quaternionf handRot = new Quaternionf()
                    .setFromNormalized(tickPose.getHand(hand).getRotation());

            // Update hand history for average speed calculation
            final Vec3 controllerPos = relativePose.getHand(hand).getPositionVec3();
            final Vec3 handCustomVector = relativePose.getHand(hand)
                    .transformDirectionVec3(VRMathUtils.FORWARD_VECTOR)
                    .scale(TIP_OFFSET);
            data.handHistory.add(controllerPos.add(handCustomVector).toVector3f());
            final float speed = data.handHistory.averageSpeed(0.31f);

            // Don't swing with a hand that is busy using an item
            if (player.isUsingItem()
                    && player.getUsedItemHand() == interactionHand) {
                data.resetSwingState();
                continue;
            }

            final ItemStack handItemStack = player.getItemInHand(interactionHand);
            final Item handItem = handItemStack.getItem();

            // Get item properties (length and damage range)
            final ItemProperties properties = getItemProperties(handItemStack, equipmentSlot);
            final float itemLength = properties.itemLength;
            final float damageRange = properties.damageRange;
            final boolean isSword = properties.isSword;
            final boolean itemRecognized = isSword || isTool(handItem);

            // Calculate the swing point based on hand position, direction, and item length
            final Vec3 swingPoint = calculateSwingPoint(handPos, handDir, itemLength);

            // Previous tick pose for arc interpolation
            final Vec3 prevHandPos = data.lastHandPos;
            final Quaternionf prevHandRot = data.lastHandRot;
            final Vec3 prevSwingPoint = data.lastSwingPoint;
            data.lastHandPos = handPos;
            data.lastHandRot = handRot;
            data.lastSwingPoint = swingPoint;

            boolean canSwing = speed > effectiveSpeedThreshold() && !data.lastSwingBlock;
            // Re-armed unless a block gets hit below
            data.lastSwingBlock = false;

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
                data.lastAttackPoint = null;
                data.lastWeaponTip = null;
                data.lastHitEntities = List.of();
                continue;
            }
            final boolean attackedEntity = handleEntitySwing(
                    player, hand, data,
                    handPos, handDir,
                    swingPoint,
                    itemLength,
                    damageRange,
                    speed,
                    canSwing
            );

            //----BLOCK MINING----
            // Only allow block swing if no entity was attacked.
            canSwing = canSwing && !attackedEntity;

            // If climbing and the item isn’t recognized, skip swinging
            if (TaskRoomClimb.getInstance().isGrabbed() && !itemRecognized) {
                continue;
            }
            if (!canSwing) {
                continue;
            }

            final BlockHitResult blockHit = findBlockHitAlongArc(
                    player, handItemStack, isSword,
                    prevHandPos, prevHandRot, prevSwingPoint,
                    handPos, handRot, swingPoint,
                    itemLength
            );
            if (blockHit == null) {
                continue;
            }
            final BlockState blockState = MC.level.getBlockState(blockHit.getBlockPos());

            event = new SwingBlockVREvent(
                    player, hand, handItem, blockState, blockHit, speed
            );
            VisorAPI.eventBus().callEvent(event);
            if(event.isCanceled()){
                continue;
            }

            data.lastSwingBlock = true;
            handleBlockSwing(player, hand, handItem, blockState, blockHit, speed);
        }
    }

    @Override
    public void onClear(@Nullable LocalPlayer player) {
        for (HandType hand : HandType.values()) {
            HandSwingData data = handData.get(hand);
            data.resetSwingState();
            data.handHistory.clear();
        }
        vanillaMiningPos = null;
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
                && (VRClientSettings.isSwingInCreative() || !p.isCreative())
                && !p.isSpectator()
                && p.getVehicle() == null
                && !TaskRoomClimb.getInstance().isGrabbed()
                && MC.screen == null
                && (VRServerSettings.isAttacksWhileBlocking() || !p.isBlocking());
    }

    // Calculates the swing point by adding the scaled hand direction to the hand position.
    private Vec3 calculateSwingPoint(final Vec3 handPos, final Vec3 handDir, final float itemLength) {
        return handPos.add(handDir.scale(itemLength));
    }

    private static float effectiveSpeedThreshold() {
        return Math.max(0.5F, VRClientSettings.getSwingSpeedThreshold());
    }



    // Computes the effective item length and damage range based on the item type.
    private ItemProperties getItemProperties(final ItemStack itemStack, final EquipmentSlot slot) {
        final boolean isSword = ItemClassifier.SWORD.is(itemStack.getItem()) || ItemClassifier.SPEAR.is(itemStack.getItem());

        float itemLength;
        float damageRange;
        if (itemStack.isEmpty()) {
            itemLength = 0F;
            damageRange = FIST_REACH;
        } else {
            float reach = (float) ModLoader.get()
                    .getItemEntityReach(VANILLA_ENTITY_REACH, itemStack, slot);
            reach = Math.min(reach, 6) - 0.5f;

            // longer items get more of the base reach
            float reachShare;
            if (isSword) {
                itemLength = SWORD_LENGTH;
                reachShare = 1F;
            } else if (isTool(itemStack.getItem())) {
                itemLength = TOOL_LENGTH;
                reachShare = 0.62F;
            } else {
                itemLength = DEFAULT_ITEM_LENGTH;
                reachShare = 0.16F;
            }
            damageRange = reach * reachShare - itemLength;
        }

        itemLength *= ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK).getWorldScale();
        return new ItemProperties(itemLength, damageRange, isSword);
    }

    // Attacks entities within the weapon reach. Each entity gets hit at most once per swing.
    private boolean handleEntitySwing(final LocalPlayer player,
                                      final HandType hand,
                                      final HandSwingData data,
                                      final Vec3 handPos,
                                      final Vec3 handDir,
                                      final Vec3 swingPoint,
                                      final float itemLength,
                                      final float damageRange,
                                      final float speed,
                                      final boolean canSwing) {
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
            if (blockHitResult.getType() != HitResult.Type.MISS) {
                canAttack = false;
            }
        }

        final Vec3 attackPointMobs = restrictToFirstBlock(
                handPos,
                handPos.add(handDir.scale(itemLength + damageRange))
        );
        final Vec3 attackPointPlayers = restrictToFirstBlock(handPos, swingPoint);

        AABB damageAreaMobs = new AABB(handPos, attackPointMobs);
        AABB damageAreaPlayers = new AABB(handPos, attackPointPlayers);
        // Include previous tick points, so fast swings can't skip past targets
        if (data.lastWeaponTip != null) {
            damageAreaMobs = includePoint(damageAreaMobs, data.lastWeaponTip);
        }
        if (data.lastAttackPoint != null) {
            damageAreaPlayers = includePoint(damageAreaPlayers, data.lastAttackPoint);
        }
        data.lastWeaponTip = attackPointMobs;
        data.lastAttackPoint = attackPointPlayers;

        // Shorter reach against players, to avoid accidental pvp hits
        final List<Entity> targets = MC.level.getEntities(
                MC.player, damageAreaMobs,
                entity -> !entity.isSpectator() && !(entity instanceof Player));
        targets.addAll(MC.level.getEntities(
                MC.player, damageAreaPlayers,
                entity -> !entity.isSpectator() && entity instanceof Player));

        boolean attacked = false;
        if (canAttack) {
            final Entity vehicle = MC.getCameraEntity().getVehicle();
            for (final Entity target : targets) {
                if (!target.isPickable() || target == vehicle
                        || data.lastHitEntities.contains(target)) {
                    continue;
                }
                swingAttack(player, target, hand);
                attacked = true;
            }
        }
        data.lastHitEntities = speed > effectiveSpeedThreshold()
                ? targets : List.of();
        return attacked;
    }


    private BlockHitResult findBlockHitAlongArc(final LocalPlayer player,
                                                final ItemStack handItemStack,
                                                final boolean isSword,
                                                final Vec3 prevHandPos,
                                                final Quaternionf prevHandRot,
                                                final Vec3 prevSwingPoint,
                                                final Vec3 handPos,
                                                final Quaternionf handRot,
                                                final Vec3 swingPoint,
                                                final float itemLength) {
        final List<Vec3> points = new ArrayList<>();
        if (prevHandPos != null && prevHandRot != null && prevSwingPoint != null) {
            // subdivide proportionally to how far the hand rotated since last tick
            final float cosHalfTurn = Math.min(1.0F, Math.abs(handRot.dot(prevHandRot)));
            final float turnAngle = 2.0F * (float) Math.acos(cosHalfTurn);
            final int steps = Mth.floor(turnAngle / Mth.PI * MAX_ARC_SUBDIVISIONS);

            points.add(prevSwingPoint);
            final Quaternionf stepRot = new Quaternionf();
            final Vector3f stepTip = new Vector3f();
            for (int s = 1; s < steps; s++) {
                final float t = s / (float) steps;
                prevHandRot.slerp(handRot, t, stepRot);
                stepRot.transform(0.0F, 0.0F, -itemLength, stepTip);
                final Vec3 base = prevHandPos.lerp(handPos, t);
                points.add(base.add(stepTip.x, stepTip.y, stepTip.z));
            }
        } else {
            points.add(swingPoint);
        }
        points.add(swingPoint);

        for (int p = 1; p < points.size(); p++) {
            final Vec3 start = points.get(p - 1);
            Vec3 end = points.get(p);
            if (start.subtract(end).lengthSqr() < DEGENERATE_SEGMENT_SQ) {
                // pad degenerate segments, clip() reports a miss for near-zero spans
                end = end.add(DEGENERATE_NUDGE, DEGENERATE_NUDGE, DEGENERATE_NUDGE);
            }
            final BlockHitResult hit = MC.level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));
            if (hit.getType() != HitResult.Type.BLOCK) {
                continue;
            }
            final BlockState state = MC.level.getBlockState(hit.getBlockPos());
            if (TaskRoomClimb.isClimbableBlock(state.getBlock())) {
                continue;
            }
            // Swords only interact with blocks they break instantly
            if (isSword && !canSwordBreak(player, handItemStack, state, hit)) {
                continue;
            }
            // An inside hit means the tip started buried in the block
            return hit.isInside() ? null : hit;
        }
        return null;
    }

    private static boolean canSwordBreak(final LocalPlayer player,
                                         final ItemStack itemStack,
                                         final BlockState state,
                                         final BlockHitResult hit) {
        return CommonUtils.withForcedHand(itemStack, () ->
                itemStack.isCorrectToolForDrops(state)
                        || state.getDestroyProgress(player, player.level(), hit.getBlockPos()) == 1.0F);
    }

    private static AABB includePoint(final AABB box, final Vec3 point) {
        return box.minmax(new AABB(point, point));
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
                        handItem.useOn(new UseOnContext(
                                player.level(), null, interactionHand,
                                player.getItemInHand(interactionHand).copy(), blockHit
                        )).shouldSwing());
        if (isFarmableBlock) {
            MC.gameMode.useItemOn(player, interactionHand, blockHit);
        } else {
            // Swing faster = more damage.
            float overSpeed = Math.min(speed - effectiveSpeedThreshold(), MAX_OVERSPEED_BONUS);
            totalHits += (int) overSpeed;
            swingMining(blockHit, blockState, totalHits, hand);
        }
        ClientContext.inputManager.triggerHapticPulseMicroSec(hand, HapticFeedback.SWING_BLOCK_PER_HIT * totalHits);
    }

    private void swingAttack(final Player player, final Entity entity, final HandType handType) {
        if (VRServerSettings.isBetterSwinging()) {
            attackBetter(player, entity, handType);
        } else {
            attackVanilla(player, entity, handType);
        }
        ClientContext.inputManager.triggerHapticPulseMicroSec(handType, HapticFeedback.SWING_ATTACK);
    }

    public static void attackBetter(final Player player, final Entity entity, HandType handType) {
        ClientNetworking.sendVRPacket(new SwingAttackPayloadToServer(entity.getId(), player.isShiftKeyDown(), handType==HandType.MAIN));
        if (MC.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            ((PlayerExtension) player).visor$swingAttack(entity, handType);
            player.resetAttackStrengthTicker();
        }
    }

    private void attackVanilla(final Player player, final Entity entity, final HandType handType) {
        switchActiveHand(handType);
        MC.gameMode.attack(player, entity);
    }


    private void switchActiveHand(final HandType handType) {
        if (VRServerSettings.isTwoHandedVR()) {
            ClientContext.localPlayer.setActiveHand(handType);
        }
    }

    private void swingMining(final BlockHitResult blockHit,
                             final BlockState blockState,
                             final int totalHits,
                             final HandType handType) {
        if (VRServerSettings.isBetterSwinging()) {
            mineBetter(blockHit, blockState, totalHits, handType);
        } else {
            mineVanilla(blockHit, totalHits, handType);
        }
        blockDust(blockHit.getLocation(), DUST_PER_HIT * totalHits, blockState, DUST_SPEED, 1.0F);
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
        pendingBreaks.put(blockHit.getBlockPos(), new PendingBreak(blockState, VisorState.TICK_COUNT));
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

    // vanilla plays the break sound effect from destroyBlock local call,
    //swinging do not use that, so, we need this replacement
    private void confirmPendingBreaks() {
        if (pendingBreaks.isEmpty()) {
            return;
        }
        var iterator = pendingBreaks.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            PendingBreak pending = entry.getValue();
            if (VisorState.TICK_COUNT - pending.tick() > BREAK_CONFIRM_TICKS) {
                iterator.remove();
                continue;
            }
            if (MC.level.getBlockState(entry.getKey()).is(pending.state().getBlock())) {
                continue;
            }
            ((BlockAccessor) pending.state().getBlock()).visor$spawnDestroyParticles(
                    MC.level, MC.player, entry.getKey(), pending.state()
            );
            iterator.remove();
        }
    }

    private void mineVanilla(final BlockHitResult blockHit, final int totalHits, final HandType handType) {
        switchActiveHand(handType);
        MC.gameMode.startDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection());
        if (isHittingBlock()) {
            for (int hit = 0; hit < totalHits; ++hit) {
                if (MC.gameMode.continueDestroyBlock(blockHit.getBlockPos(), blockHit.getDirection())) {
                    MC.particleEngine.crack(blockHit.getBlockPos(), blockHit.getDirection());
                }
                if (!isHittingBlock()) {
                    break;
                }
            }
            MC.gameMode.destroyDelay = 0;
        }
        if (isHittingBlock()) {
            vanillaMiningPos = MC.gameMode.destroyBlockPos;
            vanillaMiningLastTick = VisorState.TICK_COUNT;
        } else {
            vanillaMiningPos = null;
        }
    }


    public boolean isKeepingVanillaMining() {
        if (vanillaMiningPos == null) {
            return false;
        }
        if (MC.gameMode == null
                || !MC.gameMode.isDestroying()
                || !vanillaMiningPos.equals(MC.gameMode.destroyBlockPos)
                || VisorState.TICK_COUNT - vanillaMiningLastTick > VANILLA_MINING_KEEP_TICKS) {
            vanillaMiningPos = null;
            return false;
        }
        return true;
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

    private void blockDust(Vec3 at, int count, BlockState state,
                           float sizeScale, float speedScale) {
        for (int i = 0; i < count; ++i) {
            TerrainParticle particle = new TerrainParticle(
                    MC.level,
                    at.x, at.y, at.z,
                    0.0D, 0.0D, 0.0D,
                    state
            );
            particle.setPower(speedScale);
            MC.particleEngine.add(particle.scale(sizeScale));
        }
    }

    private static final Set<Item> HANDHELD_MISC = Set.of(
            Items.STICK, Items.BONE, Items.BAMBOO, Items.BLAZE_ROD,
            Items.TORCH, Items.REDSTONE_TORCH
    );

    public static boolean isTool(final Item item) {
        return HANDHELD_MISC.contains(item)
                || item instanceof DiggerItem
                || item instanceof ShearsItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem
                || item instanceof FishingRodItem
                || item instanceof FoodOnAStickItem
                || item instanceof ArrowItem
                || item instanceof DebugStickItem;
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

    private record PendingBreak(BlockState state, int tick) { }
}
