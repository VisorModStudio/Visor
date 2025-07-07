package me.phoenixra.visor.core.mixin.common.world.item;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"),
        method = "shootProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;FZFFF)V")
    private static Vec3 visor$vrShooterViewVector(LivingEntity livingEntity,
                                                 float v) {
        Vec3 viewVector = livingEntity.getViewVector(v);
        if (!(livingEntity instanceof ServerPlayer player)) {
            return viewVector;
        }
        VRServerPlayer serverPlayer = VisorAPI.server().getVrPlayer(player);
        if (serverPlayer == null || !serverPlayer.isVr()) {
            return viewVector;
        }
        return serverPlayer.getActiveHandDir();
    }
}
