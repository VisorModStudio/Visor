package me.phoenixra.visor.core.mixin.common.player;

import com.mojang.authlib.GameProfile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.server.VRServerSettings;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import me.phoenixra.visor.core.common.network.server.ServerNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow
    @Final
    public MinecraftServer server;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;tick()V", shift = Shift.AFTER), method = "doTick()V")
    public void visor$tick(CallbackInfo info) {
        ServerNetworking.updatePlayerPose(
                (ServerPlayer) (Object) this
        );
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z", shift = Shift.BEFORE), method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
        locals = LocalCapture.CAPTURE_FAILHARD)
    public void visor$vrItemDrop(ItemStack itemStack,
                                boolean dropAround,
                                boolean includeName,
                                CallbackInfoReturnable<ItemEntity> info,
                                ItemEntity itemEntity) {
        VRServerPlayer serverPlayer = visor$getVrPlayer();
        if (serverPlayer == null
                || !serverPlayer.isVr()
                || dropAround) {
            return;
        }

        Vec3 handDir = serverPlayer.getControllerDir(
                ControllerHand.MAIN
        ).scale(0.3F);
        Vec3 handPos = serverPlayer.getControllerPos(
                ControllerHand.MAIN
        );
        itemEntity.setDeltaMovement(
                handDir.x,
                handDir.y,
                handDir.z
        );
        itemEntity.setPos(
                handPos.x() + itemEntity.getDeltaMovement().x(),
                handPos.y() + itemEntity.getDeltaMovement().y(),
                handPos.z() + itemEntity.getDeltaMovement().z()
        );
    }

    @Inject(at = @At("HEAD"), method = "hurt", cancellable = true)
    public void visor$canGetHurtByPlayer(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = damageSource.getEntity();
        ServerPlayer damager = null;

        if (entity instanceof ServerPlayer) {
            damager = (ServerPlayer) entity;
        } else if ((entity instanceof AbstractArrow && (((AbstractArrow) entity).getOwner() instanceof ServerPlayer))) {
            damager = (ServerPlayer) ((AbstractArrow) entity).getOwner();
        }
        if(damager == null){
            return;
        }

        VRServerPlayer damagerPlayer = VisorAPI.server().getVrPlayer(damager);
        VRServerPlayer thisPlayer = visor$getVrPlayer();
        boolean victimHasVR;
        boolean damagerHasVR;

        damagerHasVR = damagerPlayer != null
                && damagerPlayer.isVr();
        victimHasVR = thisPlayer != null
                && thisPlayer.isVr();

        boolean blockedDamage = false;
        String blockedDamageCase = "";

        if (!VRServerSettings.isPvpVRvsVR()
                && damagerHasVR && victimHasVR) {
            blockedDamage = true;
            blockedDamageCase = "Server: cancelled VR vs VR player damage";

        } else if (!VRServerSettings.isPvpVRvsVanilla()
                && ((!damagerHasVR && victimHasVR) || (damagerHasVR && !victimHasVR))) {
            blockedDamage = true;
            blockedDamageCase = "Server: cancelled NonVR vs VR player damage";
        }

        if(!blockedDamage) return;

        if(VRServerSettings.isNotifyPvpBlocked()) {
            damager.sendSystemMessage(Component.literal(blockedDamageCase));
        }
        cir.setReturnValue(false);
    }

    /**
     * Particles..
     */
    @Override
    public void sweepAttack() {
        VRServerPlayer vrServerPlayer = visor$getVrPlayer();

        if (vrServerPlayer != null && vrServerPlayer.isVr()) {
            Vec3 handDir = vrServerPlayer.getControllerDir(ControllerHand.MAIN);
            Vec3 handPos = vrServerPlayer.getControllerPos(ControllerHand.MAIN);


            float handAngle = (float) Math.toDegrees(Mth.atan2(handDir.x, -handDir.z));
            double offsetX = -Mth.sin(handAngle * ((float) Math.PI / 180F));
            double offsetZ = Mth.cos(handAngle * ((float) Math.PI / 180F));

            if (this.level() instanceof ServerLevel) {
                ((ServerLevel) this.level()).sendParticles(
                        ParticleTypes.SWEEP_ATTACK,
                        handPos.x + offsetX,
                        handPos.y,
                        handPos.z + offsetZ,
                        0,
                        offsetX, 0.0D, offsetZ,
                        0.0D
                );
            }
        } else {
            super.sweepAttack();
        }
    }

    @Unique
    private VRServerPlayer visor$getVrPlayer() {
        return VisorAPI.server().getVrPlayer((ServerPlayer) (Object) this);
    }
}
