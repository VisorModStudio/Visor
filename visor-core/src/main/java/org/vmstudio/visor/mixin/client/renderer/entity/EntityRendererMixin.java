// #!MC-VERSION:: 1.21.10+
package org.vmstudio.visor.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Unique
    private static final String SUBMIT_NAME_TAG =
            "submitNameTag(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
                    + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                    + "Lnet/minecraft/client/renderer/state/CameraRenderState;)V";

    // 1.21.9 dropped EntityRenderDispatcher.cameraOrientation; the billboard basis is read from the
    // CameraRenderState the tag is submitted with, so the VR look-at goes in as a per-entity copy
    @WrapOperation(method = SUBMIT_NAME_TAG,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/CameraRenderState;)V"))
    private void visor$vrNameTagCameraOrient(SubmitNodeCollector collector, PoseStack poseStack, Vec3 pos,
                                             int light, Component text, boolean discrete, int background,
                                             double distance, CameraRenderState camera, Operation<Void> original,
                                             @Local(argsOnly = true) EntityRenderState state) {
        original.call(collector, poseStack, pos, light, text, discrete, background, distance,
                visor$nameTagCamera(state, camera));
    }

    @Inject(method = SUBMIT_NAME_TAG, at = @At("HEAD"), cancellable = true)
    private void visor$hideSpectatedVRNameTag(CallbackInfo ci, @Local(argsOnly = true) EntityRenderState state) {
        if (VRRenderState.isSpectatedVRView(VRPlayerRenderState.playerOf(state))) {
            ci.cancel();
        }
    }

    @Unique
    private static CameraRenderState visor$nameTagCamera(EntityRenderState state, CameraRenderState camera) {
        if (VRRenderState.getPhase().isNotVRWorld()) {
            return camera;
        }
        float heightScale = 1.0F;
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        VRClientPlayer vrPlayer = player == null ? null : VRClientPlayers.getPlayer(player);
        if (vrPlayer != null) {
            heightScale = vrPlayer.getModelScale();
        }

        Vec3 source = VRRenderState.getRenderPass().isThirdPerson()
                ? camera.pos
                : ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK).getHmd().getPositionVec3();
        Vec3 target = new Vec3(state.x,
                state.y + state.boundingBoxHeight * heightScale + 0.5F * heightScale,
                state.z);
        Vec3 dir = target.subtract(source).normalize();

        float yaw = (float) Math.atan2(dir.x, dir.z);
        float pitch = (float) -Math.asin(dir.y);

        CameraRenderState vrCamera = new CameraRenderState();
        vrCamera.blockPos = camera.blockPos;
        vrCamera.pos = camera.pos;
        vrCamera.entityPos = camera.entityPos;
        vrCamera.initialized = camera.initialized;
        vrCamera.orientation = new Quaternionf().rotationYXZ(yaw + (float) Math.PI, -pitch, 0F);
        return vrCamera;
    }
}
