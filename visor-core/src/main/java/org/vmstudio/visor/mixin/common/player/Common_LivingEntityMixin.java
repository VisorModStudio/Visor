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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;

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

    @Inject(method = "isDamageSourceBlocked", at = @At("RETURN"), cancellable = true)
    private void visor$poseShieldBlock(DamageSource damageSource,
                                       CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof ServerPlayerExtension serverPlayer
                && serverPlayer.visor$poseBlocks(damageSource, cir.getReturnValueZ())) {
            cir.setReturnValue(true);
        }
    }

    @WrapOperation(method = "hurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;knockback(DDD)V"))
    private void visor$vrHurtKnockbackDirection(LivingEntity instance, double strength, double x, double z,
                                                Operation<Void> original,
                                                @Local(argsOnly = true) DamageSource damageSource) {
        Vec3 knockBack = CommonUtils.calcVRKnockback(damageSource.getEntity(), instance);
        if (knockBack != null) {
            x = knockBack.x;
            z = knockBack.z;
        }
        original.call(instance, strength, x, z);
    }
}
