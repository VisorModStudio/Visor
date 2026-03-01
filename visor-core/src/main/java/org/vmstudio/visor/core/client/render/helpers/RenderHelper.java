package org.vmstudio.visor.core.client.render.helpers;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.mixin.client.accessors.RenderSystemAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderHelper {
    private RenderHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static boolean isInSolidBlock(Vector3fc in) {
        if (MC.level == null) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(new Vec3((Vector3f) in));
            return MC.level.getBlockState(blockpos).isSolidRender(MC.level, blockpos);
        }
    }

    public static void renderCuboid(BufferBuilder bufferBuilder,
                                    Matrix4f poseMatrix,
                                    Vector3fc start,
                                    Vector3fc end,
                                    float innerWidth, float outerWidth,
                                    float innerHeight, float outerHeight,
                                    AtumColor color) {

        // --- Prepare variables ---
        var forward = end.sub(start, new Vector3f()).normalize();
        var right = forward.cross(VRMathUtils.UP_VECTOR, new Vector3f()).normalize();
        var up = right.cross(forward, new Vector3f()).normalize();

        var r0 = right.mul(innerWidth, new Vector3f());
        var r1 = right.mul(outerWidth, new Vector3f());
        var u0 = up.mul(innerHeight, new Vector3f());
        var u1 = up.mul(outerHeight, new Vector3f());

        var corners = new Vector3fc[][] {
                { start, r0, u0 }, { start, r1, u0 }, { start, r1, u1 }, { start, r0, u1 },
                { end,   r0, u0 }, { end,   r1, u0 }, { end,   r1, u1 }, { end,   r0, u1 }
        };

        var faceIndices = new int[][] {
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
        var faceNormals = new Vector3f[] {
                forward, forward.mul(-1, new Vector3f()).normalize(),
                right, right.mul(-1, new Vector3f()).normalize(),
                up, up.mul(-1, new Vector3f()).normalize()
        };


        // --- Render ---
        bufferBuilder.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        for (int f = 0; f < faceIndices.length; f++) {
            Vector3f normal = faceNormals[f];
            for (int idx : faceIndices[f]) {
                var base = corners[idx][0];
                var xOff = corners[idx][1];
                var yOff = corners[idx][2];
                var pos = base.add(xOff, new Vector3f()).add(yOff);
                addVertex(bufferBuilder, poseMatrix, pos, color, normal);
            }
        }
        BufferUploader.drawWithShader(bufferBuilder.end());
    }


    public static void renderFlatQuad(BufferBuilder bufferBuilder,
                                      Matrix4f poseMatrix,
                                      Vector3fc pos,
                                      float width,
                                      float height,
                                      float yaw,
                                      AtumColor color) {
        // --- Prepare variables ---
        float halfW = width  * 0.5f;
        float halfH = height * 0.5f;
        Vector3f off = new Vector3f(halfW, 0, halfH)
                .rotateY((float)Math.toRadians(-yaw));
        Vector3fc normal = VRMathUtils.UP_VECTOR;
        float xOff = off.x, zOff = off.z;
        float r = color.getRed(), g = color.getGreen(),
                b = color.getBlue(), a = color.getAlpha();


        float[][] vertices = {
                { pos.x() + xOff, pos.y(), pos.z() + zOff },
                { pos.x() + xOff, pos.y(), pos.z() - zOff },
                { pos.x() - xOff, pos.y(), pos.z() - zOff },
                { pos.x() - xOff, pos.y(), pos.z() + zOff }
        };


        // --- Render ---
        bufferBuilder.begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (float[] vertex : vertices) {
            bufferBuilder.vertex(poseMatrix, vertex[0], vertex[1], vertex[2])
                    .color(r, g, b, a)
                    .normal(normal.x(), normal.y(), normal.z())
                    .endVertex();
        }
        BufferUploader.drawWithShader(bufferBuilder.end());

    }


    public static void renderDisplayQuad(Matrix4f poseMatrix,
                                         AtumColor color,
                                         float displayWidth,
                                         float displayHeight,
                                         float size) {
        // --- Prepare variables ---
        float aspect = displayHeight / displayWidth;
        float halfSize = size * 0.5f;
        float halfHeight = halfSize * aspect;
        float u0 = 0f, u1 = 1f, v0 = 0f, v1 = 1f;
        float r = color.getRed(), g = color.getGreen(),
                b = color.getBlue(), a = color.getAlpha();


        float[][] vertices = {
                { -halfSize, -halfHeight, 0f,   u0, v0 },
                {  halfSize, -halfHeight, 0f,   u1, v0 },
                {  halfSize,  halfHeight, 0f,   u1, v1 },
                { -halfSize,  halfHeight, 0f,   u0, v1 }
        };

        // --- Setup ---
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(r, g, b, a);

        // --- Render ---
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_TEX);

        for (float[] vertex : vertices) {
            buf.vertex(poseMatrix, vertex[0], vertex[1], vertex[2])
                    .uv(vertex[3], vertex[4])
                    .endVertex();
        }
        BufferUploader.drawWithShader(buf.end());

        // --- Restore ---
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

    }


    public static void renderDisplayQuadWithLight(Matrix4f poseMatrix,
                                                  AtumColor color,
                                                  float displayWidth,
                                                  float displayHeight,
                                                  float size,
                                                  int light,
                                                  boolean flipY) {
        renderDisplayQuadWithLight(poseMatrix, color, GameRenderer::getRendertypeEntityCutoutNoCullShader, displayWidth, displayHeight, size, light, flipY);
    }



    public static void renderDisplayQuadWithLight(Matrix4f poseMatrix,
                                                  AtumColor color,
                                                  Supplier<ShaderInstance> shader,
                                                  float displayWidth,
                                                  float displayHeight,
                                                  float size,
                                                  int light,
                                                  boolean flipY) {
        // --- Prepare variables ---
        float red = color.getRed();
        float green = color.getGreen();
        float blue = color.getBlue();
        float alpha = color.getAlpha();

        float aspect = displayHeight / displayWidth;
        float halfSize = size * 0.5f;
        float halfHeight = halfSize * aspect;
        float uMin = 0f;
        float uMax = 1f;
        float vMin = flipY ? 1f : 0f;
        float vMax = flipY ? 0f : 1f;

        float[][] pos = {
                { -halfSize, -halfHeight },
                {  halfSize, -halfHeight },
                {  halfSize,  halfHeight },
                { -halfSize,  halfHeight }
        };
        float[][] uv = {
                { uMin, vMin },
                { uMax, vMin },
                { uMax, vMax },
                { uMin, vMax }
        };

        // --- Setup ---
        RenderSystem.setShader(shader);
        MC.gameRenderer.lightTexture().turnOnLightLayer();
        MC.gameRenderer.overlayTexture().setupOverlayColor();

        // cache old light directions
        Vector3f[] oldLights = RenderSystemAccessor.getShaderLightDirections();
        Vector3f old0 = oldLights[0];
        Vector3f old1 = oldLights[1];

        // force lighting to face forward
        Vector3f forward = (Vector3f) VRMathUtils.FORWARD_VECTOR;
        RenderSystem.setShaderLights(forward, forward);
        RenderSystem.setupShaderLights(RenderSystem.getShader());


        // --- Render ---
        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        for (int i = 0; i < 4; i++) {
            float x = pos[i][0], y = pos[i][1];
            float u = uv[i][0], v = uv[i][1];
            buf.vertex(poseMatrix, x, y, 0f)
                    .color(red, green, blue, alpha)
                    .uv(u, v)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(0f, 0f, 1f)
                    .endVertex();
        }

        BufferUploader.drawWithShader(buf.end());

        // --- Restore ---
        MC.gameRenderer.lightTexture().turnOffLightLayer();
        if (old0 != null && old1 != null) {
            RenderSystem.setShaderLights(old0, old1);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }



    /**
     * Searches within a sphere of radius {@code radius} around {@code origin}
     * for the nearest block whose {@code isSolidRender} is true.
     *
     * @param origin the center of the search in world coordinates
     * @param radius the search radius
     * @return an Optional containing the nearest opaque block info, or empty if none found
     */
    public static Optional<VREffectsHelper.NearestOpaqueBlock> findNearestSolidBlock(Vec3 origin, double radius) {
        ClientLevel level = MC.level;
        if (level == null) {
            return Optional.empty();
        }


        AABB box = new AABB(
                origin.subtract(radius, radius, radius),
                origin.add(radius, radius, radius)
        );

        return BlockPos
                .betweenClosedStream(box)
                // only those that actually render as solid
                .filter(pos -> level.getBlockState(pos).isSolidRender(level, pos))
                .map(pos -> {
                    float dist = (float) Vec3.atCenterOf(pos).distanceTo(origin);
                    return new VREffectsHelper.NearestOpaqueBlock(dist, level.getBlockState(pos), pos);
                })
                // pick the one with the minimum distance
                .min(Comparator.comparingDouble(VREffectsHelper.NearestOpaqueBlock::distance));
    }




    private static void addVertex(BufferBuilder buff,
                                  Matrix4f mat,
                                  Vector3fc pos,
                                  AtumColor color,
                                  Vector3fc normal) {
        buff.vertex(mat, pos.x(), pos.y(), pos.z())
                .color(color.getRedInt(), color.getGreenInt(), color.getBlueInt(), (byte) color.getAlphaInt())
                .normal(normal.x(), normal.y(), normal.z())
                .endVertex();
    }
}
