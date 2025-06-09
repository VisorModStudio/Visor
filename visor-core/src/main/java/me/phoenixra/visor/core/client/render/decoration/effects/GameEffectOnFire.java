package me.phoenixra.visor.core.client.render.decoration.effects;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRGameEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.view.VRGameEffectBase;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorGame;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRGameEffect
public class GameEffectOnFire extends VRGameEffectBase {

    private static final String ID = "on_fire";
    public GameEffectOnFire(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void render(@NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       float partialTicks) {

        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);

        poseStack = new PoseStack();
        RenderHelper.applyDisplayOrientation(renderDisplay, poseStack);
        RenderHelper.applyDisplayTranslation(renderDisplay, poseStack);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        if (renderDisplay == VRDisplay.THIRD_PERSON) {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TextureAtlasSprite fireSprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.enableDepthTest();

        /*if (OptifineHelper.isOptifineLoaded()) {
            OptifineHelper.markTextureAsActive(fireSprite);
        }*/

        // code adapted from net.minecraft.client.renderer.ScreenEffectRenderer.renderFire

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, fireSprite.atlasLocation());
        float fireXMin = fireSprite.getU0();
        float fireXMax = fireSprite.getU1();
        float fireXMid = (fireXMin + fireXMax) / 2.0F;

        float fireYMin = fireSprite.getV0();
        float fireYMax = fireSprite.getV1();
        float fireYMid = (fireYMin + fireYMax) / 2.0F;

        float ShrinkRatio = fireSprite.uvShrinkRatio();

        float xMin = Mth.lerp(ShrinkRatio, fireXMin, fireXMid); //t, A, B
        float xMax = Mth.lerp(ShrinkRatio, fireXMax, fireXMid);
        float yMin = Mth.lerp(ShrinkRatio, fireYMin, fireYMid);
        float yMax = Mth.lerp(ShrinkRatio, fireYMax, fireYMid);

        float a = 0.3F;
        float b = (float) (renderPose.getHeadPivot().y - ((GameRendererModified) MC.gameRenderer).visor$getCameraEntityCache().getY());

        for (int i = 0; i < 4; ++i) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotation(
                    i * ((float)Math.PI/2) - renderPose.getBodyYaw()));
            poseStack.translate(0.0D, -b, 0.0D);
            Matrix4f matrix4f = poseStack.last().pose();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -a, 0.0F, -a)
                    .uv(xMax, yMax).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, 0.0F, -a)
                    .uv(xMin, yMax).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, b, -a)
                    .uv(xMin, yMin).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, -a, b, -a)
                    .uv(xMax, yMin).color(1.0F, 1.0F, 1.0F, 0.9F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());

            poseStack.popPose();
        }

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();

    }

    @Override
    public boolean isVisible() {
        String currentViewId = ClientContext.decoratorManager
                .getCurrentDecorator()
                .getId();
        if(!currentViewId.equals(DecoratorGame.ID)){
            return false;
        }
        return ((GameRendererModified) MC.gameRenderer).visor$isOnFire();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
