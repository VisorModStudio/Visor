package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class RenderPoseHelper {

    private RenderPoseHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }



    public static void applyDisplayPose(VRDisplay vrDisplay,
                                        PoseStack poseStack){
        applyDisplayOrientation(vrDisplay, poseStack);
        applyDisplayTranslation(vrDisplay, poseStack);
    }

    public static void applyDisplayOrientation(VRDisplay vrDisplay,
                                               PoseStack poseStack) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        PoseDataImpl renderPose = ClientContext.player.getPose(PoseDataType.RENDER);
        final Matrix4f rotationMatrix;

        boolean smooth = vrDisplay == VRDisplay.FIRST_PERSON && mirrorSmooth > 0f;
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
                    .getElementForDisplay(vrDisplay)
                    .getRotation()
                    .transpose(new Matrix4f());
        }

        // apply to both pos & normal
        poseStack.last().pose().mul(rotationMatrix);
        poseStack.last().normal().mul(new Matrix3f(rotationMatrix));
    }

    public static void applyDisplayTranslation(VRDisplay vrDisplay,
                                               PoseStack poseStack) {
        if (!vrDisplay.isEye()) {
            return;
        }
        PoseDataImpl renderPose = ClientContext.player.getPose(PoseDataType.RENDER);
        var eyePos = renderPose.getElementForDisplay(vrDisplay).getPosition();
        var hmdOrigin = renderPose.getHmd().getPosition();
        var offset = eyePos.sub(hmdOrigin, new Vector3f());

        poseStack.translate(-offset.x, -offset.y, -offset.z);
    }



    public static void applyControllerPose(ControllerHand hand,
                                           PoseStack poseStack) {
        PoseDataImpl renderPose = ClientContext.player.getPose(PoseDataType.RENDER);

        // move origin to controller pos relative to camera
        var controllerPos = getControllerPosition(hand);
        var cameraPos = getCameraPosition(VRRenderState.getCurrentVRDisplay(), renderPose);
        var relative = controllerPos.sub(cameraPos, new Vector3f());
        poseStack.translate(relative.x, relative.y, relative.z);

        // apply controller’s inverse rotation
        Matrix4f invRot = renderPose
                .getController(hand)
                .getRotation()
                .invert(new Matrix4f())
                .transpose(new Matrix4f());
        poseStack.last().pose().mul(invRot);

        // scale to world scale
        float s = renderPose.getWorldScale();
        poseStack.scale(s, s, s);
    }


    public static Vector3fc getCameraPosition(VRDisplay vrDisplay,
                                              PoseData vrPose) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        boolean smooth = vrDisplay == VRDisplay.FIRST_PERSON && mirrorSmooth > 0f;
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

        return vrPose.getElementForDisplay(vrDisplay).getPosition();
    }




    public static Vector3fc getControllerPosition(ControllerHand hand) {
        return ClientContext
                .player
                .getPose(PoseDataType.RENDER)
                .getController(hand)
                .getPosition();
    }





}
