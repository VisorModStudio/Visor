package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.vertex.*;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseClient;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRCameraType;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import org.vmstudio.visor.core.client.ClientContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class RenderPoseHelper {

    private RenderPoseHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }



    public static void applyCameraPose(VRCameraType cameraType,
                                       PoseStack poseStack){
        applyCameraOrientation(cameraType, poseStack);
        applyCameraTranslation(cameraType, poseStack);
    }

    public static void applyCameraOrientation(VRCameraType cameraType,
                                              PoseStack poseStack) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        final Matrix4f rotationMatrix;

        boolean smooth = cameraType == VRCameraType.FIRST_PERSON && mirrorSmooth > 0f;
        if (smooth) {

            // average rotation over history
            rotationMatrix = new Matrix4f()
                    .rotation(
                            ClientContext.rawPoseHandler
                                    .getHmdData()
                                    .getRotationHistory()
                                    .averageRotation(mirrorSmooth)
                    );
        } else {
            // direct VR eye/head rotation
            rotationMatrix = renderPose
                    .getCameraPose(cameraType)
                    .getRotation()
                    .transpose(new Matrix4f());
        }

        // apply to both pos & normal
        poseStack.last().pose().mul(rotationMatrix);
        poseStack.last().normal().mul(new Matrix3f(rotationMatrix));
    }

    public static void applyCameraTranslation(VRCameraType cameraType,
                                              PoseStack poseStack) {
        if (!cameraType.isEye()) {
            return;
        }
        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var eyePos = renderPose.getCameraPose(cameraType).getPosition();
        var hmdOrigin = renderPose.getHmd().getPosition();
        var offset = eyePos.sub(hmdOrigin, new Vector3f());

        poseStack.translate(-offset.x, -offset.y, -offset.z);
    }



    public static void applyHandPose(HandType hand,
                                     PoseStack poseStack) {
        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);

        var handPose = renderPose.getHand(hand);
        // move origin to hand pos relative to camera
        var handPos = handPose.getPosition();

        var cameraPos = getCameraPosition(VRRenderState.getCameraType(), renderPose);
        var relative = handPos.sub(cameraPos, new Vector3f());
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


    public static Vector3fc getCameraPosition(VRCameraType cameraTye,
                                              PlayerPoseClient vrPose) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        boolean smooth = cameraTye == VRCameraType.FIRST_PERSON && mirrorSmooth > 0f;
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

        return vrPose.getCameraPose(cameraTye).getPosition();
    }




    public static Vector3fc getHandPosition(HandType hand) {
        return ClientContext
                .localPlayer
                .getPoseData(PlayerPoseType.RENDER)
                .getHand(hand)
                .getPosition();
    }





}
