package org.vmstudio.visor.core.client.render.decoration.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.render.VRCameraType;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.modified.client.render.GameRendererModified;
import org.vmstudio.visor.core.client.render.decoration.registry.VRHandEffectRegistry;
import org.vmstudio.visor.core.client.render.decoration.registry.VRHandItemPoseRegistry;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MirrorMode;
import org.vmstudio.visor.core.client.gui.VRCursorHandlerImpl;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11C;

import org.vmstudio.visor.core.client.ClientContext;

import java.util.*;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class VRHandRenderer {

    private static final AtumColorImmutable GUI_HANDS_COLOR = new AtumColorImmutable(
            64, 64, 64,
            255
    );

    private static final AtumColorImmutable CURSOR_DEFAULT_COLOR = new AtumColorImmutable(
            228, 228, 228,
            255
    );

    private static final float CURSOR_BOX_HALF_SIZE = 0.0016f;

    @Getter
    private final VRHandItemPoseRegistry itemPosesRegistry = new VRHandItemPoseRegistry();

    @Getter
    private final VRHandEffectRegistry effectsRegistry = new VRHandEffectRegistry();


    public void applyItemHandPose(@NotNull AbstractClientPlayer player,
                                  @NotNull HandType hand,
                                  @NotNull ItemStack itemStack,
                                  @NotNull PoseStack poseStack,
                                  float equippedProgress,
                                  float partialTicks
    ){
        for(VRHandItemPose entry : itemPosesRegistry.getSortedComponents()){
            if(!entry.isEnabledAndCanApplyPose(player, hand, itemStack)){
                continue;
            }
            entry.applyPose(
                    poseStack,
                    player,
                    hand,
                    itemStack,
                    equippedProgress,
                    partialTicks
            );
            return;
        }

    }

    public void renderWorldHands(@NotNull VRDecorator decorator,
                                 @NotNull PoseStack poseStack,
                                 float partialTicks,
                                 boolean renderMain,
                                 boolean renderOffhand
    ) {
        renderHands(decorator, poseStack, partialTicks, renderMain, renderOffhand, false);
    }

    public void renderGuiHands(@NotNull VRDecorator decorator,
                               @NotNull PoseStack poseStack,
                               float partialTicks,
                               boolean renderMain,
                               boolean renderOffhand
    ) {
        renderHands(decorator, poseStack, partialTicks, renderMain, renderOffhand, true);
    }


    public void renderHands(@NotNull VRDecorator decorator,
                            @NotNull PoseStack poseStack,
                            float partialTicks,
                            boolean renderMain,
                            boolean renderOffhand,
                            boolean isGui){
        if (!ClientContext.visor.isFeatureEnabled(ClientFeature.VR_HANDS)) {
            return;
        }
        //don't render world hands in third person
        if(VRRenderState.getCameraType() == VRCameraType.THIRD_PERSON){
            if(VRClientSettings.getMirrorMode() != MirrorMode.MIXED_REALITY){
                return;
            }
        }

        var cursorHandler = ClientContext.cursorHandler;
        RenderSystem.backupProjectionMatrix();

        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        VRCameraType cameraType = VRRenderState.getCameraType();
        Collection<VRHandEffect> effects = effectsRegistry.getComponentsMap().values();

        if (renderMain && isTrackingHand(HandType.MAIN)) {
            boolean isCursorHand = cursorHandler.isHandFocused(HandType.MAIN)
                    && (cursorHandler.getCursorHand() == HandType.MAIN
                    || cursorHandler.isTwoHandedCursor());
            boolean isGuiHand = isGui
                    || isCursorHand
                    || ClientContext.visor.isFeatureDisabled(ClientFeature.VR_WORLD_HANDS)
                    || ClientContext.visor.isFeatureDisabled(ClientFeature.VR_WORLD_HAND_MAIN);

            renderHand(
                    HandType.MAIN,
                    poseStack,
                    partialTicks,
                    isGuiHand,
                    cameraType,
                    effects,
                    decorator
            );
        }
        if (renderOffhand && isTrackingHand(HandType.OFFHAND)) {
            boolean isCursorHand = cursorHandler.isHandFocused(HandType.OFFHAND)
                    && (cursorHandler.getCursorHand() == HandType.OFFHAND
                    || cursorHandler.isTwoHandedCursor());
            boolean isGuiHand = isGui
                    || isCursorHand
                    || ClientContext.visor.isFeatureDisabled(ClientFeature.VR_WORLD_HANDS)
                    || ClientContext.visor.isFeatureDisabled(ClientFeature.VR_WORLD_HAND_OFFHAND);

            renderHand(
                    HandType.OFFHAND,
                    poseStack,
                    partialTicks,
                    isGuiHand,
                    cameraType,
                    effects,
                    decorator
            );
        }

        RenderSystem.restoreProjectionMatrix();
    }

    /**
     * Renders the cursor ray for all active cursor hands.
     * <p>
     * Called by decorators AFTER HUD overlays, but BEFORE Gui hands,
     * so the cursor is always visually on top of everything, e
     * except gui hands
     */
    public void renderCursor(@NotNull PoseStack poseStack,
                             float partialTicks) {

        if (!ClientContext.visor.isFeatureEnabled(ClientFeature.GUI_CURSOR)) {
            return;
        }

        VRCursorHandlerImpl cursorHandler = ClientContext.cursorHandler;

        RenderSystem.backupProjectionMatrix();
        ((GameRendererModified) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        VRCameraType cameraType = VRRenderState.getCameraType();

        boolean twoHanded = cursorHandler.isTwoHandedCursor();
        HandType primaryCursor = cursorHandler.getCursorHand();

        // Main hand
        if (twoHanded || primaryCursor == HandType.MAIN) {
            if (cursorHandler.isHandFocused(HandType.MAIN) && isTrackingHand(HandType.MAIN)) {
                renderCursorLine(HandType.MAIN, cameraType, poseStack, cursorHandler);
            }
        }

        // Offhand
        if (twoHanded || primaryCursor == HandType.OFFHAND) {
            if (cursorHandler.isHandFocused(HandType.OFFHAND) && isTrackingHand(HandType.OFFHAND)) {
                renderCursorLine(HandType.OFFHAND, cameraType, poseStack, cursorHandler);
            }
        }

        RenderSystem.restoreProjectionMatrix();
    }

    private void renderCursorLine(@NotNull HandType hand,
                                  @NotNull VRCameraType cameraType,
                                  @NotNull PoseStack poseStack,
                                  @NotNull VRCursorHandlerImpl cursorHandler) {

        float cursorLength = (float) cursorHandler.getCursorLineLength(hand);
        if (cursorLength <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(cameraType, poseStack);
        RenderPoseHelper.applyHandPose(hand, poseStack);

        Vector3f start = new Vector3f(0, 0, 0);
        Vector3f end = new Vector3f(0, 0, -cursorLength);

        // Compute brightness-tinted color
        AtumColorImmutable color;
        if (MC.level != null) {
            float rawLight = MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            new Vec3(
                                    (Vector3f) ClientContext.localPlayer
                                            .getPoseData(PlayerPoseType.RENDER)
                                            .getHmd()
                                            .getPosition()
                            )
                    )
            );

            float light = Math.max(rawLight, ShadersHelper.shaderLight());
            float lightPercent = light / MC.level.getMaxLightLevel();
            color = new AtumColorImmutable(
                    Mth.floor(CURSOR_DEFAULT_COLOR.getRedInt() * lightPercent),
                    Mth.floor(CURSOR_DEFAULT_COLOR.getGreenInt() * lightPercent),
                    Mth.floor(CURSOR_DEFAULT_COLOR.getBlueInt() * lightPercent),
                    255
            );
        } else {
            color = CURSOR_DEFAULT_COLOR;
        }

        // --- GL setup ---
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        if (MC.getOverlay() == null) {
            var whiteTex = TexturesHelper.getWhiteTexture();
            MC.getTextureManager().bindForSetup(whiteTex);
            RenderSystem.setShaderTexture(0, whiteTex);
        }

        // --- Render ---
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        RenderHelper.renderCuboid(
                builder,
                poseStack.last().pose(),
                start, end,
                -CURSOR_BOX_HALF_SIZE, CURSOR_BOX_HALF_SIZE,
                -CURSOR_BOX_HALF_SIZE, CURSOR_BOX_HALF_SIZE,
                color
        );

        // --- Restore GL ---
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

        poseStack.popPose();
    }


    private void renderHand(HandType hand,
                            @NotNull PoseStack poseStack,
                            float partialTicks,
                            boolean isGui,
                            VRCameraType cameraType,
                            Collection<VRHandEffect> effects,
                            VRDecorator decorator) {

        poseStack.pushPose();

        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(cameraType, poseStack);
        RenderPoseHelper.applyHandPose(hand, poseStack);

        var stageEffects = groupEffectsByStage(effects, decorator, hand, isGui);

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        renderHandEffects(
                stageEffects.get(VRHandEffect.RenderStage.BEFORE_HANDS),
                hand,
                cameraType,
                poseStack,
                isGui,
                partialTicks
        );

        if (isGui) {
            renderGuiHand(poseStack);
        } else {
            renderWorldHand(poseStack, hand, partialTicks);
        }

        renderHandEffects(
                stageEffects.get(VRHandEffect.RenderStage.AFTER_HANDS),
                hand,
                cameraType,
                poseStack,
                isGui,
                partialTicks
        );

        poseStack.popPose();
    }


    private void renderGuiHand(PoseStack poseStack) {

        MC.getTextureManager().bindForSetup(TexturesHelper.getWhiteTexture());
        RenderSystem.setShaderTexture(
                0,
                TexturesHelper.getWhiteTexture()
        );

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        AtumColorImmutable color;

        Vector3fc dir = VRMathUtils.BACK_VECTOR;

        Vector3f start = new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3f end = new Vector3f(
                start.x - dir.x() * 0.18f,
                start.y - dir.y() * 0.18f,
                start.z - dir.z() * 0.18f
        );

        if (MC.level != null) {
            float light = (float) MC.level.getMaxLocalRawBrightness(
                    BlockPos.containing(
                            new Vec3(
                                    (Vector3f) ClientContext.localPlayer
                                            .getPoseData(PlayerPoseType.RENDER)
                                            .getHmd().getPosition()
                            )
                    )
            );

            light = Math.max(light, ShadersHelper.shaderLight());
            float lightPercent = light / (float) MC.level.getMaxLightLevel();

            color = new AtumColorImmutable(
                    Mth.floor(GUI_HANDS_COLOR.getRedInt() * lightPercent),
                    Mth.floor(GUI_HANDS_COLOR.getGreenInt() * lightPercent),
                    Mth.floor(GUI_HANDS_COLOR.getBlueInt() * lightPercent),
                    255
            );
        }else{
            color = GUI_HANDS_COLOR;
        }

        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        RenderHelper.renderCuboid(
                tesselator.getBuilder(),
                poseStack.last().pose(),
                start, end,
                -0.02F, 0.02F,
                -0.0125F, 0.0125F,
                color
        );
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);

    }

    private void renderWorldHand(PoseStack poseStack,
                                 HandType hand,
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


    private void renderHandEffects(Collection<VRHandEffect> effects,
                                   HandType hand,
                                   VRCameraType cameraType,
                                   PoseStack poseStack,
                                   boolean isSimple,
                                   float partialTicks) {
        if (effects == null || effects.isEmpty()) return;

        effects.forEach(it->
                it.render(hand, cameraType, poseStack, isSimple, partialTicks)
        );
    }


    private Map<VRHandEffect.RenderStage, Collection<VRHandEffect>> groupEffectsByStage(
            Collection<VRHandEffect> effects, VRDecorator decorator, HandType hand, boolean isSimple) {

        Map<VRHandEffect.RenderStage, Collection<VRHandEffect>> map = new EnumMap<>(VRHandEffect.RenderStage.class);
        for (VRHandEffect effect : effects) {
            if(!effect.isGlobal()
                    && !decorator.handEffects().contains(effect.getId())){
                continue;
            }
            if (effect.isEnabledAndVisible(decorator, hand, isSimple)) {
                map.computeIfAbsent(effect.renderAtStage(), k -> new java.util.ArrayList<>()).add(effect);
            }
        }
        return map;
    }

    private boolean isTrackingHand(HandType hand) {
        return ClientContext.rawPoseHandler.getControllerData(hand)
                .isTracking();
    }

}