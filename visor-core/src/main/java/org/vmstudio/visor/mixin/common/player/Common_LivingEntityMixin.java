package org.vmstudio.visor.mixin.common.player;
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
}
