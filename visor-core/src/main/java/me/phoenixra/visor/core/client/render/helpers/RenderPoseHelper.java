package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43C;

import java.util.Comparator;
import java.util.Optional;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

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

        PoseDataImpl renderPose = ClientContext.player.getPose(PoseType.RENDER);
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
                    .getRotationMatrix()
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
        PoseDataImpl renderPose = ClientContext.player.getPose(PoseType.RENDER);
        Vec3 eyePos = renderPose.getElementForDisplay(vrDisplay).getPosition();
        Vec3 hmdOrigin = renderPose.getHmd().getPosition();
        Vec3 offset = eyePos.subtract(hmdOrigin);

        poseStack.translate(-offset.x, -offset.y, -offset.z);
    }



    public static void applyControllerPose(ControllerHand hand,
                                           PoseStack poseStack) {
        PoseDataImpl renderPose = ClientContext.player.getPose(PoseType.RENDER);

        // move origin to controller pos relative to camera
        Vec3 controllerPos = getControllerPosition(hand);
        Vec3 cameraPos    = getCameraPosition(VRRenderState.getCurrentVRDisplay(), renderPose);
        Vec3 relative     = controllerPos.subtract(cameraPos);
        poseStack.translate(relative.x, relative.y, relative.z);

        // apply controller’s inverse rotation
        Matrix4f invRot = renderPose
                .getController(hand)
                .getRotationMatrix()
                .invert(new Matrix4f())
                .transpose(new Matrix4f());
        poseStack.last().pose().mul(invRot);

        // scale to world scale
        float s = renderPose.getWorldScale();
        poseStack.scale(s, s, s);
    }


    public static Vec3 getCameraPosition(VRDisplay vrDisplay,
                                         PoseData vrPose) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        boolean smooth = vrDisplay == VRDisplay.FIRST_PERSON && mirrorSmooth > 0f;
        if (smooth) {
            Vec3 avg = ClientContext.rawPoseHandler
                    .getHmdData()
                    .getPositionHistory()
                    .averagePosition(mirrorSmooth);

            return avg
                    .scale(vrPose.getWorldScale())
                    .yRot(vrPose.getRotationY())
                    .add(vrPose.getOrigin());
        }

        return vrPose.getElementForDisplay(vrDisplay).getPosition();
    }




    public static Vec3 getControllerPosition(ControllerHand hand) {
        return ClientContext
                .player
                .getPose(PoseType.RENDER)
                .getController(hand)
                .getPosition();
    }





}
