package me.phoenixra.visor.mixin.common.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @WrapOperation(method = "shootProjectile", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getViewVector(F)Lnet/minecraft/world/phys/Vec3;"))
    private static Vec3 visor$vrAim(LivingEntity instance, float partialTicks, Operation<Vec3> original) {
        if (instance instanceof ServerPlayer player) {
            VRServerPlayer vrPlayer = VisorAPI.server().getVrPlayer(player);
            if (vrPlayer == null || !vrPlayer.isVRActive()) {
                return original.call(instance, partialTicks);
            }
            return vrPlayer.getPoseData()
                    .getActiveHand()
                    .getDirectionVec3();
        }
        return original.call(instance, partialTicks);
    }

}
