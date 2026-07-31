package org.vmstudio.visor.mixin.common.player;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @ModifyExpressionValue(method = "isDamageSourceBlocked",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isBlocking()Z"))
    protected boolean visor$roomscaleShieldBlocking(boolean isBlocking,
                                                    @Local(argsOnly = true) DamageSource damageSource,
                                                    @Share("roomscaleBlocked") LocalBooleanRef roomscaleBlocked) {
        return isBlocking;
    }

    @ModifyReturnValue(method = "isDamageSourceBlocked", at = @At("RETURN"))
    private boolean visor$roomscaleShieldBlocked(boolean blocked,
                                                 @Share("roomscaleBlocked") LocalBooleanRef roomscaleBlocked) {
        return blocked || roomscaleBlocked.get();
    }
}
