package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

@Mixin(value = PlayerItemInHandLayer.class, priority = 900)
public class PlayerItemInHandLayerMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void vivecraft$noItemInGui(
            CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) HumanoidArm arm,
            @Local(argsOnly = true) ItemStack itemStack)
    {
        if (VRRenderState.isSelfModelRender(entity)) {
            boolean leftHanded = ClientContext.localPlayer.isLeftHanded();
            if(ClientContext.decorationRenderer
                    .getHandRenderState(HandType.fromMcArm(arm, leftHanded)) != HandRenderState.WORLD_HAND){
                ci.cancel();
            }
        }
    }
    @ModifyExpressionValue(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean visor$noSpyglass(
        boolean isSpyglass, @Local(argsOnly = true) LivingEntity livingEntity)
    {
        return isSpyglass && !VRRenderState.isSelfModelRender(livingEntity);
    }
}
