package me.phoenixra.visor.core.client.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.VisorRendererBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43;

import java.util.Comparator;
import java.util.Optional;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VREffectsHelper {


    public record NearestOpaqueBlock(float distance, BlockState state, BlockPos position) {}

    /**
     * Searches within a sphere of radius {@code radius} around {@code origin}
     * for the nearest block whose {@code isSolidRender} is true.
     *
     * @param origin the center of the search in world coordinates
     * @param radius the search radius
     * @return an Optional containing the nearest opaque block info, or empty if none found
     */
    public static Optional<NearestOpaqueBlock> findNearestSolidBlock(Vec3 origin, double radius) {
        ClientLevel level = MC.level;
        if (level == null) {
            return Optional.empty();
        }

        double radiusSq = radius * radius;
        AABB box = new AABB(
                origin.subtract(radius, radius, radius),
                origin.add(radius, radius, radius)
        );

        return BlockPos
                .betweenClosedStream(box)
                // only those that actually render as solid
                .filter(pos -> {
                    BlockState st = level.getBlockState(pos);
                    return st.isSolidRender(level, pos);
                })
                // ensure it’s truly within the spherical radius
                .filter(pos -> Vec3.atCenterOf(pos).distanceToSqr(origin) <= radiusSq)
                .map(pos -> {
                    float dist = (float) Vec3.atCenterOf(pos).distanceTo(origin);
                    return new NearestOpaqueBlock(dist, level.getBlockState(pos), pos);
                })
                // pick the one with the minimum distance
                .min(Comparator.comparingDouble(NearestOpaqueBlock::distance));
    }



    private static boolean stencilEnabledByVisor;


    public static void drawEyeStencil() {
        stencilEnabledByVisor = GL11C.glIsEnabled(GL11C.GL_STENCIL_TEST);

        if ((VRRenderState.getCurrentVRDisplay().isEye())) {
            doStencil(false);
        }
    }

    public static void disableStencilTest() {
        if (!stencilEnabledByVisor) {
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        }
    }




    public static void doStencil(boolean inverse) {
        Minecraft mc = Minecraft.getInstance();
        RenderTarget rt = mc.getMainRenderTarget();

        // 1) backup shader + matrices
        int prevProgram = GL11.glGetInteger(GL43.GL_CURRENT_PROGRAM);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.getModelViewStack().pushPose();

        try {
            enableStencilTest();
            configureStencilWrite(inverse);
            clearStencilAndDepth();

            setupMaskDrawState();
            applyOrthoProjection(rt, inverse);

            // draw hidden‐area triangles into the stencil
            VRDisplay eye = VRRenderState.getCurrentVRDisplay();
            float[] maskVerts = getStencilMask(eye);
            drawStencilMask(maskVerts);

        } finally {
            // 2) restore matrices
            RenderSystem.getModelViewStack().popPose();
            RenderSystem.restoreProjectionMatrix();

            // 3) restore GL state for regular rendering
            restorePostStencilState(prevProgram);
        }
    }

    private static void enableStencilTest() {
        GL11.glEnable(GL11.GL_STENCIL_TEST);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
        RenderSystem.stencilMask(0xFF);
    }

    private static void configureStencilWrite(boolean inverse) {
        if (inverse) {
            // clear stencil to 0xFF then write zero inside mask
            RenderSystem.clearStencil(0xFF);
            RenderSystem.clearDepth(0);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            RenderSystem.colorMask(false, false, false, true);
        } else {
            // clear stencil to 0 then write one inside mask
            RenderSystem.clearStencil(0);
            RenderSystem.clearDepth(1);
            RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0xFF, 0xFF);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    private static void clearStencilAndDepth() {
        RenderSystem.clear(
                GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT,
                false
        );
    }

    private static void setupMaskDrawState() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.disableCull();
        RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
    }

    private static void applyOrthoProjection(RenderTarget rt, boolean inverse) {

        Matrix4f ortho = new Matrix4f()
                .setOrtho(0, rt.viewWidth, 0, rt.viewHeight, 0, 20f);
        RenderSystem.setProjectionMatrix(ortho, VertexSorting.ORTHOGRAPHIC_Z);

        if (inverse) {
            RenderSystem.getModelViewStack().translate(0, 0, -20);
        }
        RenderSystem.applyModelViewMatrix();
    }

    private static float[] getStencilMask(VRDisplay eye) {
        if (eye != VRDisplay.EYE_LEFT && eye != VRDisplay.EYE_RIGHT) {
            return null;
        }
        VisorRendererBase renderer = ClientContext.renderer;
        return (eye == VRDisplay.EYE_LEFT)
                ? renderer.getHiddenAreaVertices(EyeType.LEFT)
                : renderer.getHiddenAreaVertices(EyeType.RIGHT);
    }

    private static void drawStencilMask(float[] verts) {
        if (verts == null || verts.length < 2) return;

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);

        // bind a simple 1×1 black texture so shader has "something"
        Minecraft.getInstance()
                .getTextureManager()
                .bindForSetup(TexturesHelper.getBlackTexture());

        float scale = ClientContext.renderer.renderScale;
        for (int i = 0; i < verts.length; i += 2) {
            buf
                    .vertex(verts[i] * scale, verts[i+1] * scale, 0f)
                    .endVertex();
        }

        RenderSystem.setShader(GameRenderer::getPositionShader);
        BufferUploader.drawWithShader(buf.end());
    }

    private static void restorePostStencilState(int prevProgram) {
        // restore program & color
        ProgramManager.glUseProgram(prevProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // stencil: only pass where stencil != 255
        RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 255, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        RenderSystem.stencilMask(0);

        // depth back to normal
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);

        RenderSystem.enableCull();
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }
}
