package me.phoenixra.visor.core.client.render.decoration.effects.hand;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRHandEffect;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorGame;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11C;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRHandEffect
public class HandEffectCrosshair extends VRHandEffect {
    private static final String ID = "crosshair";

    public HandEffectCrosshair(@NotNull VisorAddon owner){
        super(owner);
    }

    @Override
    public void render(@NotNull ControllerHand hand,
                       @NotNull VRDisplay renderDisplay,
                       @NotNull PoseStack poseStack,
                       boolean simpleHand, float partialTicks
    ) {

        PoseData renderPose = ClientContext.player
                .getPose(PoseType.RENDER);
        // white crosshair, with blending
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Vec3 crosshairRenderPos = ((GameRendererModified) MC.gameRenderer).visor$getCrossVec();
        Vec3 aim = crosshairRenderPos.subtract(renderPose.getController(hand).getPosition());

        float scale = (float) (0.125F * Math.sqrt(renderPose.getWorldScale()));

        //scooch closer a bit for light calc.
        crosshairRenderPos = crosshairRenderPos.add(aim.normalize().scale(-0.01D));

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderHelper.applyDisplayOrientation(renderDisplay, poseStack);

        Vec3 translate = crosshairRenderPos.subtract(MC.getCameraEntity().position());
        poseStack.translate(translate.x, translate.y, translate.z);


        if (MC.hitResult != null && MC.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockhitresult = (BlockHitResult) MC.hitResult;

            switch (blockhitresult.getDirection()) {
                case DOWN -> {
                    rotateDeg(poseStack,
                            renderPose.getController(hand).getYaw(), 0.0F, 1.0F,
                            0.0F);
                    rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
                }
                case UP -> {
                    rotateDeg(poseStack,
                            -renderPose.getController(hand).getYaw(), 0.0F,
                            1.0F, 0.0F);
                    rotateDeg(poseStack, 90.0F, 1.0F, 0.0F, 0.0F);
                }
                case WEST -> rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
                case EAST -> rotateDeg(poseStack, -90.0F, 0.0F, 1.0F, 0.0F);
                case SOUTH -> rotateDeg(poseStack, 180.0F, 0.0F, 1.0F, 0.0F);
            }
        } else {
            rotateDeg(poseStack,
                    -renderPose.getController(hand).getYaw(), 0.0F, 1.0F,
                    0.0F);
            rotateDeg(poseStack,
                    -renderPose.getController(hand).getPitch(), 1.0F, 0.0F,
                    0.0F);
        }


        MC.gameRenderer.lightTexture().turnOnLightLayer();
        poseStack.scale(scale, scale, scale);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

        RenderSystem.enableBlend(); // Fuck it, we want a proper crosshair
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        int light = LevelRenderer.getLightColor(MC.level, BlockPos.containing(crosshairRenderPos));
        float brightness = 1.0F;

        if (MC.hitResult == null || MC.hitResult.getType() == HitResult.Type.MISS) {
            brightness = 0.5F;
        }

        RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);

        // sprite location of the crosshair on the atlas
        float uMax = 15.0F / 256.0F;
        float vMax = 15.0F / 256.0F;

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

        RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        bufferbuilder.vertex(poseStack.last().pose(), -1.0F, 1.0F, 0.0F)
                .color(brightness, brightness, brightness, 1.0F)
                .uv(uMax, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(poseStack.last().pose(), 1.0F, 1.0F, 0.0F)
                .color(brightness, brightness, brightness, 1.0F)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(poseStack.last().pose(), 1.0F, -1.0F, 0.0F)
                .color(brightness, brightness, brightness, 1.0F)
                .uv(0.0F, vMax)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(0.0F, 0.0F, 1.0F).endVertex();
        bufferbuilder.vertex(poseStack.last().pose(), -1.0F, -1.0F, 0.0F)
                .color(brightness, brightness, brightness, 1.0F)
                .uv(uMax, vMax)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(0.0F, 0.0F, 1.0F).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());

        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        poseStack.popPose();

    }

    private void rotateDeg(PoseStack pose, float angle, float x, float y, float z) {
        pose.mulPose(new Quaternionf(new AxisAngle4f(angle * Mth.DEG_TO_RAD, x, y, z)));
    }

    @Override
    public boolean isVisible(@NotNull VRDecorator currentDecorator,
                             @NotNull ControllerHand hand,
                             boolean simpleHand) {
        if(!currentDecorator.getId().equals(DecoratorGame.ID)){
            return false;
        }
        if(simpleHand){
            return false;
        }
        if(hand != ClientContext.player.getActiveHand()){
            return false;
        }
        boolean insideBlock = ((GameRendererModified) MC.gameRenderer).visor$isInBlock() > 0.0F;
        if(insideBlock){
            return false;
        }
        return ClientContext.visor.isFeatureEnabled(ClientFeature.AIM_EFFECTS);
    }


    @Override
    public @NotNull String getId() {
        return ID;
    }

}
