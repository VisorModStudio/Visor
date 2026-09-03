package org.vmstudio.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import net.minecraft.world.entity.Pose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.entity.LocalPlayerExtension;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL43C;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectShadow extends VRGameEffect {
    public static final String ID = "shadow";

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
    public void render(@NotNull VRRenderPass renderPass,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {


        // --- Prepare variables ---
        var player = MC.player;
        if(player == null) return;
        AABB box = player.getBoundingBox();
        float playerWidth  = (float) box.getXsize();
        float playerLength = (float) box.getZsize();

        Vec3 camPos = new Vec3((Vector3f) RenderPoseHelper.getCameraPosition(renderPass,
                ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER))
        );
        Vec3 worldPlayerPos = ((GameRendererExtension) MC.gameRenderer)
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
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        poseStack.translate(shadowPos.x, shadowPos.y, shadowPos.z);


        // --- Render ---
        RenderHelper.renderFlatQuad(
                Tesselator.getInstance().getBuilder(),
                poseStack.last().pose(),
                VRMathUtils.ZERO_VECTOR,
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
        if(!VRClientSettings.isSelfShadowEnabled()){
            return false;
        }

        if(VRRenderState.getRenderPass() == VRRenderPass.THIRD_PERSON){
            return false;
        }

        if(MC.player == null) {
            return false;
        }
        if (!MC.player.isAlive()) {
            return false;
        }
        if (MC.player.getVehicle() != null) {
            return false;
        }
        Pose pose = MC.player.getPose();
        if (pose == Pose.SWIMMING
                || pose == Pose.FALL_FLYING
                || pose == Pose.SPIN_ATTACK) {
            return false;
        }

        return true;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
