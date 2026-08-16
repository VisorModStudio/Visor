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
                Entity entity, CallbackInfoReturnable cir)
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
                // 1.21.1: getModelName() removed; PlayerSkin.Model#id() is "slim"/"default"
                String modelName = player.getSkin().model().id();
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
                    source = ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getHmd().getPositionVec3();
                }
                Vec3 direction = entity.position()
                        .add(0.0D, entity.getBbHeight() * scale + offset, 0.0D)
                        .subtract(source).normalize();

                // Vec3.normalize() returns ZERO below 1e-5, which would make the asin below
                // NaN and poison the whole quaternion - every billboard using it disappears.
                if (direction.lengthSqr() < 1.0E-6D) {
                    return this.camera.rotation();
                }

                // Must match Camera#setRotation's convention: rotationYXZ(PI - yaw, -pitch, 0),
                // with FORWARDS = (0,0,-1). A billboard's front is +Z in model space, so the
                // quaternion has to map -Z onto the view direction. Without the PI term this
                // look-at is exactly camera.rotation() * rotY(180) - it turns every
                // cameraOrientation() billboard away from the eye, and the culling render types
                // (entityCutout for the fishing bobber, itemEntityTranslucentCull for xp orbs,
                // text for name tags) then discard it entirely.
                // MC 1.20.5 flipped this convention: 1.20.1 had FORWARDS = (0,0,+1) and
                // rotationYXZ(-yaw, +pitch, 0), which is what the old form was written against.
                return new Quaternionf()
                        .rotateY((float) (Math.PI - Math.atan2(-direction.x, direction.z)))
                        .rotateX((float) Math.asin(direction.y));
            }
        }


    }


}
