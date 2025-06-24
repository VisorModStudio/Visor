package me.phoenixra.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.entity.LocalPlayerModified;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorGame;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.render.helpers.TexturesHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectShadow extends VRGameEffect {
    private static final String ID = "shadow";

    private static final AtumColorImmutable SHADOW_COLOR = new AtumColorImmutable(
            0,0,0,
            64
    );
    private int glCacheBlendSrcA;
    private int glCacheBlendDstA;
    private int glCacheBlendSrcRGB;
    private int glCacheBlendDstRGB;
    private boolean glCacheBlend;
    private boolean glCacheCull;

    public GameEffectShadow(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {


        // --- Prepare variables ---
        AABB box = MC.player.getBoundingBox();
        float playerWidth  = (float) box.getXsize();
        float playerLength = (float) box.getZsize();

        Vec3 camPos = new Vec3((Vector3f) RenderPoseHelper.getCameraPosition(renderDisplay,
                ClientContext.player.getPose(PoseType.RENDER))
        );
        Vec3 worldPlayerPos = ((GameRendererModified) MC.gameRenderer)
                .visor$getCameraEntityCache()
                .getInterpolatedPos(partialTicks);
        Vec3 shadowPos = worldPlayerPos
                .subtract(camPos)
                .add(0, 0.005, 0);

        // --- GL setup ---
        RenderSystem.disableCull();
        setupPolygonGlState(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderTexture(0, TexturesHelper.getWhiteTexture());


        // --- Pose setup ---
        poseStack.pushPose();

        poseStack.setIdentity();
        RenderPoseHelper.applyDisplayOrientation(renderDisplay, poseStack);
        poseStack.translate(shadowPos.x, shadowPos.y, shadowPos.z);


        // --- Render ---
        RenderHelper.renderFlatQuad(
                Tesselator.getInstance().getBuilder(),
                poseStack.last().pose(),
                VRMathUtils.zeroVector,
                playerWidth,
                playerLength,
                0f,
                SHADOW_COLOR
        );

        // --- Restore GL & pose ---
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        setupPolygonGlState(false);
        RenderSystem.enableCull();

        poseStack.popPose();
    }


    private void setupPolygonGlState(boolean enable) {

        if (enable) {
            glCacheBlendSrcA = GlStateManager.BLEND.srcAlpha;
            glCacheBlendDstA = GlStateManager.BLEND.dstAlpha;
            glCacheBlendSrcRGB = GlStateManager.BLEND.srcRgb;
            glCacheBlendDstRGB = GlStateManager.BLEND.dstRgb;
            glCacheBlend = GL43C.glIsEnabled(GL11.GL_BLEND);
            glCacheCull = true;
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();

        } else {
            RenderSystem.blendFuncSeparate(glCacheBlendSrcRGB, glCacheBlendDstRGB, glCacheBlendSrcA,
                    glCacheBlendDstA);

            if (!glCacheBlend) {
                RenderSystem.disableBlend();
            }

            if (glCacheCull) {
                RenderSystem.enableCull();
            }


        }
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator) {
        if(!currentDecorator.getId().equals(DecoratorGame.ID)){
            return false;
        }
        if(VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON){
            return false;
        }
        if (!MC.player.isAlive()) {
            return false;
        }
        if (MC.player.getVehicle() != null) {
            return false;
        }
        if ((((LocalPlayerModified) MC.player).visor$getRoomYOffset() < 0.0D)) {
            return false;
        }

        return true;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
