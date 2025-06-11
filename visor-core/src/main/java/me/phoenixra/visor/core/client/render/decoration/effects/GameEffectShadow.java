package me.phoenixra.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.entity.LocalPlayerModified;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorGame;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.TexturesHelper;
import me.phoenixra.visor.core.client.render.helpers.VRScreenHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectShadow extends VRGameEffect {
    private static final String ID = "shadow";
    public GameEffectShadow(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {


        // --- Prepare variables
        AABB box = MC.player.getBoundingBox();
        float playerWidth  = (float) box.getXsize();
        float playerLength = (float) box.getZsize();

        Vec3 camPos = RenderHelper.getCameraPosition(renderDisplay,
                ClientContext.player.getPose(PoseType.RENDER));
        Vec3 worldPlayerPos = ((GameRendererModified) MC.gameRenderer)
                .visor$getCameraEntityCache()
                .getInterpolatedPos(partialTicks);
        Vec3 shadowPos = worldPlayerPos
                .subtract(camPos)
                .add(0, 0.005, 0);

        // --- GL setup
        RenderSystem.disableCull();
        RenderHelper.setupPolyRendering(true);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderTexture(0, TexturesHelper.getWhiteTexture());


        // --- Pose setup
        poseStack.pushPose();

        poseStack.setIdentity();
        RenderHelper.applyDisplayOrientation(renderDisplay, poseStack);
        poseStack.translate(shadowPos.x, shadowPos.y, shadowPos.z);


        // --- Render
        VRScreenHelper.renderFlatQuad(
                Vec3.ZERO,
                playerWidth,
                playerLength,
                0f,
                0, 0,
                0, 64,
                poseStack
        );

        // --- Restore GL & pose
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderHelper.setupPolyRendering(false);
        RenderSystem.enableCull();

        poseStack.popPose();
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
