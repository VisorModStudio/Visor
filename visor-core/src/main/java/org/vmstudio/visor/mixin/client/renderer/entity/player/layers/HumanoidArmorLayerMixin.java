package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.render.VRRenderState;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    @Unique
    private HumanoidRenderState visor$currentRenderState;

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At("HEAD"))
    private void visor$storeRenderState(CallbackInfo ci,
                                        @Local(argsOnly = true) HumanoidRenderState renderState)
    {
        this.visor$currentRenderState = renderState;
    }

    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void visor$noHelmetInFirstPerson(CallbackInfo ci,
                                             @Local(argsOnly = true) EquipmentSlot slot)
    {
        if (slot == EquipmentSlot.HEAD
                && this.visor$currentRenderState != null
                && VRRenderState.isSelfOrSpectatedVRView(this.visor$currentRenderState)) {
            ci.cancel();
        }
    }
}
