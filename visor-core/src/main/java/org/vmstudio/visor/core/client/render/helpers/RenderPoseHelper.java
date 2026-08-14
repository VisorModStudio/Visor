package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import org.vmstudio.visor.core.client.ClientContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderPoseHelper {

    private RenderPoseHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }



    public static void applyCameraPose(VRRenderPass renderPass,
                                       PoseStack poseStack){
        applyCameraOrientation(renderPass, poseStack);
        applyCameraTranslation(renderPass, poseStack);
    }

    public static void applyCameraOrientation(VRRenderPass renderPass,
                                              PoseStack poseStack) {
        Matrix4f rotationMatrix = getViewRotation(renderPass);

        // apply to both blockPos & normal
        poseStack.last().pose().mul(rotationMatrix);
        poseStack.last().normal().mul(new Matrix3f(rotationMatrix));
    }


    public static Matrix4f getViewRotation(VRRenderPass renderPass) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        boolean smooth = renderPass == VRRenderPass.CENTER && mirrorSmooth > 0f;
        if (smooth) {
            // average rotation over history
            return new Matrix4f()
                    .rotation(
                            ClientContext.rawPoseHandler
                                    .getHmdData()
                                    .getRotationHistory()
                                    .averageRotation(mirrorSmooth)
                    );
        }
        // direct VR eye/head rotation
        return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                .getCameraPose(renderPass)
                .getRotation()
                .transpose(new Matrix4f());
    }

   private static final Vector3fc LEVEL_LIGHT_0 = new Vector3f(0.2f, 1.0f, -0.7f).normalize();
    private static final Vector3fc LEVEL_LIGHT_1 = new Vector3f(-0.2f, 1.0f, 0.7f).normalize();
    private static final Vector3fc NETHER_LEVEL_LIGHT_1 = new Vector3f(-0.2f, -1.0f, 0.7f).normalize();


    public static void setupEyeSpaceLevelLights(VRRenderPass renderPass) {
        boolean constantAmbient = MC.level != null
                && MC.level.effects().constantAmbientLight();
        Vector3fc light1 = constantAmbient ? NETHER_LEVEL_LIGHT_1 : LEVEL_LIGHT_1;

        Matrix4f view = getViewRotation(renderPass);
        RenderSystem.setShaderLights(
                view.transformDirection(LEVEL_LIGHT_0, new Vector3f()),
                view.transformDirection(light1, new Vector3f())
        );
    }


    public static void restoreLevelLights() {
        if (MC.level != null && MC.level.effects().constantAmbientLight()) {
            Lighting.setupNetherLevel();
        } else {
            Lighting.setupLevel();
        }
    }

    public static void applyCameraTranslation(VRRenderPass renderPass,
                                              PoseStack poseStack) {
        if (!renderPass.isEye()) {
            return;
        }
        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var eyePos = renderPose.getCameraPose(renderPass).getPosition();
        var hmdOrigin = renderPose.getHmd().getPosition();
        var offset = eyePos.sub(hmdOrigin, new Vector3f());

        poseStack.translate(-offset.x, -offset.y, -offset.z);
    }



    public static void applyHandPose(HandType hand,
                                     PoseStack poseStack) {
        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        Vector3fc cameraPos = getCameraPosition(VRRenderState.getRenderPass(), renderPose);
        applyHandPose(renderPose, hand, cameraPos, poseStack);
    }

    public static void applyHandPose(VRPlayerPoseClient renderPose,
                                     HandType hand,
                                     Vector3fc referencePos,
                                     PoseStack poseStack) {
        var handPose = renderPose.getBody().getHand(hand).getPose();
        // move origin to hand position relative to the reference origin
        var handPos = handPose.getPosition();

        var relative = handPos.sub(referencePos, new Vector3f());
        poseStack.translate(relative.x, relative.y, relative.z);

        // apply hand’s inverse rotation
        Matrix4f invRot = handPose
                .getRotation()
                .invert(new Matrix4f())
                .transpose(new Matrix4f());
        poseStack.last().pose().mul(invRot);

        // scale to world scale
        float s = renderPose.getWorldScale();
        poseStack.scale(s, s, s);
    }

    public static Vector3fc getCameraPosition(VRRenderPass renderPass,
                                              VRPlayerPoseClient vrPose) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        boolean smooth = renderPass == VRRenderPass.CENTER && mirrorSmooth > 0f;
        if (smooth) {
            var avg = ClientContext.rawPoseHandler
                    .getHmdData()
                    .getPositionHistory()
                    .averagePosition(mirrorSmooth);

            return avg
                    .mul(vrPose.getWorldScale())
                    .rotateY(vrPose.getRotationY())
                    .add(vrPose.getOrigin());
        }

        return vrPose.getCameraPose(renderPass).getPosition();
    }




    public static Vector3fc getHandPosition(HandType hand) {
        return ClientContext
                .localPlayer
                .getPoseData(PlayerPoseType.RENDER)
                .getHand(hand)
                .getPosition();
    }





}
