package me.phoenixra.visor.core.client.render.decoration.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import me.phoenixra.visor.api.client.render.decoration.effects.hand.HandRenderStage;
import me.phoenixra.visor.api.client.render.decoration.effects.hand.VRHandEffect;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandRenderer;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.decoration.registry.VRHandEffectRegistry;
import me.phoenixra.visor.core.client.render.decoration.registry.VRHandItemPoseRegistry;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.TexturesHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11C;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.ArrayList;
import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRHandRendererImpl implements VRHandRenderer {

    @Getter
    private final VRHandItemPoseRegistry itemPosesRegistry = new VRHandItemPoseRegistry();

    @Getter
    private final VRHandEffectRegistry effectsRegistry = new VRHandEffectRegistry();


    public void applyItemHandPose(@NotNull AbstractClientPlayer player,
                                  @NotNull ControllerHand hand,
                                  @NotNull ItemStack itemStack,
                                  @NotNull PoseStack poseStack,
                                  float equippedProgress,
                                  float partialTick
    ){
        for(VRHandItemPose entry : itemPosesRegistry.getSortedElements()){
            boolean appliedPose = entry.applyPose(
                    player,
                    hand,
                    itemStack,
                    poseStack,
                    equippedProgress,
                    partialTick
            );
            if(appliedPose) {
                return;
            }
        }

    }
    public void renderWorldHands(@NotNull PoseStack poseStack,
                                 float partialTick,
                                 boolean renderMain,
                                 boolean renderOffhand
    ) {
        // backup projection matrix, not doing might break some mods
        RenderSystem.backupProjectionMatrix();
        if (renderMain
                && ClientContext.rawPoseHandler.getControllerData(ControllerHand.MAIN)
                .isTracking()) {
            renderWorldHand(
                    ControllerHand.MAIN,
                    partialTick
            );
        }

        if (renderOffhand
                && ClientContext.rawPoseHandler.getControllerData(ControllerHand.OFFHAND)
                .isTracking()) {
            renderWorldHand(
                    ControllerHand.OFFHAND,
                    partialTick
            );
        }

        RenderSystem.restoreProjectionMatrix();
    }

    public void renderSimpleHands(@NotNull PoseStack poseStack,
                                  float partialTicks,
                                  boolean renderMain,
                                  boolean renderOffhand
    ) {

        RenderSystem.backupProjectionMatrix();

        if (renderMain && ClientContext.rawPoseHandler
                .getControllerData(ControllerHand.MAIN)
                .isTracking()) {
            renderSimpleHand(ControllerHand.MAIN, partialTicks, poseStack);
        }
        if (renderOffhand && ClientContext.rawPoseHandler
                .getControllerData(ControllerHand.OFFHAND)
                .isTracking()) {
            renderSimpleHand(ControllerHand.OFFHAND, partialTicks, poseStack);
        }

        RenderSystem.restoreProjectionMatrix();

    }



    private void renderSimpleHand(ControllerHand hand,
                                  float partialTick,
                                  PoseStack poseStack) {
        ((GameRendererModified) MC.gameRenderer)
                .visor$resetProjectionMatrix(partialTick);
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderHelper.applyDisplayOrientation(VRRenderState.getCurrentVRDisplay(), poseStack);
        RenderHelper.applyControllerPose(hand, poseStack);

        //EFFECTS
        List<VRHandEffect> effects = new ArrayList<>(effectsRegistry.getElementsMap().values());
        renderHandEffects(
                effects,
                hand,
                HandRenderStage.BEFORE_RENDERED,
                true,
                poseStack,
                partialTick
        );
        //------

        if (MC.getOverlay() == null) {
            MC.getTextureManager()
                    .bindForSetup(TexturesHelper.getWhiteTexture());
            RenderSystem.setShaderTexture(
                    0,
                    TexturesHelper.getWhiteTexture()
            );
        }
        Tesselator tesselator = Tesselator.getInstance();

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

        Vec3i color = new Vec3i(64, 64, 64);
        byte alpha = (byte) 255;

        Vec3 dir = new Vec3(0.0D, 0.0D, -1.0D);

        Vec3 start = new Vec3(0.0D, 0.0D, 0.0D);
        Vec3 end = new Vec3(
                start.x - dir.x * 0.18D,
                start.y - dir.y * 0.18D,
                start.z - dir.z * 0.18D
        );

        if (MC.level != null) {
            float light = (float) MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            ClientContext.player
                                    .getPose(PoseType.RENDER)
                                    .getHmd().getPosition()
                    )
            );

            int minLight = ShadersHelper.shaderLight();

            if (light < (float) minLight) {
                light = (float) minLight;
            }

            float lightPercent = light / (float) MC.level.getMaxLightLevel();
            color = new Vec3i(
                    Mth.floor(color.getX() * lightPercent),
                    Mth.floor(color.getY() * lightPercent),
                    Mth.floor(color.getZ() * lightPercent)
            );
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.getBuilder().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        RenderHelper.renderBox(
                tesselator.getBuilder(),
                start, end,
                -0.02F, 0.02F,
                -0.0125F, 0.0125F,
                color, alpha, poseStack
        );
        BufferUploader.drawWithShader(tesselator.getBuilder().end());


        //EFFECTS
        renderHandEffects(
                effects,
                hand,
                HandRenderStage.AFTER_RENDERED,
                true,
                poseStack,
                partialTick
        );
        //------

        poseStack.popPose();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }

    private void renderWorldHand(ControllerHand hand,
                                 float partialTick) {
        ((GameRendererModified) MC.gameRenderer)
                .visor$resetProjectionMatrix(partialTick);
        PoseStack poseStack = new PoseStack();
        poseStack.last().pose().identity();
        RenderHelper.applyDisplayOrientation(VRRenderState.getCurrentVRDisplay(), poseStack);

        poseStack.pushPose();
        RenderHelper.applyControllerPose(hand, poseStack);


        //EFFECTS
        List<VRHandEffect> effects = new ArrayList<>(effectsRegistry.getElementsMap().values());
        renderHandEffects(
                effects,
                hand,
                HandRenderStage.BEFORE_RENDERED,
                false,
                poseStack,
                partialTick
        );
        //------

        RenderSystem.enableDepthTest();


        InteractionHand interactionHand = hand==ControllerHand.MAIN
                ? InteractionHand.MAIN_HAND
                : InteractionHand.OFF_HAND;
        ItemStack item = MC.player.getItemInHand(interactionHand);
        if(MC.screen != null){
            item = ItemStack.EMPTY;
        }

        poseStack.pushPose();

        MC.gameRenderer.lightTexture().turnOnLightLayer();
        MultiBufferSource.BufferSource bufferSource = MC.renderBuffers().bufferSource();
        MC.gameRenderer.itemInHandRenderer.renderArmWithItem(MC.player, partialTick,
                0.0F, interactionHand, MC.player.getAttackAnim(partialTick), item, 0.0F,
                poseStack, bufferSource,
                MC.getEntityRenderDispatcher().getPackedLightCoords(MC.player, partialTick));
        bufferSource.endBatch();
        MC.gameRenderer.lightTexture().turnOffLightLayer();

        poseStack.popPose();

        //EFFECTS
        renderHandEffects(
                effects,
                hand,
                HandRenderStage.AFTER_RENDERED,
                false,
                poseStack,
                partialTick
        );
        //------


        poseStack.popPose();
    }


    private void renderHandEffects(List<VRHandEffect> effects,
                                   ControllerHand hand,
                                   HandRenderStage renderStage,
                                   boolean simpleHand,
                                   PoseStack poseStack,
                                   float partialTick){
        String currentView = ClientContext.decoratorManager.getCurrentDecorator().getId();
        List<VRHandEffect> consumed = new ArrayList<>();
        for(VRHandEffect effect : effects){
            if(!effect.isEnabled()) continue;
            if(effect.renderAtStage() != renderStage){
                continue;
            }
            if(!effect.isVisible(hand, simpleHand)){
                continue;
            }

            consumed.add(effect);

            effect.render(
                    hand,
                    VRRenderState.getCurrentVRDisplay(),
                    poseStack,
                    simpleHand,
                    partialTick
            );
        }
        effects.removeAll(consumed);
    }

}
