package org.vmstudio.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
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
import org.vmstudio.visor.api.common.network.toserver.SwingBlockPayloadToServer;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.api.server.player.VRServerPlayer;
import org.vmstudio.visor.core.common.CommonUtils;
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

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void visor$tick(CallbackInfo ci) {
        if (!VRServerSettings.isBetterVrSwinging()){
            return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVrPlayer(player);
        if (vrPlayer == null) return;
        ci.cancel();
        ++this.gameTicks;
        BlockState blockState;
        if (this.hasDelayedDestroy) {
            //clean vr swinging staff for this block
            // since vanilla mining used
            visor$blockDamage.remove(delayedDestroyPos.asLong());
            visor$sendSwingDamageCleanUp(delayedDestroyPos, true);


            blockState = this.level.getBlockState(this.delayedDestroyPos);
            if (blockState.isAir()) {
                this.hasDelayedDestroy = false;
            } else {
                float f = this.incrementDestroyProgress(blockState, this.delayedDestroyPos, this.delayedTickStart);
                if (f >= 1.0F) {
                    this.hasDelayedDestroy = false;
                    this.destroyBlock(this.delayedDestroyPos);
                }
            }

        } else if (this.isDestroyingBlock) {
            //clean vr swinging staff for this block
            // since vanilla mining used
            visor$blockDamage.remove(destroyPos.asLong());
            visor$sendSwingDamageCleanUp(destroyPos, true);

            blockState = this.level.getBlockState(this.destroyPos);
            if (blockState.isAir()) {
                this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1);
                this.lastSentState = -1;
                this.isDestroyingBlock = false;
            } else {
                this.incrementDestroyProgress(blockState, this.destroyPos, this.destroyProgressStart);
            }

        }

        //VR Swinging
        List<Long> remove = new ArrayList<>();
        for (Map.Entry<Long, PairRecord<Long, Float>> entry
                : visor$blockDamage.entrySet()) {
            long delay = entry.getValue().first();
            BlockPos blockPos = BlockPos.of(entry.getKey());
            BlockState state = level.getBlockState(blockPos);
            if (state.isAir() || delay <= 0) {
                this.level.destroyBlockProgress(
                        this.player.getId(),
                        blockPos,
                        -1
                );

                remove.add(entry.getKey());
                continue;
            }
            //tick delay
            entry.getValue().setFirst(delay - 1);
        }
        remove.forEach(it -> {
            visor$blockDamage.remove(it);
            BlockPos blockPos = BlockPos.of(it);
            visor$sendSwingDamageCleanUp(blockPos, false);
        });

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
    @Override
    public void visor$handleVrBlockDamage(BlockPos blockPos,
                                          Direction direction,
                                          int i, int j,
                                          ItemStack usedItem
    ) {
        if (!VRServerSettings.isBetterVrSwinging()){
            VisorAPI.server().getLogger().info("Received BlockSwingDamage " +
                    "packet while this feature is disabled!");
            return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVrPlayer(player);
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
            blockDamage = visor$getDestroyProgress(
                    blockState,
                    this.player,
                    this.player.level(),
                    blockPos, usedItem
            ) + savedDamage;
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
        visor$blockDamage.remove(blockPos.asLong());
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

}
