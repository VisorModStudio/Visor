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
//? if >=1.21.2 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.item.ItemStack;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;
//?} else {
/*import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
*///?}

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

    // 1.21.4 renamed the armor fields of the render state to *Equipment
    //? if >=1.21.9 {
    @ModifyExpressionValue(
            method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;headEquipment:Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack visor$hideHeadArmorOnVRSelf(ItemStack headItem, @Local(argsOnly = true) HumanoidRenderState state) {
        return visor$hidesPiece(VRPlayerRenderState.playerOf(state), EquipmentSlot.HEAD) ? ItemStack.EMPTY : headItem;
    }
    //?} elif >=1.21.4 {
    /*@ModifyExpressionValue(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;headEquipment:Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack visor$hideHeadArmorOnVRSelf(ItemStack headItem, @Local(argsOnly = true) HumanoidRenderState state) {
        return visor$hidesPiece(VRPlayerRenderState.playerOf(state), EquipmentSlot.HEAD) ? ItemStack.EMPTY : headItem;
    }
    *///?} elif >=1.21.2 {
    /*@ModifyExpressionValue(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;headItem:Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack visor$hideHeadArmorOnVRSelf(ItemStack headItem, @Local(argsOnly = true) HumanoidRenderState state) {
        return visor$hidesPiece(VRPlayerRenderState.playerOf(state), EquipmentSlot.HEAD) ? ItemStack.EMPTY : headItem;
    }
    *///?} else {
    /*@Inject(
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
    *///?}

    @Unique
    private static boolean visor$hidesPiece(@Nullable LivingEntity entity, EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                && entity != null
                && (VRRenderState.isSelfModelRender(entity)
                || VRRenderState.isSpectatedVRView(entity));
    }
}
