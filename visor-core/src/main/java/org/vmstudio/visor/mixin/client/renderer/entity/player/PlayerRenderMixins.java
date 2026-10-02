// #!MC-VERSION:: 1.21.10+
package org.vmstudio.visor.mixin.client.renderer.entity.player;

import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;
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

        // 1.21.9 moved the camera basis onto CameraRenderState (GameRenderer.extractCamera) and
        // dropped distanceToSqr, the distance now lives in EntityRenderState.distanceToCameraSq

        @Inject(method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
        private void visor$swapInVRBodyRenderer(Entity entity, CallbackInfoReturnable<Object> cir) {
            visor$useVRBodyRenderer(entity, cir);
        }

        @Inject(method = "getRenderer(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", at = @At("HEAD"), cancellable = true)
        private void visor$swapInVRBodyRendererForState(EntityRenderState state, CallbackInfoReturnable<Object> cir) {
            if (state instanceof VRPlayerRenderState vrState) {
                visor$useVRBodyRenderer(vrState.player, cir);
            }
        }

        @Unique
        private static void visor$useVRBodyRenderer(Entity entity, CallbackInfoReturnable<Object> cir) {
            if(ClientContext.visor == null) {
                return;
            }

            if (entity instanceof AbstractClientPlayer player)
            {
                var vrPlayer = VRClientPlayers.getPlayer(player);
                if(vrPlayer == null){
                    return;
                }
                String modelName = McRenderUtils.getModelName(player);
                var model = vrPlayer.getBodyType().getRenderer().getModelRenderer(
                        vrPlayer, modelName
                );
                if(model != null) {
                    cir.setReturnValue(model);
                }
            }
        }


        @Inject(method = "onResourceManagerReload", at = @At(value = "HEAD"))
        private void visor$dropVRBodyModels(CallbackInfo ci) {
            if(ClientContext.visor == null) {
                return;
            }
            ClientContext.decorationRenderer.getVrBodyTypeRegistry().getAllComponents().forEach(
                    it-> it.getRenderer().clearModels()
            );

        }

        //keep @Local without variable name, to search by type
        @Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createAvatarRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"))
        private void visor$rebuildVRBodyModels(CallbackInfo ci, @Local EntityRendererProvider.Context context) {
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
        public Quaternionf visor$lookAtCameraOrientation(float heightFraction, float yOffset) {
            Entity entity = LevelRendererExtension.get().visor$getCurrentRenderEntity();
            if (entity == null) {
                return this.camera.rotation();
            }
            Vec3 source = VRRenderState.getRenderPass().isThirdPerson()
                    ? McVersionClientUtils.cameraPosition(this.camera)
                    : ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getHmd().getPositionVec3();

            Vec3 target = entity.position()
                    .add(0.0D, entity.getBbHeight() * heightFraction + yOffset, 0.0D);
            Vec3 dir = target.subtract(source).normalize();

            float yaw = (float) Math.atan2(dir.x, dir.z);
            float pitch = (float) -Math.asin(dir.y);
            return new Quaternionf().rotationYXZ(yaw + (float) Math.PI, -pitch, 0F);
        }


    }


}
