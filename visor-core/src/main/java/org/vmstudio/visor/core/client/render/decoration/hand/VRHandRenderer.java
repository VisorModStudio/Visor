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
import org.spongepowered.asm.mixin.Unique;
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
import org.vmstudio.visor.core.client.utils.ModelUtils;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.decoration.registry.VRHandEffectRegistry;
import org.vmstudio.visor.core.client.render.decoration.registry.VRHandItemPoseRegistry;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.gui.VRCursorHandlerImpl;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.CoreShaders;
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
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;

import java.util.*;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtilsClient;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.lighting.LightEngine;


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

        // 1.21.2: getRenderOffset takes a PlayerRenderState we do not have here.
        // PlayerRenderer only adds a crouch offset on top of EntityRenderer's passenger
        // offset, so compute it from the entity directly.
        Vec3 offset = player.isCrouching()
                ? new Vec3(0.0, player.getScale() * -2.0F / 16.0, 0.0)
                : Vec3.ZERO;
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
            float lightPercent = light / LightEngine.MAX_LEVEL;
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
        RenderSystem.depthMask(false);
        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        if (MC.getOverlay() == null) {
            var whiteTex = TexturesHelper.getWhiteTexture();
            McVersionUtilsClient.bindTexture(whiteTex);
            RenderSystem.setShaderTexture(0, whiteTex);
        }

        // --- Render ---
        RenderHelper.renderCuboid(
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

        McVersionUtilsClient.bindTexture(TexturesHelper.getWhiteTexture());
        RenderSystem.setShaderTexture(
                0,
                TexturesHelper.getWhiteTexture()
        );

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(false);

        AtumColorImmutable color;

        Vector3fc dir = VRMathUtils.FORWARD_VECTOR;

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
            float lightPercent = light / (float) LightEngine.MAX_LEVEL;

            color = new AtumColorImmutable(
                    Mth.floor(GUI_HANDS_COLOR.getRedInt() * lightPercent),
                    Mth.floor(GUI_HANDS_COLOR.getGreenInt() * lightPercent),
                    Mth.floor(GUI_HANDS_COLOR.getBlueInt() * lightPercent),
                    255
            );
        }else{
            color = GUI_HANDS_COLOR;
        }

        RenderSystem.setShader(CoreShaders.POSITION_COLOR);

        RenderHelper.renderCuboid(
                poseStack.last().pose(),
                start, end,
                -0.02F, 0.02F,
                -0.0125F, 0.0125F,
                color
        );
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.depthMask(true);

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
                                        float pSwingProgress,
                                        ItemStack itemStack,
                                        PoseStack poseStack,
                                        MultiBufferSource pBuffer,
                                        int pCombinedLight,
                                        float pPartialTicks
    ) {
        boolean mainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = mainHand
                ? player.getMainArm()
                : player.getMainArm().getOpposite();
        var equipProgress = ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                .visor$getEquipProgress(hand, pPartialTicks);


        boolean renderArm =
                VRRenderState.getRenderPass() != VRRenderPass.THIRD_PERSON
                        || (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY
                        && VRClientSettings.isMixedRealityRenderHands());

        poseStack.pushPose();
        if (renderArm && !player.isInvisible() && !state.isWithItemOnly()) {
            renderWorldArm(
                    player,
                    vrPlayer,
                    poseStack,
                    pBuffer,
                    pCombinedLight,
                    equipProgress,
                    pSwingProgress,
                    humanoidarm
            );
        }

        if (itemStack.isEmpty() || !state.isWithItem()) {
            poseStack.popPose();
            return;
        }

        if (player.swingingArm == hand) {
            applySwingPose(
                    swingType,
                    poseStack,
                    humanoidarm,
                    pSwingProgress
            );
        }


        float entityScale = 0.9375F;
        float armsScale = VRClientSettings.getPlayerModelArmsScale();
        poseStack.scale(entityScale, entityScale, entityScale);

        ModelUtils.controllerToModelOrientation(poseStack);

        //match ItemInHandLayerMixin pose
        poseStack.scale(armsScale, 1.0F, armsScale);
        poseStack.translate(0.0F, 0.65F, 0.0F);
        poseStack.scale(1.0F, armsScale, 1.0F);
        poseStack.translate(0.0F, -0.65F, 0.0F);

        boolean isLeftHand = humanoidarm == HumanoidArm.LEFT;
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(
                0,
                0.125F,
                0.625F
        );

        HandType handType = mainHand ? HandType.MAIN : HandType.OFFHAND;
        applyItemHandPose(player, handType, itemStack, poseStack, equipProgress, pPartialTicks);

        if (itemStack.getItem() instanceof MapItem) {
            RenderSystem.disableCull();
            ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                    .visor$renderMap(
                            poseStack,
                            pBuffer,
                            pCombinedLight,
                            itemStack
                    );
        } else {
            ItemDisplayContext displayCtx = isLeftHand
                    ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            MC.gameRenderer.itemInHandRenderer.renderItem(
                    player, itemStack, displayCtx, isLeftHand,
                    poseStack, pBuffer, pCombinedLight
            );
        }

        poseStack.popPose();
    }

    private void renderWorldArm(AbstractClientPlayer player,
                                VRClientPlayer vrPlayer,
                                PoseStack poseStack,
                                MultiBufferSource multiBufferSource,
                                int i, float equipProgress, float swingProgress,
                                HumanoidArm humanoidArm
    ) {
        boolean mainHand = humanoidArm != HumanoidArm.LEFT;
        float handFactor = mainHand ? 1.0F : -1.0F;
        RenderSystem.setShaderTexture(0, player.getSkin().texture());


        poseStack.pushPose();

        boolean swingingArm = (player.swingingArm == InteractionHand.OFF_HAND
                && !mainHand)
                || (player.swingingArm == InteractionHand.MAIN_HAND
                && mainHand);
        if (swingingArm) {
            applySwingPose(swingType, poseStack, humanoidArm, swingProgress);
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);

        boolean slim = "slim".equals(player.getSkin().model().id());

        poseStack.translate(
                (slim ? -0.34375F : -0.375F) * handFactor,
                0.0F,
                slim ? 0.78125F : 0.75F
        );
        ModelUtils.controllerToModelOrientation(poseStack);

        var bodyRenderer = vrPlayer.getBodyType().getRenderer()
                .getModelRenderer(
                        vrPlayer,
                        slim ? VRBodyRenderer.MODEL_NAME_SLIM : VRBodyRenderer.MODEL_NAME_DEFAULT
                );
        // 1.21.2: the hand renderers take the resolved skin and sleeve flag
        // instead of the player, matching vanilla's ItemInHandRenderer.
        ResourceLocation skinTexture = player.getSkin().texture();
        if (mainHand) {
            bodyRenderer.renderRightHand(poseStack, multiBufferSource, i, skinTexture,
                    player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            bodyRenderer.renderLeftHand(poseStack, multiBufferSource, i, skinTexture,
                    player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
        poseStack.popPose();
    }

    @Unique
    private void applySwingPose(HandAction handAction,
                                PoseStack matrixStackIn,
                                HumanoidArm hand,
                                float swingProgress) {
        if (swingProgress == 0.0F) {
            return;
        }
        switch (handAction) {
            case ATTACK ->{
                float swingAngle;
                if ((double) swingProgress > 0.5D) {
                    swingAngle = Mth.sin(
                            (float) ((double) swingProgress * Math.PI + Math.PI)
                    );
                } else {
                    swingAngle = Mth.sin(
                            (float) ((double) (swingProgress * 3.0F) * Math.PI)
                    );
                }

                matrixStackIn.translate(
                        0.0D, 0.0D, 0.2F
                );
                matrixStackIn.mulPose(
                        Axis.XP.rotationDegrees(
                                swingAngle * 30.0F
                        )
                );
                matrixStackIn.translate(
                        0.0D, 0.0D, -0.2F
                );
            }
            case INTERACT -> {
                float swingAngle;

                if ((double) swingProgress > 0.5D) {
                    swingAngle = Mth.sin(
                            (float) ((double) swingProgress * Math.PI + Math.PI)
                    );
                } else {
                    swingAngle = Mth.sin(
                            (float) ((double) (swingProgress * 3.0F) * Math.PI)
                    );
                }

                matrixStackIn.mulPose(
                        Axis.ZP.rotationDegrees(
                                (float) (hand == HumanoidArm.RIGHT ? -1 : 1) * swingAngle * 45.0F
                        )
                );
            }
            case USE -> {
                float swingOffset;

                if ((double) swingProgress > 0.25D) {
                    swingOffset = Mth.sin((float) ((double) (swingProgress / 2.0F) * Math.PI + Math.PI));
                } else {
                    swingOffset = Mth.sin(
                            (float) ((double) (swingProgress * 2.0F) * Math.PI)
                    );
                }
                matrixStackIn.translate(
                        0.0D,
                        0.0D,
                        -(1.0F + swingOffset) * 0.1F
                );
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