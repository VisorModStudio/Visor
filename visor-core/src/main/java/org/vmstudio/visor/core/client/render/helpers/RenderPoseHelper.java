package org.vmstudio.visor.core.client.render.helpers;

//? if >=1.20.5 {
import com.mojang.blaze3d.platform.Lighting;
//?}
import com.mojang.blaze3d.vertex.*;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
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

    // 1.20.5 renderLevel carries the view rotation as a plain frustum matrix
    public static void applyCameraOrientation(VRRenderPass renderPass,
                                              Matrix4f frustumMatrix) {
        frustumMatrix.mul(getViewRotation(renderPass));
    }

    // Lighting.DIFFUSE_LIGHT_0 / DIFFUSE_LIGHT_1 / NETHER_DIFFUSE_LIGHT_1, which are private
    private static final Vector3fc LEVEL_LIGHT_0 = new Vector3f(0.2F, 1.0F, -0.7F).normalize();
    private static final Vector3fc LEVEL_LIGHT_1 = new Vector3f(-0.2F, 1.0F, 0.7F).normalize();
    private static final Vector3fc NETHER_LEVEL_LIGHT_1 = new Vector3f(-0.2F, -1.0F, 0.7F).normalize();


    public static void setupEyeSpaceLevelLights(VRRenderPass renderPass) {
        Vector3fc light1 = isConstantAmbient() ? NETHER_LEVEL_LIGHT_1 : LEVEL_LIGHT_1;

        Matrix4f view = getViewRotation(renderPass);
        McGlState.setShaderLights(
                view.transformDirection(LEVEL_LIGHT_0, new Vector3f()),
                view.transformDirection(light1, new Vector3f())
        );
    }

    // before 1.20.5 vanilla keeps the level lights in view space, so the eye-space upload already is its state
    public static void restoreLevelLights() {
        //? if >=1.21.6 {
        // the LEVEL entry already holds what the level pass uploaded, nether included
        MC.gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
        //?} elif >=1.20.5 {
        /*if (isConstantAmbient()) {
            Lighting.setupNetherLevel();
        } else {
            Lighting.setupLevel();
        }
        *///?}
    }

    private static boolean isConstantAmbient() {
        //? if >=26.1 {
        return MC.level != null && MC.level.dimensionType().cardinalLightType()
                == net.minecraft.world.level.CardinalLighting.Type.NETHER;
        //?} elif >=1.21.11 {
        /*return MC.level != null && MC.level.dimensionType().cardinalLightType()
                == net.minecraft.world.level.dimension.DimensionType.CardinalLightType.NETHER;
        *///?} else {
        /*return MC.level != null && MC.level.effects().constantAmbientLight();
        *///?}
    }

    public static Matrix4f getViewRotation(VRRenderPass renderPass) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);

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
        return renderPose
                .getCameraPose(renderPass)
                .getRotation()
                .transpose(new Matrix4f());
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
        poseStack.last().normal().mul(new Matrix3f(invRot));

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
