package org.vmstudio.visor.mixin.common.player;

import me.phoenixra.atumconfig.api.tuples.PairRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.toclient.BlockDamagePayloadToClient;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.server.network.ServerNetworking;
import org.vmstudio.visor.extensions.common.ServerPlayerGameModeExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin implements ServerPlayerGameModeExtension {
    @Shadow
    protected ServerLevel level;
    @Shadow
    @Final
    protected ServerPlayer player;

    @Shadow
    private GameType gameModeForPlayer;
    @Shadow
    private BlockPos destroyPos;
    @Shadow
    private BlockPos delayedDestroyPos;
    @Shadow
    private int destroyProgressStart;
    @Shadow
    private int delayedTickStart;
    @Shadow
    private int lastSentState;
    @Shadow
    private boolean isDestroyingBlock;
    @Shadow
    private boolean hasDelayedDestroy;

    @Shadow
    private int gameTicks;


    @Unique
    private Map<Long, PairRecord<Long, Float>>
            visor$blockDamage = new HashMap<>();

    @Unique
    private Map<Long, Integer> visor$blockDamageStart = new HashMap<>();

    @Unique
    private static final int visor$MINING_BONUS_TICKS = 2;


    @Shadow
    protected abstract void debugLogging(BlockPos blockPos,
                                         boolean bl, int i, String string
    );

    @Shadow
    public abstract boolean isCreative();

    @Shadow
    public abstract boolean destroyBlock(BlockPos blockPos);
    @Shadow
    public abstract void destroyAndAck(BlockPos pos, int sequence, String message);

    @Shadow
    protected abstract float incrementDestroyProgress(
            BlockState blockState,
            BlockPos blockPos,
            int i
    );


    /* ***************************************** *\
  //--------TWO HANDED VR (OFFHAND SUPPORT)--------\\
    \* ***************************************** */
    @Redirect(method = "destroyBlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack visor$destroyBlock(ServerPlayer player) {
        if(!VRServerSettings.isTwoHandedVR()) return player.getMainHandItem();
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null) {
            return player.getMainHandItem();
        }
        if (vrPlayer.getActiveHand() == HandType.OFFHAND) {
            return player.getOffhandItem();
        } else {
            return player.getMainHandItem();
        }
    }

    /* ************************* *\
  //--------BETTER SWINGING--------\\
    \* ************************* */

    @Inject(method = "tick", at = @At("HEAD"))
    private void visor$tickCleanupForVanillaMining(CallbackInfo ci) {
        if (visor$isBetterSwingingNotActive()) return;
        if (this.hasDelayedDestroy) {
            if (visor$forgetBlockDamage(this.delayedDestroyPos.asLong())) {
                visor$sendSwingDamageCleanUp(this.delayedDestroyPos, true);
            }
        } else if (this.isDestroyingBlock) {
            if (visor$forgetBlockDamage(this.destroyPos.asLong())) {
                visor$sendSwingDamageCleanUp(this.destroyPos, true);
            }
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void visor$tickDecayVrDamage(CallbackInfo ci) {
        if (visor$isBetterSwingingNotActive()) return;
        List<Long> remove = new ArrayList<>();
        for (var entry : visor$blockDamage.entrySet()) {
            long delay = entry.getValue().first();
            BlockPos pos = BlockPos.of(entry.getKey());
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || delay <= 0) {
                this.level.destroyBlockProgress(this.player.getId(), pos, -1);
                remove.add(entry.getKey());
                continue;
            }
            entry.getValue().setFirst(delay - 1);
        }
        remove.forEach(key -> {
            visor$forgetBlockDamage(key);
            visor$sendSwingDamageCleanUp(BlockPos.of(key), false);
        });
    }

    @Unique
    private boolean visor$forgetBlockDamage(long key) {
        boolean removed = visor$blockDamage.remove(key) != null;
        return visor$blockDamageStart.remove(key) != null || removed;
    }

    @Unique
    @Override
    public void visor$handleVrBlockDamage(BlockPos blockPos,
                                          Direction direction,
                                          int i, int j,
                                          ItemStack usedItem
    ) {
        if (visor$isBetterSwingingNotActive()){
            VisorAPI.server().getLogger().info("Received BlockSwingDamage " +
                    "packet while this feature is disabled!");
            return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(player);
        if (vrPlayer == null) return;

        double dist = this.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(blockPos));
        if (dist > ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE) {
            this.debugLogging(blockPos, false, j, "too far");
            return;
        } else if (blockPos.getY() >= i) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, j, "too high");
            return;
        }

        BlockState blockState;
        if (!this.level.mayInteract(this.player, blockPos)) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, j, "may not interact");
            return;
        }

        this.hasDelayedDestroy = false;
        this.isDestroyingBlock = false;
        if (this.isCreative()) {
            this.visor$destroyAndAck(
                    blockPos, j, "creative destroy",
                    usedItem
            );
            return;
        }

        if (visor$blockActionRestricted(this.level, blockPos, this.gameModeForPlayer, usedItem)) {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, j, "block action restricted");
            return;
        }

        float blockDamage = 1.0F;
        blockState = this.level.getBlockState(blockPos);
        if (!blockState.isAir()) {
            blockState.attack(this.level, blockPos, this.player);
            float savedDamage = 0;
            PairRecord<Long, Float> savedBlockDamage = visor$blockDamage.get(blockPos.asLong());
            if (savedBlockDamage != null) {
                savedDamage = savedBlockDamage.second();
            }
            float step = visor$getDestroyProgress(
                    blockState,
                    this.player,
                    this.player.level(),
                    blockPos, usedItem
            );
            Integer startTick = visor$blockDamageStart.get(blockPos.asLong());
            int elapsed = startTick != null
                    ? Math.max(0, this.gameTicks - startTick) : 0;
            float speedFactor = Math.max(0.1F, VRServerSettings.getSwingingMiningSpeed());
            float allowed = step * (elapsed + visor$MINING_BONUS_TICKS) * speedFactor;
            blockDamage = Math.max(savedDamage, Math.min(savedDamage + step, allowed));
        }

        if (!blockState.isAir() && blockDamage >= 1.0F) {
            this.visor$destroyAndAck(
                    blockPos, j,
                    "instant mine",
                    usedItem
            );
        } else {
            int blockDamageInt = (int) (blockDamage * 10.0F);

            this.level.destroyBlockProgress(this.player.getId(), blockPos, blockDamageInt);
            //send damage block progress to player who damaged it,
            //since destroyBlockProgress method ignores this player
            double d = (double) blockPos.getX() - player.getX();
            double e = (double) blockPos.getY() - player.getY();
            double f = (double) blockPos.getZ() - player.getZ();
            if (d * d + e * e + f * f < 1024.0) {
                player.connection.send(new ClientboundBlockDestructionPacket(this.player.getId(), blockPos, blockDamageInt));
            }
            ServerNetworking.sendPacketToTrackedVRPlayers(
                    player,
                    true,
                    new BlockDamagePayloadToClient(
                            player.getUUID(),
                            blockPos,
                            blockDamageInt
                    )
            );

            //save data
            visor$blockDamage.put(
                    blockPos.asLong(),
                    new PairRecord<>(
                            VRServerSettings.getSwingingRepairDelay(),
                            blockDamage
                    )
            );
            visor$blockDamageStart.putIfAbsent(blockPos.asLong(), this.gameTicks);

            this.debugLogging(blockPos,
                    true, j, "actual start of destroying"
            );
        }
    }

    @Unique
    public void visor$destroyAndAck(BlockPos blockPos,
                                    int i, String string,
                                    ItemStack usedItem
    ) {
        visor$forgetBlockDamage(blockPos.asLong());
        visor$sendSwingDamageCleanUp(blockPos, false);

        if (this.visor$destroyBlock(blockPos, usedItem)) {
            this.debugLogging(blockPos, true, i, string);
        } else {
            this.player.connection.send(new ClientboundBlockUpdatePacket(blockPos, this.level.getBlockState(blockPos)));
            this.debugLogging(blockPos, false, i, string);
        }
    }


    @Unique
    public boolean visor$destroyBlock(BlockPos blockPos, ItemStack usedItem) {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (!usedItem.getItem().canAttackBlock(blockState, this.level, blockPos, this.player)) {
            return false;
        } else {
            BlockEntity blockEntity = this.level.getBlockEntity(blockPos);
            Block block = blockState.getBlock();
            if (block instanceof GameMasterBlock && !this.player.canUseGameMasterBlocks()) {
                this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                return false;
            } else if (visor$blockActionRestricted(this.level, blockPos, this.gameModeForPlayer, usedItem)) {
                return false;
            } else {
                block.playerWillDestroy(this.level, blockPos, blockState, this.player);
                boolean bl = this.level.removeBlock(blockPos, false);
                if (bl) {
                    block.destroy(this.level, blockPos, blockState);
                }

                if (this.isCreative()) {
                    return true;
                } else {
                    ItemStack itemStack2 = usedItem.copy();
                    boolean bl2 = !blockState.requiresCorrectToolForDrops()
                            || visor$hasCorrectToolForDrops(blockState, usedItem);
                    usedItem.mineBlock(this.level, blockState, blockPos, this.player);
                    if (bl && bl2) {
                        block.playerDestroy(this.level, this.player, blockPos, blockState, blockEntity, itemStack2);
                    }

                    return true;
                }
            }
        }
    }

    @Unique
    private boolean visor$hasCorrectToolForDrops(BlockState blockState, ItemStack usedItem) {
        return !blockState.requiresCorrectToolForDrops()
                || usedItem.isCorrectToolForDrops(blockState);
    }

    @Unique
    public boolean visor$blockActionRestricted(Level level,
                                               BlockPos blockPos,
                                               GameType gameType,
                                               ItemStack itemUsed
    ) {
        if (!gameType.isBlockPlacingRestricted()) {
            return false;
        } else if (gameType == GameType.SPECTATOR) {
            return true;
        } else if (player.mayBuild()) {
            return false;
        } else {
            return itemUsed.isEmpty()
                    || !itemUsed.hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(level, blockPos, false));
        }
    }

    @Unique
    private float visor$getDestroyProgress(BlockState blockState,
                                           Player player,
                                           BlockGetter blockGetter,
                                           BlockPos blockPos,
                                           ItemStack itemUsed
    ) {
        float blockDestroySpeed = blockState.getDestroySpeed(blockGetter, blockPos);
        if (blockDestroySpeed == -1.0F) {
            return 0.0F;
        } else {
            int i = !blockState.requiresCorrectToolForDrops()
                    || visor$hasCorrectToolForDrops(blockState, itemUsed) ? 30 : 100;
            return visor$getDamageStep(player, itemUsed, blockState)
                    / blockDestroySpeed
                    / (float) i;
        }
    }

    @Unique
    private float visor$getDamageStep(@NotNull Player player,
                                      @NotNull ItemStack itemUsed,
                                      BlockState blockState
    ) {
        float f = itemUsed.getDestroySpeed(blockState);
        if (f > 1.0F) {
            int i = EnchantmentHelper.getBlockEfficiency(player);
            if (i > 0 && !itemUsed.isEmpty()) {
                f += (float) (i * i + 1);
            }
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (float) (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            float g = switch (player.getEffect(MobEffects.DIG_SLOWDOWN)
                    .getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };

            f *= g;
        }

        if (player.isEyeInFluid(FluidTags.WATER)
                && !EnchantmentHelper.hasAquaAffinity(player)) {
            f /= 5.0F;
        }

        if (!player.onGround()) {
            f /= 5.0F;
        }

        return f;
    }



    @Unique
    private void visor$sendSwingDamageCleanUp(BlockPos blockPos, boolean onlyVrData) {
        ServerNetworking.sendPacketToTrackedVRPlayers(
                player,
                true,
                new BlockDamagePayloadToClient(
                        player.getUUID(),
                        blockPos,
                        onlyVrData ? -2 : -1
                )
        );
        if (!onlyVrData) {
            player.connection.send(
                    new ClientboundBlockDestructionPacket(
                            this.player.getId(), blockPos, -1
                    )
            );
        }
    }

    @Unique
    private boolean visor$isBetterSwingingNotActive() {
        if (!VRServerSettings.isBetterSwinging()) return true;
        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(player);
        return vrPlayer == null;
    }
}
