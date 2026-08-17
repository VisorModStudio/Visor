package org.vmstudio.visor.mixin.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.player.VRClientPlayers;


@Mixin(ArmedEntityRenderState.class)
public class ArmedEntityRenderStateMixin {

    @Redirect(
            method = "extractArmedEntityRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getMainArm()Lnet/minecraft/world/entity/HumanoidArm;"))
    private static HumanoidArm visor$vrMainArm(LivingEntity entity) {
        HumanoidArm vrArm = visor$vrMainArmOrNull(entity);
        return vrArm != null ? vrArm : entity.getMainArm();
    }

    @Redirect(
            method = "extractArmedEntityRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getItemHeldByArm(Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack visor$vrItemHeldByArm(LivingEntity entity, HumanoidArm arm) {
        HumanoidArm vrArm = visor$vrMainArmOrNull(entity);
        if (vrArm == null) {
            return entity.getItemHeldByArm(arm);
        }
        return arm == vrArm ? entity.getMainHandItem() : entity.getOffhandItem();
    }

    @Unique
    @Nullable
    private static HumanoidArm visor$vrMainArmOrNull(LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return null;
        }
        VRClientPlayer vrPlayer = VRClientPlayers.getPlayer(player);
        if (vrPlayer == null) {
            return null;
        }
        return vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }
}
