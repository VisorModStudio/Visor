package org.vmstudio.visor.core.client.render.helpers;


import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.PrimitiveTopology;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;


import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RenderHelper {
    private RenderHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static boolean isInSolidBlock(Vector3fc in) {
        if (MC.level == null) {
            return false;
        }
        BlockPos pos = BlockPos.containing(in.x(), in.y(), in.z());
        return McVersionUtils.isSolidRender(MC.level.getBlockState(pos), MC.level, pos);
    }

    public static void renderCuboid(McVertexBuilder bufferBuilder,
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
                forward, forward.negate(new Vector3f()),
                right, right.negate(new Vector3f()),
                up, up.negate(new Vector3f())
        };


        // --- Render ---
        bufferBuilder.begin(
                PrimitiveTopology.QUADS,
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
        bufferBuilder.draw();
    }


    public static void renderFlatQuad(McVertexBuilder bufferBuilder,
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
                .rotateY(-yaw * Mth.DEG_TO_RAD);
        Vector3fc normal = VRMathUtils.UP_VECTOR;
        float xOff = off.x, zOff = off.z;
        float r = color.getRed(), g = color.getGreen(),
                b = color.getBlue(), a = color.getAlpha();


        float[][] vertices = {
                { pos.x() - xOff, pos.y(), pos.z() + zOff },
                { pos.x() + xOff, pos.y(), pos.z() + zOff },
                { pos.x() + xOff, pos.y(), pos.z() - zOff },
                { pos.x() - xOff, pos.y(), pos.z() - zOff }
        };


        // --- Render ---
        bufferBuilder.begin(PrimitiveTopology.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL);
        for (float[] vertex : vertices) {
            bufferBuilder.vertex(poseMatrix, vertex[0], vertex[1], vertex[2])
                    .color(r, g, b, a)
                    .normal(normal.x(), normal.y(), normal.z())
                    .endVertex();
        }
        bufferBuilder.draw();

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
        McShaders.use(McShaders.Core.POSITION_TEX);
        McGlState.setShaderColor(r, g, b, a);

        // --- Render ---
        McVertexBuilder buf = McVertexBuilder.get();
        buf.begin(PrimitiveTopology.QUADS,
                DefaultVertexFormat.POSITION_TEX);

        for (float[] vertex : vertices) {
            buf.vertex(poseMatrix, vertex[0], vertex[1], vertex[2])
                    .uv(vertex[3], vertex[4])
                    .endVertex();
        }
        buf.draw();

        // --- Restore ---
        McGlState.setShaderColor(1f, 1f, 1f, 1f);

    }


    public static void renderDisplayQuadWithLight(Matrix4f poseMatrix,
                                                  AtumColor color,
                                                  float displayWidth,
                                                  float displayHeight,
                                                  float size,
                                                  int packedLight,
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
        McShaders.use(McShaders.Core.RENDERTYPE_TEXT);
        McGlState.turnOnLightLayer();

        // --- Render ---
        McVertexBuilder buf = McVertexBuilder.get();
        buf.begin(PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);

        for (int i = 0; i < 4; i++) {
            buf.vertex(poseMatrix, pos[i][0], pos[i][1], 0f)
                    .color(red, green, blue, alpha)
                    .uv(uv[i][0], uv[i][1])
                    .uv2(packedLight)
                    .endVertex();
        }

        buf.draw();

        // --- Restore ---
        McGlState.turnOffLightLayer();
    }


    public static float distanceToNearestSolidBlockSurface(Vec3 origin, double maxRadius) {
        ClientLevel level = MC.level;
        if (level == null) {
            return (float) maxRadius;
        }

        int minX = (int) Math.floor(origin.x - maxRadius);
        int minY = (int) Math.floor(origin.y - maxRadius);
        int minZ = (int) Math.floor(origin.z - maxRadius);
        int maxX = (int) Math.floor(origin.x + maxRadius);
        int maxY = (int) Math.floor(origin.y + maxRadius);
        int maxZ = (int) Math.floor(origin.z + maxRadius);

        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();
        double minDistSq = maxRadius * maxRadius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mPos.set(x, y, z);
                    if (!McVersionUtils.isSolidRender(level.getBlockState(mPos), level, mPos)) {
                        continue;
                    }
                    double dx = Math.max(0.0, Math.max(x - origin.x, origin.x - (x + 1.0)));
                    double dy = Math.max(0.0, Math.max(y - origin.y, origin.y - (y + 1.0)));
                    double dz = Math.max(0.0, Math.max(z - origin.z, origin.z - (z + 1.0)));
                    double distSq = dx * dx + dy * dy + dz * dz;
                    if (distSq < minDistSq) {
                        minDistSq = distSq;
                        if (minDistSq <= 0.0) {
                            return 0.0f;
                        }
                    }
                }
            }
        }

        return (float) Math.sqrt(minDistSq);
    }


    private static void addVertex(McVertexBuilder buff,
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
