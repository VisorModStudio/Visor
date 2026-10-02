package org.vmstudio.visor.mixin.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.Minecraft;
//? if <26.2 {
/*import net.minecraft.client.renderer.MultiBufferSource;
*///?}
//? if >=1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
//?}
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


    //? if >=1.21.9 {
    @Shadow
    public abstract void renderItem(LivingEntity livingEntity,
                                    ItemStack itemStack,
                                    ItemDisplayContext itemDisplayContext,
                                    PoseStack poseStack,
                                    SubmitNodeCollector submitNodeCollector,
                                    int i);
    //?} elif >=1.21.5 {
    /*@Shadow
    public abstract void renderItem(LivingEntity livingEntity,
                                    ItemStack itemStack,
                                    ItemDisplayContext itemDisplayContext,
                                    PoseStack poseStack,
                                    MultiBufferSource multiBufferSource,
                                    int i);
    *///?} else {
    /*@Shadow
    public abstract void renderItem(LivingEntity livingEntity,
                                    ItemStack itemStack,
                                    ItemDisplayContext itemDisplayContext,
                                    boolean bl,
                                    PoseStack poseStack,
                                    MultiBufferSource multiBufferSource,
                                    int i);
    *///?}

    //? if >=1.21.9 {
    @Shadow
    protected abstract void renderMap(PoseStack pMatrixStack,
                                      SubmitNodeCollector pCollector,
                                      int pCombinedLight,
                                      ItemStack pStack);
    //?} else {
    /*@Shadow
    protected abstract void renderMap(PoseStack pMatrixStack,
                                      MultiBufferSource pBuffer,
                                      int pCombinedLight,
                                      ItemStack pStack);
    *///?}


    // CallbackInfo only: 1.21.9 swapped the buffer source for a SubmitNodeCollector
    //? if >=26.2 {
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    //?} else {
    /*@Inject(method = "renderHandsWithItems", at = @At("HEAD"), cancellable = true)
    *///?}
    private void visor$noFirstPersonHandsInVR(CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Override
    public void visor$renderMap(PoseStack poseStack,
                                //? if >=26.2 {
                                SubmitNodeCollector bufferSource,
                                //?} else {
                                /*MultiBufferSource bufferSource,
                                *///?}
                                int pCombinedLight,
                                ItemStack itemStack) {
        //? if >=1.21.9 && <26.2 {
        /*// 1.21.9 submits instead of drawing; the caller's buffer source has no equivalent
        renderMap(poseStack, Minecraft.getInstance().gameRenderer.getSubmitNodeStorage(), pCombinedLight, itemStack);
        *///?} else {
        renderMap(poseStack, bufferSource, pCombinedLight, itemStack);
        //?}
    }

    @Unique
    public float visor$getEquipProgress(InteractionHand hand, float partialTicks) {
        return hand == InteractionHand.MAIN_HAND
                ? 1.0F - (this.oMainHandHeight + (this.mainHandHeight - this.oMainHandHeight) * partialTicks)
                : 1.0F - (this.oOffHandHeight + (this.offHandHeight - this.oOffHandHeight) * partialTicks);
    }

}
