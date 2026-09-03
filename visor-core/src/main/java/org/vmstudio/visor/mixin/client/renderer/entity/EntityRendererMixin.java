package org.vmstudio.visor.mixin.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.entity.EntityRenderDispatcherExtension;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"), method = "renderNameTag")
    public Quaternionf visor$vrNameTagCameraOrient(EntityRenderDispatcher instance, Entity entity) {
        float heightScale = 1.0f;
        VRClientPlayer vrPlayer = VRClientPlayers.getPlayer(entity);
        if (vrPlayer != null) {
            heightScale = vrPlayer.getModelScale();
        }
        return ((EntityRenderDispatcherExtension) this.entityRenderDispatcher)
                .visor$lookAtCameraOrientation(heightScale, 0.5f * heightScale);
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void visor$hideSpectatedVRNameTag(Entity entity, Component displayName,
                                                PoseStack poseStack, MultiBufferSource buffer,
                                                int packedLight, CallbackInfo ci) {
        if (VRRenderState.isSpectatedVRView(entity)) {
            ci.cancel();
        }
    }
}