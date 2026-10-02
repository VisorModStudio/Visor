// #!MC-VERSION:: 1.20.1-1.21.1
package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Inject(
            method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void visor$hideHeadArmorOnVRSelf(
            CallbackInfo ci,
            @Local(argsOnly = true) LivingEntity entity,
            @Local(argsOnly = true) EquipmentSlot slot)
    {
        if (visor$hidesPiece(entity, slot)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean visor$hidesPiece(@Nullable LivingEntity entity, EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                && entity != null
                && (VRRenderState.isSelfModelRender(entity)
                || VRRenderState.isSpectatedVRView(entity));
    }
}
