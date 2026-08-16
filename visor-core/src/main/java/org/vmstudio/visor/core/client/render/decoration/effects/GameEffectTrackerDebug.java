package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.player.VRBodyPartType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VRPoseTrackers;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;

import java.util.EnumMap;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectTrackerDebug extends VRGameEffect {

    public static final String ID = "tracker_markers";

    private static boolean ENABLED = false;


    private static final double FRONT_DISTANCE = 2.0;
    private static final int REANCHOR_INTERVAL_TICKS = 200;

    private static final double AXIS_LENGTH = 0.15;
    private static final float AXIS_HALF_THICK = 0.012f;
    private static final float BONE_HALF_THICK = 0.009f;

    private static final Vector3fc AXIS_X = new Vector3f(1f, 0f, 0f);
    private static final Vector3fc AXIS_Y = new Vector3f(0f, 1f, 0f);
    private static final Vector3fc AXIS_Z = new Vector3f(0f, 0f, 1f);


    private static final EnumMap<VRBodyPartType, VRBodyPartType> BONE_PARENTS =
            new EnumMap<>(VRBodyPartType.class);

    static {
        BONE_PARENTS.put(VRBodyPartType.CHEST, VRBodyPartType.WAIST);

        BONE_PARENTS.put(VRBodyPartType.LEFT_SHOULDER, VRBodyPartType.CHEST);
        BONE_PARENTS.put(VRBodyPartType.LEFT_ELBOW, VRBodyPartType.LEFT_SHOULDER);
        BONE_PARENTS.put(VRBodyPartType.LEFT_WRIST, VRBodyPartType.LEFT_ELBOW);

        BONE_PARENTS.put(VRBodyPartType.RIGHT_SHOULDER, VRBodyPartType.CHEST);
        BONE_PARENTS.put(VRBodyPartType.RIGHT_ELBOW, VRBodyPartType.RIGHT_SHOULDER);
        BONE_PARENTS.put(VRBodyPartType.RIGHT_WRIST, VRBodyPartType.RIGHT_ELBOW);

        BONE_PARENTS.put(VRBodyPartType.LEFT_KNEE, VRBodyPartType.WAIST);
        BONE_PARENTS.put(VRBodyPartType.LEFT_ANKLE, VRBodyPartType.LEFT_KNEE);
        BONE_PARENTS.put(VRBodyPartType.LEFT_FOOT, VRBodyPartType.LEFT_ANKLE);

        BONE_PARENTS.put(VRBodyPartType.RIGHT_KNEE, VRBodyPartType.WAIST);
        BONE_PARENTS.put(VRBodyPartType.RIGHT_ANKLE, VRBodyPartType.RIGHT_KNEE);
        BONE_PARENTS.put(VRBodyPartType.RIGHT_FOOT, VRBodyPartType.RIGHT_ANKLE);
    }

    @Nullable
    private Vec3 anchorWorld;
    private float anchorYaw;
    private int lastAnchorTick;

    public GameEffectTrackerDebug(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {

        LocalPlayerPose renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        VRPoseTrackers trackers = renderPose.getTrackers();

        EnumMap<VRBodyPartType, VRPose> active = collectActive(trackers);
        if (active.isEmpty()) {
            return;
        }

        Vec3 hmdPos = new Vec3((Vector3f) renderPose.getHmd().getPosition());
        float yaw = renderPose.getHmd().getYaw();
        updateAnchor(hmdPos, yaw);

        double deltaYaw = anchorYaw + Math.PI - yaw;
        double cos = Math.cos(deltaYaw);
        double sin = Math.sin(deltaYaw);

        Vec3 camPos = new Vec3((Vector3f) RenderPoseHelper.getCameraPosition(renderPass, renderPose));

        // --- GL setup ---
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        // --- Pose setup ---
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        Matrix4f pose = poseStack.last().pose();

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        for (var entry : active.entrySet()) {
            VRPose ancestor = findActiveAncestor(entry.getKey(), active);
            if (ancestor != null) {
                Vec3 from = project(entry.getValue().getPosition(), hmdPos, cos, sin, camPos);
                Vec3 to = project(ancestor.getPosition(), hmdPos, cos, sin, camPos);
                addBeam(builder, pose, from, to, BONE_HALF_THICK, 225, 225, 235);
            }
        }

        for (VRPose tracker : active.values()) {
            Vec3 center = project(tracker.getPosition(), hmdPos, cos, sin, camPos);
            addAxis(builder, pose, center, projectDir(tracker.getCustomVector(AXIS_X), cos, sin), 235, 64, 52);  // X red
            addAxis(builder, pose, center, projectDir(tracker.getCustomVector(AXIS_Y), cos, sin), 64, 235, 90);  // Y green
            addAxis(builder, pose, center, projectDir(tracker.getCustomVector(AXIS_Z), cos, sin), 66, 135, 245); // Z blue
        }
        BufferUploader.drawWithShader(builder.buildOrThrow());

        poseStack.popPose();

        // --- Restore GL ---
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
    }

    private void updateAnchor(Vec3 hmdPos, float yaw) {
        if (anchorWorld != null && VisorState.TICK_COUNT - lastAnchorTick < REANCHOR_INTERVAL_TICKS) {
            return;
        }
        this.anchorWorld = hmdPos.add(
                -Math.sin(yaw) * FRONT_DISTANCE,
                0.0,
                Math.cos(yaw) * FRONT_DISTANCE
        );
        this.anchorYaw = yaw;
        this.lastAnchorTick = VisorState.TICK_COUNT;
    }

    private Vec3 project(Vector3fc worldPos, Vec3 hmdPos,
                         double cos, double sin, Vec3 camPos) {
        double rx = worldPos.x() - hmdPos.x;
        double ry = worldPos.y() - hmdPos.y;
        double rz = worldPos.z() - hmdPos.z;
        double x = rx * cos + rz * sin;
        double z = -rx * sin + rz * cos;
        return new Vec3(
                anchorWorld.x + x - camPos.x,
                anchorWorld.y + ry - camPos.y,
                anchorWorld.z + z - camPos.z
        );
    }

    private Vec3 projectDir(Vector3fc dir, double cos, double sin) {
        double x = dir.x() * cos + dir.z() * sin;
        double z = -dir.x() * sin + dir.z() * cos;
        return new Vec3(x, dir.y(), z);
    }

    private EnumMap<VRBodyPartType, VRPose> collectActive(VRPoseTrackers trackers) {
        EnumMap<VRBodyPartType, VRPose> active = new EnumMap<>(VRBodyPartType.class);
        List<VRPose> poses = trackers.getActiveTrackersPose();
        List<VRBodyPartType> types = trackers.getActiveTrackersType();
        int count = Math.min(poses.size(), types.size());
        for (int i = 0; i < count; i++) {
            active.put(types.get(i), poses.get(i));
        }
        return active;
    }

    @Nullable
    private VRPose findActiveAncestor(VRBodyPartType type,
                                      EnumMap<VRBodyPartType, VRPose> active) {
        VRBodyPartType parent = BONE_PARENTS.get(type);
        while (parent != null) {
            VRPose pose = active.get(parent);
            if (pose != null) {
                return pose;
            }
            parent = BONE_PARENTS.get(parent);
        }
        return null;
    }

    private void addAxis(BufferBuilder builder, Matrix4f pose,
                         Vec3 center, Vec3 dir,
                         int r, int g, int b) {
        Vec3 end = center.add(dir.x * AXIS_LENGTH, dir.y * AXIS_LENGTH, dir.z * AXIS_LENGTH);
        addBeam(builder, pose, center, end, AXIS_HALF_THICK, r, g, b);
    }


    private void addBeam(BufferBuilder buf, Matrix4f pose,
                         Vec3 start, Vec3 end, float halfThick,
                         int r, int g, int b) {
        Vec3 forward = end.subtract(start);
        double len = forward.length();
        if (len < 1.0e-6) {
            return;
        }
        forward = forward.scale(1.0 / len);

        Vec3 ref = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(ref).normalize().scale(halfThick);
        Vec3 up = right.cross(forward).normalize().scale(halfThick);

        Vec3 s0 = start.add(right).add(up);
        Vec3 s1 = start.subtract(right).add(up);
        Vec3 s2 = start.subtract(right).subtract(up);
        Vec3 s3 = start.add(right).subtract(up);
        Vec3 e0 = end.add(right).add(up);
        Vec3 e1 = end.subtract(right).add(up);
        Vec3 e2 = end.subtract(right).subtract(up);
        Vec3 e3 = end.add(right).subtract(up);

        // cull is disabled, so winding does not matter
        quad(buf, pose, s0, s1, s2, s3, r, g, b);
        quad(buf, pose, e0, e1, e2, e3, r, g, b);
        quad(buf, pose, s0, s3, e3, e0, r, g, b);
        quad(buf, pose, s1, s0, e0, e1, r, g, b);
        quad(buf, pose, s2, s1, e1, e2, r, g, b);
        quad(buf, pose, s3, s2, e2, e3, r, g, b);
    }

    private void quad(BufferBuilder buf, Matrix4f pose,
                      Vec3 a, Vec3 b, Vec3 c, Vec3 d,
                      int r, int g, int bl) {
        vertex(buf, pose, a, r, g, bl);
        vertex(buf, pose, b, r, g, bl);
        vertex(buf, pose, c, r, g, bl);
        vertex(buf, pose, d, r, g, bl);
    }

    private void vertex(BufferBuilder buf, Matrix4f pose, Vec3 p, int r, int g, int b) {
        buf.addVertex(pose, (float) p.x, (float) p.y, (float) p.z)
                .setColor(r, g, b, 255)
                .setNormal(0f, 1f, 0f)
        ;
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        if(!ENABLED){
            return false;
        }
        if (MC.player == null || !MC.player.isAlive()) {
            return false;
        }
        VRPoseTrackers trackers = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER)
                .getTrackers();
        return trackers.isActive() && !trackers.getActiveTrackersPose().isEmpty();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
