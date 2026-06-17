package org.vmstudio.visor.mixin.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;


@Mixin(value = ItemInHandRenderer.class, priority = 999)
public abstract class ItemInHandRendererMixin implements ItemInHandRendererExtension {

    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private float offHandHeight;


    @Shadow
    public abstract void renderItem(LivingEntity livingEntity,
                                    ItemStack itemStack,
                                    ItemDisplayContext itemDisplayContext,
                                    boolean bl,
                                    PoseStack poseStack,
                                    MultiBufferSource multiBufferSource,
                                    int i);

    @Shadow
    protected abstract void renderMap(PoseStack pMatrixStack,
                                      MultiBufferSource pBuffer,
                                      int pCombinedLight,
                                      ItemStack pStack);


    @Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void visor$noFirstPersonHandsInVR(float tickDelta,
                                              PoseStack poseStack,
                                              MultiBufferSource.BufferSource bufferSource,
                                              LocalPlayer player,
                                              int light,
                                              CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Override
    public void visor$renderMap(PoseStack poseStack,
                                MultiBufferSource bufferSource,
                                int pCombinedLight,
                                ItemStack itemStack) {
        renderMap(poseStack, bufferSource, pCombinedLight, itemStack);
    }

    @Unique
    public float visor$getEquipProgress(InteractionHand hand, float partialTicks) {
        return hand == InteractionHand.MAIN_HAND
                ? 1.0F - (this.oMainHandHeight + (this.mainHandHeight - this.oMainHandHeight) * partialTicks)
                : 1.0F - (this.oOffHandHeight + (this.offHandHeight - this.oOffHandHeight) * partialTicks);
    }

}
