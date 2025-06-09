package me.phoenixra.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.view.VRGameEffectBase;
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
public class GameEffectShadow extends VRGameEffectBase {
    private static final String ID = "shadow";
    public GameEffectShadow(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {

        MC.getProfiler().push("vr shadow");
        AABB playerBox = MC.player.getBoundingBox();
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.disableCull();

        RenderHelper.applyDisplayOrientation(renderDisplay, poseStack);

        Vec3 cameraPos = RenderHelper.getCameraPosition(
                renderDisplay,
                ClientContext.player.getPose(PoseType.RENDER)
        );
        Vec3 interpolatedPlayerPos = ((GameRendererModified) MC.gameRenderer)
                .visor$getCameraEntityCache()
                .getInterpolatedPos(partialTicks);
        Vec3 pos = interpolatedPlayerPos.subtract(cameraPos).add(0.0D, 0.005D, 0.0D);
        RenderHelper.setupPolyRendering(true);
        RenderSystem.enableDepthTest();

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        MC.getTextureManager().bindForSetup(TexturesHelper.getWhiteTexture());
        RenderSystem.setShaderTexture(0, TexturesHelper.getWhiteTexture());

        VRScreenHelper.renderFlatQuad(pos, (float) (playerBox.maxX - playerBox.minX), (float) (playerBox.maxZ - playerBox.minZ),
                0.0F, 0, 0, 0, 64, poseStack);

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderHelper.setupPolyRendering(false);
        poseStack.popPose();
        RenderSystem.enableCull();
        MC.getProfiler().pop();
    }

    @Override
    public boolean isVisible() {
        String currentViewId = ClientContext.decoratorManager
                .getCurrentDecorator()
                .getId();
        if(!currentViewId.equals(DecoratorGame.ID)){
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
