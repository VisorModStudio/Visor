package org.vmstudio.visor.core.client.render.decoration.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.MapItem;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.input.HandAction;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import org.vmstudio.visor.core.client.utils.PlayerModelUtils;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.decoration.registry.VRHandEffectRegistry;
import org.vmstudio.visor.core.client.render.decoration.registry.VRHandItemPoseRegistry;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
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
import org.lwjgl.opengl.GL11C;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;

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


    @Getter @Setter
    private HandAction swingType = HandAction.ATTACK;


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

    public void renderHandEffectsOnly(@NotNull VRDecorator decorator,
                                      @NotNull PoseStack poseStack,
                                      @NotNull HandRenderState handStateMain,
                                      @NotNull HandRenderState handStateOffhand,
                                      boolean isGuiStage,
                                      float partialTicks){
        VRRenderPass renderPass = VRRenderState.getRenderPass();

        poseStack.pushPose();

        for(HandType hand : HandType.values()) {
            HandRenderState handState = hand == HandType.MAIN
                    ? handStateMain
                    : handStateOffhand;
            if(handState == HandRenderState.OFF) continue;
            if(!isGuiStage && handState.isGuiHand()) continue;
            if(isGuiStage && handState.isWorldHand()) continue;

            poseStack.setIdentity();
            RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
            RenderPoseHelper.applyHandPose(hand, poseStack);

            Collection<VRHandEffect> effects = effectsRegistry.getComponentsMap().values();
            var activeEffects = findActiveEffects(effects, decorator, hand, handState.isGuiHand());

            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();

            renderHandEffects(
                    activeEffects,
                    hand,
                    renderPass,
                    poseStack,
                    handState.isGuiHand(),
                    partialTicks
            );
        }
        poseStack.popPose();
    }

    /**
     * Uses {@link VRBodyRenderer#getModelRenderer(VRClientPlayer, String) player renderer} to render hands
     *
     */
    public void renderWorldHands(@NotNull PoseStack poseStack,
                                 @NotNull HandRenderState handStateMain,
                                 @NotNull HandRenderState handStateOffhand,
                                 float partialTicks
    ) {
        renderHands(poseStack, handStateMain, handStateOffhand, partialTicks,false);
    }

    public void renderGuiHands(@NotNull PoseStack poseStack,
                               @NotNull HandRenderState handStateMain,
                               @NotNull HandRenderState handStateOffhand,
                               float partialTicks
    ) {
        renderHands(poseStack, handStateMain, handStateOffhand, partialTicks,true);
    }


    public void renderHands(@NotNull PoseStack poseStack,
                            @NotNull HandRenderState handStateMain,
                            @NotNull HandRenderState handStateOffhand,
                            float partialTicks,
                            boolean isGuiStage){
        RenderSystem.backupProjectionMatrix();

        ((GameRendererExtension) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        VRRenderPass renderPass = VRRenderState.getRenderPass();

        if(handStateMain.isGuiHand() && isGuiStage){
            renderHand(
                    HandType.MAIN,
                    handStateMain,
                    true,
                    renderPass,
                    poseStack, partialTicks
            );
        } else if(handStateMain.isWorldHand() && !isGuiStage){
            renderHand(
                    HandType.MAIN,
                    handStateMain,
                    false,
                    renderPass,
                    poseStack, partialTicks
            );
        }

        if(handStateOffhand.isGuiHand() && isGuiStage){
            renderHand(
                    HandType.OFFHAND,
                    handStateOffhand,
                    true,
                    renderPass,
                    poseStack, partialTicks
            );
        } else if(handStateOffhand.isWorldHand() && !isGuiStage){
            renderHand(
                    HandType.OFFHAND,
                    handStateOffhand,
                    false,
                    renderPass,
                    poseStack, partialTicks
            );
        }

        RenderSystem.restoreProjectionMatrix();
    }
    public void renderSpectatedHands(@NotNull PlayerRenderer renderer,
                                     @NotNull AbstractClientPlayer player,
                                     @NotNull VRClientPlayer vrPlayer,
                                     @NotNull PoseStack poseStack,
                                     @NotNull MultiBufferSource buffer,
                                     int packedLight,
                                     float partialTicks) {
        var renderPose = vrPlayer.getPoseData(PlayerPoseType.RENDER);

        Vec3 offset = renderer.getRenderOffset(player, partialTicks);
        Vector3f referenceOrigin = new Vector3f(
                (float) (Mth.lerp(partialTicks, player.xOld, player.getX()) + offset.x),
                (float) (Mth.lerp(partialTicks, player.yOld, player.getY()) + offset.y),
                (float) (Mth.lerp(partialTicks, player.zOld, player.getZ()) + offset.z)
        );

        for (HandType hand : HandType.values()) {
            poseStack.pushPose();
            RenderPoseHelper.applyHandPose(renderPose, hand, referenceOrigin, poseStack);
            renderWorldArmWithItem(
                    player,
                    vrPlayer,
                    hand.asInteractionHand(),
                    HandRenderState.WORLD_HAND,
                    player.getAttackAnim(partialTicks),
                    player.getItemInHand(hand.asInteractionHand()),
                    poseStack,
                    buffer,
                    packedLight,
                    partialTicks
            );
            poseStack.popPose();
        }
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
        ((GameRendererExtension) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        VRRenderPass renderPass = VRRenderState.getRenderPass();

        boolean twoHanded = cursorHandler.isTwoHandedCursor();
        HandType primaryCursor = cursorHandler.getCursorHand();

        // Main hand
        if (twoHanded || primaryCursor == HandType.MAIN) {
            if (cursorHandler.isHandFocused(HandType.MAIN) && isTrackingHand(HandType.MAIN)) {
                renderCursorLine(HandType.MAIN, renderPass, poseStack, cursorHandler);
            }
        }

        // Offhand
        if (twoHanded || primaryCursor == HandType.OFFHAND) {
            if (cursorHandler.isHandFocused(HandType.OFFHAND) && isTrackingHand(HandType.OFFHAND)) {
                renderCursorLine(HandType.OFFHAND, renderPass, poseStack, cursorHandler);
            }
        }

        RenderSystem.restoreProjectionMatrix();
    }

    private void renderCursorLine(@NotNull HandType hand,
                                  @NotNull VRRenderPass renderPass,
                                  @NotNull PoseStack poseStack,
                                  @NotNull VRCursorHandlerImpl cursorHandler) {

        float cursorLength = (float) cursorHandler.getCursorLineLength(hand);
        if (cursorLength <= 0) {
            return;
        }

        poseStack.pushPose();
        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        RenderPoseHelper.applyHandPose(hand, poseStack);

        Vector3f start = new Vector3f(0, 0, 0);
        Vector3f end = new Vector3f(0, 0, -cursorLength);

        AtumColorImmutable color = dimByLocalLight(CURSOR_DEFAULT_COLOR);

        // --- GL setup ---
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);
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
        RenderSystem.depthMask(true);

        poseStack.popPose();
    }


    private void renderHand(HandType hand,
                            HandRenderState state,
                            boolean isGui,
                            VRRenderPass renderPass,
                            @NotNull PoseStack poseStack,
                            float partialTicks) {

        poseStack.pushPose();

        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        RenderPoseHelper.applyHandPose(hand, poseStack);


        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        if (isGui) {
            renderGuiHand(poseStack);
        } else {
            renderWorldHand(poseStack, hand, state, partialTicks);
        }

        poseStack.popPose();
    }


    private void renderGuiHand(PoseStack poseStack) {
        var whiteTex = TexturesHelper.getWhiteTexture();
        MC.getTextureManager().bindForSetup(whiteTex);
        RenderSystem.setShaderTexture(0, whiteTex);

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);

        float handLength = 0.18F;
        Vector3f start = new Vector3f();
        Vector3f end = new Vector3f(VRMathUtils.FORWARD_VECTOR).mul(-handLength);

        AtumColorImmutable color = dimByLocalLight(GUI_HANDS_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderHelper.renderCuboid(
                Tesselator.getInstance().getBuilder(),
                poseStack.last().pose(),
                start, end,
                -0.02F, 0.02F,
                -0.0125F, 0.0125F,
                color
        );
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);
    }

    private AtumColorImmutable dimByLocalLight(AtumColorImmutable base) {
        if (MC.level == null) {
            return base;
        }
        Vec3 headPos = new Vec3((Vector3f) ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER).getHmd().getPosition());
        float light = MC.level.getMaxLocalRawBrightness(BlockPos.containing(headPos));
        light = Math.max(light, ShadersHelper.minShaderLight());
        float fraction = light / MC.level.getMaxLightLevel();
        return new AtumColorImmutable(
                Mth.floor(base.getRedInt() * fraction),
                Mth.floor(base.getGreenInt() * fraction),
                Mth.floor(base.getBlueInt() * fraction),
                255
        );
    }

    private void renderWorldHand(PoseStack poseStack,
                                 HandType hand,
                                 HandRenderState state,
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

        renderWorldArmWithItem(
                MC.player,
                ClientContext.localPlayer,
                interactionHand,
                state,
                MC.player.getAttackAnim(partialTicks),
                item,  poseStack,
                bufferSource,
                MC.getEntityRenderDispatcher().getPackedLightCoords(MC.player, partialTicks),
                partialTicks
        );
        bufferSource.endBatch();
        MC.gameRenderer.lightTexture().turnOffLightLayer();

        poseStack.popPose();

    }


    private void renderWorldArmWithItem(AbstractClientPlayer player,
                                        VRClientPlayer vrPlayer,
                                        InteractionHand hand,
                                        HandRenderState state,
                                        float swingProgress,
                                        ItemStack itemStack,
                                        PoseStack poseStack,
                                        MultiBufferSource buffer,
                                        int packedLight,
                                        float partialTicks
    ) {
        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = mainHand
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        float equipProgress = ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                .visor$getEquipProgress(hand, partialTicks);

        boolean mixedRealityHands = VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY
                && VRClientSettings.isMixedRealityRenderHands();
        boolean renderArm = VRRenderState.getRenderPass() != VRRenderPass.THIRD_PERSON
                || mixedRealityHands;

        poseStack.pushPose();
        if (renderArm && !player.isInvisible() && !state.isWithItemOnly()) {
            renderWorldArm(player, vrPlayer, poseStack, buffer, packedLight,
                    equipProgress, swingProgress, arm);
        }

        if (itemStack.isEmpty() || !state.isWithItem()) {
            poseStack.popPose();
            return;
        }

        if (player.swingingArm == hand) {
            applySwingPose(swingType, poseStack, arm, swingProgress);
        }

        float entityScale = 0.9375F;
        float armsScale = VRClientSettings.getPlayerModelArmsScale();
        poseStack.scale(entityScale, entityScale, entityScale);

        PlayerModelUtils.controllerToModelOrientation(poseStack);

        // keep the item anchored the same way ItemInHandLayerMixin does
        final float gripY = 0.65F;
        poseStack.scale(armsScale, 1.0F, armsScale);
        poseStack.translate(0.0F, gripY, 0.0F);
        poseStack.scale(1.0F, armsScale, 1.0F);
        poseStack.translate(0.0F, -gripY, 0.0F);

        boolean isLeftHand = arm == HumanoidArm.LEFT;
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0, 0.125F, 0.625F);

        HandType handType = mainHand ? HandType.MAIN : HandType.OFFHAND;
        applyItemHandPose(player, handType, itemStack, poseStack, equipProgress, partialTicks);

        if (itemStack.getItem() instanceof MapItem) {
            RenderSystem.disableCull();
            ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                    .visor$renderMap(poseStack, buffer, packedLight, itemStack);
        } else {
            ItemDisplayContext displayCtx = isLeftHand
                    ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            MC.gameRenderer.itemInHandRenderer.renderItem(
                    player, itemStack, displayCtx, isLeftHand,
                    poseStack, buffer, packedLight
            );
        }

        poseStack.popPose();
    }

    private void renderWorldArm(AbstractClientPlayer player,
                                VRClientPlayer vrPlayer,
                                PoseStack poseStack,
                                MultiBufferSource buffer,
                                int packedLight, float equipProgress, float swingProgress,
                                HumanoidArm arm
    ) {
        boolean mainHand = arm != HumanoidArm.LEFT;
        float handFactor = mainHand ? 1.0F : -1.0F;
        RenderSystem.setShaderTexture(0, player.getSkinTextureLocation());

        poseStack.pushPose();

        InteractionHand swingHand = mainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (player.swingingArm == swingHand) {
            applySwingPose(swingType, poseStack, arm, swingProgress);
        }

        float armScale = 0.4F;
        poseStack.scale(armScale, armScale, armScale);

        boolean slim = "slim".equals(player.getModelName());
        float sideShift = slim ? -0.34375F : -0.375F;
        float depthShift = slim ? 0.78125F : 0.75F;
        poseStack.translate(sideShift * handFactor, 0.0F, depthShift);
        PlayerModelUtils.controllerToModelOrientation(poseStack);

        var bodyRenderer = vrPlayer.getBodyType().getRenderer()
                .getModelRenderer(
                        vrPlayer,
                        slim ? VRBodyRenderer.MODEL_NAME_SLIM : VRBodyRenderer.MODEL_NAME_DEFAULT
                );
        if (mainHand) {
            bodyRenderer.renderRightHand(poseStack, buffer, packedLight, player);
        } else {
            bodyRenderer.renderLeftHand(poseStack, buffer, packedLight, player);
        }
        poseStack.popPose();
    }


    private static float swingWave(float progress) {
        return progress > 0.5F
                ? Mth.sin(progress * Mth.PI + Mth.PI)
                : Mth.sin(progress * 3.0F * Mth.PI);
    }

    private void applySwingPose(HandAction action,
                                PoseStack poseStack,
                                HumanoidArm arm,
                                float progress) {
        if (progress == 0.0F) {
            return;
        }
        switch (action) {
            case ATTACK -> {
                poseStack.translate(0.0F, 0.0F, 0.2F);
                poseStack.mulPose(Axis.XP.rotationDegrees(swingWave(progress) * 30.0F));
                poseStack.translate(0.0F, 0.0F, -0.2F);
            }
            case INTERACT -> {
                float side = arm == HumanoidArm.RIGHT ? -45.0F : 45.0F;
                poseStack.mulPose(Axis.ZP.rotationDegrees(side * swingWave(progress)));
            }
            case USE -> {
                float push = progress > 0.25F
                        ? Mth.sin(progress * Mth.HALF_PI + Mth.PI)
                        : Mth.sin(progress * Mth.TWO_PI);
                poseStack.translate(0.0F, 0.0F, -0.1F * (1.0F + push));
            }
        }
    }

    private void renderHandEffects(Collection<VRHandEffect> effects,
                                   HandType hand,
                                   VRRenderPass renderPass,
                                   PoseStack poseStack,
                                   boolean isGui,
                                   float partialTicks) {
        if (effects == null || effects.isEmpty()) return;

        effects.forEach(it->
                it.render(hand, renderPass, poseStack, isGui, partialTicks)
        );
    }


    private Collection<VRHandEffect> findActiveEffects(
            Collection<VRHandEffect> effects, VRDecorator decorator, HandType hand, boolean isSimple) {

        Collection<VRHandEffect> list = new ArrayList<>();
        for (VRHandEffect effect : effects) {
            if(!effect.isGlobal()
                    && !decorator.handEffects().contains(effect.getId())){
                continue;
            }
            if (effect.isEnabledAndVisible(decorator, hand, isSimple)) {
                list.add(effect);
            }
        }
        return list;
    }

    private boolean isTrackingHand(HandType hand) {
        return ClientContext.rawPoseHandler.getControllerData(hand)
                .isTracking();
    }

}