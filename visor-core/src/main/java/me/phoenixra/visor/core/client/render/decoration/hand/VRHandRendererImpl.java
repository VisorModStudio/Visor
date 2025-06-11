package me.phoenixra.visor.core.client.render.decoration.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.effects.VRHandEffect;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandRenderer;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
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

import java.util.*;

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
            if(!entry.isEnabledAndCanApplyPose(player, hand, itemStack)){
                continue;
            }
            entry.applyPose(
                    poseStack,
                    player,
                    hand,
                    itemStack,
                    equippedProgress,
                    partialTick
            );
            return;
        }

    }

    public void renderWorldHands(@NotNull PoseStack poseStack,
                                 float partialTicks,
                                 boolean renderMain,
                                 boolean renderOffhand
    ) {
        renderHands(poseStack, partialTicks, renderMain, renderOffhand, false);
    }

    public void renderGuiHands(@NotNull PoseStack poseStack,
                               float partialTicks,
                               boolean renderMain,
                               boolean renderOffhand
    ) {
        renderHands(poseStack, partialTicks, renderMain, renderOffhand, true);
    }


    public void renderHands(@NotNull PoseStack poseStack,
                            float partialTicks,
                            boolean renderMain,
                            boolean renderOffhand,
                            boolean isGui
    ){

        RenderSystem.backupProjectionMatrix();

        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        VRDisplay display = VRRenderState.getCurrentVRDisplay();
        Collection<VRHandEffect> effects = effectsRegistry.getElementsMap().values();
        VRDecorator decorator = ClientContext.decoratorManager.getCurrentDecorator();

        if (renderMain && isControllerTracking(ControllerHand.MAIN)) {
            boolean isGuiHand = isGui || ClientContext.cursorHandler.isMainHandFocused();

            renderHand(
                    ControllerHand.MAIN,
                    poseStack,
                    partialTicks,
                    isGuiHand,
                    display,
                    effects,
                    decorator
            );
        }
        if (renderOffhand && isControllerTracking(ControllerHand.OFFHAND)) {
            boolean isGuiHand = isGui || ClientContext.cursorHandler.isOffhandFocused();

            renderHand(
                    ControllerHand.OFFHAND,
                    poseStack,
                    partialTicks,
                    isGuiHand,
                    display,
                    effects,
                    decorator
            );
        }

        RenderSystem.restoreProjectionMatrix();
    }

    private void renderHand(ControllerHand hand, @NotNull PoseStack poseStack, float partialTick, boolean isGui,
                            VRDisplay display, Collection<VRHandEffect> effects, VRDecorator decorator) {

        poseStack.pushPose();

        poseStack.setIdentity();
        RenderHelper.applyDisplayOrientation(display, poseStack);
        RenderHelper.applyControllerPose(hand, poseStack);

        var stageEffects = groupEffectsByStage(effects, decorator, hand, isGui);

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        renderHandEffects(
                stageEffects.get(VRHandEffect.RenderStage.BEFORE_HANDS),
                hand,
                display,
                poseStack,
                isGui,
                partialTick
        );

        if (isGui) {
            renderGuiHand(poseStack);
        } else {
            renderWorldHand(poseStack, hand, partialTick);
        }

        renderHandEffects(
                stageEffects.get(VRHandEffect.RenderStage.AFTER_HANDS),
                hand,
                display,
                poseStack,
                isGui,
                partialTick
        );

        poseStack.popPose();
    }


    private void renderGuiHand(PoseStack poseStack) {


        MC.getTextureManager().bindForSetup(TexturesHelper.getWhiteTexture());
        RenderSystem.setShaderTexture(
                0,
                TexturesHelper.getWhiteTexture()
        );

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

        Vec3i color = new Vec3i(64, 64, 64);

        Vec3 dir = VRMathUtils.forwardVectorMc;

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

            light = Math.max(light, ShadersHelper.shaderLight());
            float lightPercent = light / (float) MC.level.getMaxLightLevel();

            color = new Vec3i(
                    Mth.floor(color.getX() * lightPercent),
                    Mth.floor(color.getY() * lightPercent),
                    Mth.floor(color.getZ() * lightPercent)
            );
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        tesselator.getBuilder().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR_NORMAL
        );
        RenderHelper.renderBox(
                tesselator.getBuilder(),
                start, end,
                -0.02F, 0.02F,
                -0.0125F, 0.0125F,
                color, (byte) 255,
                poseStack
        );
        BufferUploader.drawWithShader(tesselator.getBuilder().end());


    }

    private void renderWorldHand(PoseStack poseStack,
                                 ControllerHand hand,
                                 float partialTicks) {

        if(MC.player == null) return;

        InteractionHand interactionHand = hand.asInteractionHand();
        ItemStack item = MC.player.getItemInHand(interactionHand);
        if(MC.screen != null){
            item = ItemStack.EMPTY;
        }

        poseStack.pushPose();

        MC.gameRenderer.lightTexture().turnOnLightLayer();
        MultiBufferSource.BufferSource bufferSource = MC.renderBuffers().bufferSource();
        MC.gameRenderer.itemInHandRenderer.renderArmWithItem(
                MC.player, partialTicks, 0.0F,
                interactionHand,
                MC.player.getAttackAnim(partialTicks),
                item, 0.0F, poseStack,
                bufferSource,
                MC.getEntityRenderDispatcher().getPackedLightCoords(
                        MC.player,
                        partialTicks
                )
        );
        bufferSource.endBatch();
        MC.gameRenderer.lightTexture().turnOffLightLayer();

        poseStack.popPose();

    }


    private void renderHandEffects(Collection<VRHandEffect> effects, ControllerHand hand, VRDisplay display,
                                   PoseStack poseStack, boolean isSimple, float partialTick) {
        if (effects == null || effects.isEmpty()) return;

        effects.forEach(it->
                it.render(hand, display, poseStack, isSimple, partialTick)
        );
    }

    private boolean isControllerTracking(ControllerHand hand) {
        return ClientContext.rawPoseHandler.getControllerData(hand).isTracking();
    }

    private Map<VRHandEffect.RenderStage, Collection<VRHandEffect>> groupEffectsByStage(
            Collection<VRHandEffect> effects, VRDecorator decorator, ControllerHand hand, boolean isSimple) {

        Map<VRHandEffect.RenderStage, Collection<VRHandEffect>> map = new EnumMap<>(VRHandEffect.RenderStage.class);
        for (VRHandEffect effect : effects) {
            if (effect.isEnabledAndVisible(decorator, hand, isSimple)) {
                map.computeIfAbsent(effect.renderAtStage(), k -> new java.util.ArrayList<>()).add(effect);
            }
        }
        return map;
    }

}
