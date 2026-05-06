package org.vmstudio.visor.mixin.client.renderer.entity.player;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.entity.EntityRenderDispatcherExtension;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;

public class PlayerRenderMixins {
    @Mixin(EntityRenderDispatcher.class)
    public abstract static class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherExtension {

        @Shadow
        public Camera camera;

        @Inject(method = "cameraOrientation", at = @At("HEAD"), cancellable = true)
        private void visor$cameraOrientation(CallbackInfoReturnable<Quaternionf> cir) {
            if (VRRenderState.getPhase().isVRWorld()) {
                cir.setReturnValue(this.visor$getCameraOrientationOffset(0.5F, 0.0F));
            }
        }

        @Inject(method = "distanceToSqr*", at = @At("HEAD"), cancellable = true)
        private void visor$checkCameraNull(CallbackInfoReturnable<Double> cir) {
            if (this.camera == null) {
                cir.setReturnValue(0.0D);
            }
        }

        @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
        private void visor$getVRPlayerRenderer(
                Entity entity, CallbackInfoReturnable<EntityRenderer<AbstractClientPlayer>> cir)
        {
            if(ClientContext.visor == null) {
                return;
            }

            if (entity instanceof AbstractClientPlayer player)
            {
                var vrPlayer = VRClientPlayers.getPlayer(player);
                if(vrPlayer == null){
                    return;
                }
                String modelName = player.getModelName();
                var model = vrPlayer.getBodyType().getRenderer().getModelRenderer(
                        vrPlayer, modelName
                );
                if(model != null) {
                    cir.setReturnValue(model);
                }
            }
        }


        @Inject(method = "onResourceManagerReload", at = @At(value = "HEAD"))
        private void visor$clearVRPlayerRenderer(CallbackInfo ci) {
            if(ClientContext.visor == null) {
                return;
            }
            ClientContext.decorationRenderer.getVrBodyTypeRegistry().getAllComponents().forEach(
                    it-> it.getRenderer().clearModels()
            );

        }

        @Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"))
        private void visor$reloadVRPlayerRenderer(CallbackInfo ci, @Local EntityRendererProvider.Context context) {
            if(ClientContext.visor == null) {
                VisorState.setDelayedVrBodyInit(context);
                return;
            }
            ClientContext.decorationRenderer.getVrBodyTypeRegistry().getAllComponents().forEach(
                    it-> it.getRenderer().initModels(context)
            );

        }


        @Override
        @Unique
        public Quaternionf visor$getCameraOrientationOffset(float scale, float offset) {
            Entity entity = ((LevelRendererExtension) Minecraft.getInstance().levelRenderer).visor$getRenderedEntity();
            if (entity == null) {
                return this.camera.rotation();
            } else {
                Vec3 source;
                if (VRRenderState.getRenderPass().isThirdPerson()) {
                    source = this.camera.getPosition();
                } else {
                    source = VRClientPlayers.getLocalPlayer().getPoseData(PlayerPoseType.TICK).getHmd().getPositionVec3();
                }
                Vec3 direction = entity.position()
                        .add(0.0D, entity.getBbHeight() * scale + offset, 0.0D)
                        .subtract(source).normalize();

                return new Quaternionf()
                        .rotateY((float) -Math.atan2(-direction.x, direction.z))
                        .rotateX((float) -Math.asin(direction.y / direction.length()));
            }
        }


    }


}
