// #!MC-VERSION:: 1.21.4
package org.vmstudio.visor.mixin.common.player;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.server.player.VRServerPlayer;

@Mixin(LivingEntity.class)
public abstract class Common_LivingEntityMixin extends Common_EntityMixin {


    @Shadow protected ItemStack useItem;

    @Shadow protected int useItemRemaining;

    @Shadow public abstract boolean isFallFlying();

    @Shadow public float zza;

    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Inject(at = @At("HEAD"), method = "spawnItemParticles", cancellable = true)
    protected void visor$spawnVRItemParticles(ItemStack itemStack,
                                              int count,
                                              CallbackInfo ci){}

    // 1.21.5 replaced isDamageSourceBlocked with the blocks_attacks component and this hook
    @Inject(method = "isDamageSourceBlocked", at = @At("RETURN"), cancellable = true)
    private void visor$poseShieldBlock(DamageSource damageSource,
                                       CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerExtension serverPlayer
                && serverPlayer.visor$poseBlocks(damageSource, cir.getReturnValueZ())) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method = "hurtServer", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void visor$vrHurtKnockbackDirection(LivingEntity instance, double strength, double x, double z,
                                                Operation<Void> original,
                                                @Local(argsOnly = true) DamageSource damageSource) {
        visor$vrKnockback(instance, strength, x, z, original, damageSource);
    }


    @Inject(method = "isLookingAtMe(Lnet/minecraft/world/entity/LivingEntity;DZZ[D)Z", at = @At("HEAD"), cancellable = true)
    private void visor$vrLookingAtMe(LivingEntity observer, double tolerance, boolean scaleByDistance,
                                     boolean visualShape, double[] yValues,
                                     CallbackInfoReturnable<Boolean> cir) {
        if (!(observer instanceof ServerPlayer serverPlayer)) {
            return;
        }
        VRServerPlayer vrPlayer = VisorAPI.server().getVRPlayer(serverPlayer);
        if (vrPlayer == null) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        var hmd = vrPlayer.getPoseData().getHmd();
        Vec3 eye = hmd.getPositionVec3();
        Vec3 look = hmd.getDirectionVec3().normalize();
        ClipContext.Block block = visualShape ? ClipContext.Block.VISUAL : ClipContext.Block.COLLIDER;

        for (double y : yValues) {
            Vec3 toSelf = new Vec3(self.getX() - eye.x, y - eye.y, self.getZ() - eye.z);
            double distance = toSelf.length();
            double dot = look.dot(toSelf.normalize());
            if (dot > 1.0 - (scaleByDistance ? tolerance / distance : tolerance)
                    && visor$hmdSees(self, serverPlayer, eye, y, block)) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }

    @Unique
    private static boolean visor$hmdSees(LivingEntity self, ServerPlayer viewer, Vec3 eye,
                                         double targetY, ClipContext.Block block) {
        if (viewer.level() != self.level()) {
            return false;
        }
        Vec3 target = new Vec3(self.getX(), targetY, self.getZ());
        if (target.distanceTo(eye) > 128.0) {
            return false;
        }
        return self.level()
                .clip(new ClipContext(eye, target, block, ClipContext.Fluid.NONE, viewer))
                .getType() == HitResult.Type.MISS;
    }

    @Unique
    private static void visor$vrKnockback(LivingEntity instance, double strength, double x, double z,
                                          Operation<Void> original, DamageSource damageSource) {
        Vec3 knockBack = CommonUtils.calcVRKnockback(damageSource.getEntity(), instance);
        if (knockBack != null) {
            x = knockBack.x;
            z = knockBack.z;
        }
        original.call(instance, strength, x, z);
    }
}
