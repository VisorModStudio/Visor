package org.vmstudio.visor.mixin.client.renderer.entity.player;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.player.model.VRPlayerModel;
import org.vmstudio.visor.core.client.settings.VRClientSettings;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerMixin extends RenderLayer {

    public ItemInHandLayerMixin(RenderLayerParent renderer) {
        super(renderer);
    }

    @ModifyVariable(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At("STORE"), ordinal = 0)
    private boolean visor$isRightMainHand(boolean isRightMainHand, @Local(argsOnly = true) LivingEntity entity) {
        if (this.getParentModel() instanceof VRPlayerModel) {
            var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
            if(vrPlayer != null){
                return !vrPlayer.isLeftHanded();
            }
            return true;
        } else {
            return isRightMainHand;
        }
    }


    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ArmedModel;translateToHand(Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;)V", shift = At.Shift.AFTER))
    private void visor$firstPersonItemScale(
        CallbackInfo ci, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) PoseStack poseStack)
    {
        if (VRRenderState.isVRBodyLocalRender(entity)) {
            // make the item scale equal in all directions
            poseStack.translate(0.0F, 0.65F, 0.0F);
            poseStack.scale(1F, VRClientSettings.getPlayerModelArmsScale(), 1f);
            poseStack.translate(0.0F, -0.65F, 0.0F);
        }
    }
}
