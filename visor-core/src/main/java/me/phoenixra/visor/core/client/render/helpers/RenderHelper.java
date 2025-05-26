package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import me.phoenixra.visor.core.client.ClientContext;

public class RenderHelper {


    public static void applyDisplayPose(VRDisplay pass, PoseStack poseStack) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        PoseDataImpl renderPose = ClientContext.player.getPose(PoseType.RENDER);
        final Matrix4f rotationMatrix;

        boolean mirrorMode = pass == VRDisplay.FIRST_PERSON && mirrorSmooth > 0f;
        if (mirrorMode) {
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
                    .getElementForDisplay(pass)
                    .getRotationMatrix()
                    .transpose(new Matrix4f());
        }

        // apply to both pos & normal
        poseStack.last().pose().mul(rotationMatrix);
        poseStack.last().normal().mul(new Matrix3f(rotationMatrix));
    }

    public static void applyDisplayTranslation(VRDisplay pass, PoseStack poseStack) {
        if (!pass.isEye()) {
            return;
        }
        PoseDataImpl renderPose = ClientContext.player.getPose(PoseType.RENDER);
        Vec3 eyePos = renderPose.getElementForDisplay(pass).getPosition();
        Vec3 hmdOrigin = renderPose.getHmd().getPosition();
        Vec3 offset = eyePos.subtract(hmdOrigin);

        poseStack.translate(-offset.x, -offset.y, -offset.z);
    }

    public static void applyControllerPose(ControllerHand hand, PoseStack poseStack) {
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

        // 3) scale to world scale
        float s = renderPose.getWorldScale();
        poseStack.scale(s, s, s);
    }


    public static Vec3 getCameraPosition(VRDisplay pass, PoseDataImpl vrPose) {
        float mirrorSmooth = VRClientSettings.getMirrorSmooth();

        boolean mirrorMode = pass == VRDisplay.FIRST_PERSON && mirrorSmooth > 0f;
        if (mirrorMode) {
            Vec3 avg = ClientContext.rawPoseHandler
                    .getHmdData()
                    .getPositionHistory()
                    .averagePosition(mirrorSmooth);
            // scale, rotate by yaw, then offset by origin
            return avg
                    .scale(vrPose.getWorldScale())
                    .yRot(vrPose.getRotationYaw())
                    .add(vrPose.getOrigin());
        }

        return vrPose.getElementForDisplay(pass).getPosition();
    }




    public static Vec3 getControllerPosition(ControllerHand hand) {
        return ClientContext
                .player
                .getPose(PoseType.RENDER)
                .getController(hand)
                .getPosition();
    }





    public static void renderBox(BufferBuilder buffer,
                                 Vec3 start,
                                 Vec3 end,
                                 float minX, float maxX,
                                 float minY, float maxY,
                                 Vec3i color,
                                 byte alpha,
                                 PoseStack poseStack) {
        // Compute local axes
        Vec3 forward = end.subtract(start).normalize();
        Vec3 upWorld = new Vec3(0, 1, 0);
        Vec3 right = forward.cross(upWorld).normalize();
        Vec3 up = right.cross(forward).normalize();

        // Scale axes for thickness
        Vec3 r0 = right.scale(minX);
        Vec3 r1 = right.scale(maxX);
        Vec3 u0 = up.scale(minY);
        Vec3 u1 = up.scale(maxY);

        // Precompute corners: each entry is {basePoint, x-offset, y-offset}
        Vec3[][] corners = new Vec3[][] {
                { start, r0, u0 }, { start, r1, u0 }, { start, r1, u1 }, { start, r0, u1 },
                { end,   r0, u0 }, { end,   r1, u0 }, { end,   r1, u1 }, { end,   r0, u1 }
        };

        Matrix4f mat = poseStack.last().pose();
        Vec3[] normals = new Vec3[] { forward, right, up };
        int[][] faceIndices = new int[][] {
                // back face (start)
                {0, 3, 2, 1},
                // front face (end)
                {4, 5, 6, 7},
                // right face
                {1, 2, 6, 5},
                // left face
                {0, 4, 7, 3},
                // top face
                {3, 7, 6, 2},
                // bottom face
                {0, 1, 5, 4}
        };
        Vec3[] faceNormals = new Vec3[] {
                forward, forward.scale(-1),
                right, right.scale(-1),
                up, up.scale(-1)
        };

        // Draw each face
        for (int f = 0; f < faceIndices.length; f++) {
            Vec3 normal = faceNormals[f].normalize();
            for (int idx : faceIndices[f]) {
                Vec3 base = corners[idx][0];
                Vec3 xOff = corners[idx][1];
                Vec3 yOff = corners[idx][2];
                Vec3 pos = base.add(xOff).add(yOff);
                addVertex(buffer, mat, pos, color, alpha, normal);
            }
        }
    }

    private static void addVertex(BufferBuilder buff,
                                  Matrix4f mat,
                                  Vec3 pos,
                                  Vec3i color,
                                  int alpha,
                                  Vec3 normal) {
        buff.vertex(mat, (float) pos.x, (float) pos.y, (float) pos.z)
                .color(color.getX(), color.getY(), color.getZ(), alpha)
                .normal((float) normal.x, (float) normal.y, (float) normal.z)
                .endVertex();
    }




}
