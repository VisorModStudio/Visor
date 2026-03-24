package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.full.armor.VRArmorModelWithArms;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
    @Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
    private void visor$noHelmetInFirstPerson(
        CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) EquipmentSlot slot)
    {
        if (slot == EquipmentSlot.HEAD &&
            VRRenderState.isSelfModelRender(entity))
        {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;setPartVisibility(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/world/entity/EquipmentSlot;)V", shift = At.Shift.AFTER))
    private void visor$noArmsInFirstPerson(
        CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) EquipmentSlot slot,
        @Local(argsOnly = true) HumanoidModel model)
    {
        if (slot == EquipmentSlot.CHEST &&
            VRRenderState.isSelfModelRender(entity))
        {
            var mode = VRClientSettings.getModelArmsMode();

            // hide the arm armor, when not showing the arms in first person
            if (model instanceof VRArmorModelWithArms<?> armsModel) {
                // shoulders when not off
                armsModel.leftArm.visible &= mode != VRClientSettings.ModelArmsMode.OFF;
                armsModel.rightArm.visible &= mode != VRClientSettings.ModelArmsMode.OFF;

                // front only when complete
                armsModel.leftHand.visible &= mode == VRClientSettings.ModelArmsMode.COMPLETE;
                armsModel.rightHand.visible &= mode == VRClientSettings.ModelArmsMode.COMPLETE;
            } else {
                // front only when complete
                model.leftArm.visible &= mode == VRClientSettings.ModelArmsMode.COMPLETE;
                model.rightArm.visible &= mode == VRClientSettings.ModelArmsMode.COMPLETE;
            }
        }
    }
}
