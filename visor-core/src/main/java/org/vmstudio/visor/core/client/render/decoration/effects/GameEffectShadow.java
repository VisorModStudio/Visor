package org.vmstudio.visor.core.client.render.decoration.effects;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaders;
import org.vmstudio.visor.api.compatibility.mcversion.render.McVertexBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
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
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
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
        Vec3 worldPlayerPos = VRCameraEntitySwap.getCameraEntityCache()
                .getInterpolatedPos(partialTicks);
        Vec3 shadowPos = worldPlayerPos
                .subtract(camPos)
                .add(0, 0.005, 0);

        // --- GL setup ---
        McGlState.disableCull();
        setupPolygonGlState(true);
        McGlState.enableDepthTest();
        McGlState.depthFunc(GL11C.GL_ALWAYS);

        McShaders.use(McShaders.Core.POSITION_COLOR);
        McGlState.setShaderTexture(0, TexturesHelper.getWhiteTexture());


        // --- Pose setup ---
        poseStack.pushPose();

        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        poseStack.translate(shadowPos.x, shadowPos.y, shadowPos.z);


        // --- Render ---
        RenderHelper.renderFlatQuad(
                McVertexBuilder.get(),
                poseStack.last().pose(),
                VRMathUtils.ZERO_VECTOR,
                playerWidth,
                playerLength,
                0f,
                SHADOW_COLOR
        );

        // --- Restore GL & pose ---
        McGlState.depthFunc(GL11C.GL_LEQUAL);
        setupPolygonGlState(false);
        McGlState.enableCull();

        poseStack.popPose();
    }


    private void setupPolygonGlState(boolean enable) {

        if (enable) {
            glCacheBlendSrcA = McGlState.blendSourceAlpha();
            glCacheBlendDstA = McGlState.blendDestinationAlpha();
            glCacheBlendSrcRGB = McGlState.blendSourceRgb();
            glCacheBlendDstRGB = McGlState.blendDestinationRgb();
            glCacheBlend = GL43C.glIsEnabled(GL11.GL_BLEND);
            glCacheCull = true;
            McGlState.enableBlend();
            McGlState.defaultBlendFunc();
            McGlState.disableCull();

        } else {
            McGlState.blendFuncSeparate(glCacheBlendSrcRGB, glCacheBlendDstRGB, glCacheBlendSrcA,
                    glCacheBlendDstA);

            if (!glCacheBlend) {
                McGlState.disableBlend();
            }

            if (glCacheCull) {
                McGlState.enableCull();
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
