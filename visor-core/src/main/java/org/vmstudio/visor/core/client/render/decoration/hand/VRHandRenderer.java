package org.vmstudio.visor.core.client.render.decoration.hand;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColorImmutable;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Unique;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.input.HandAction;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRHandEffect;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.core.client.render.player.VRPlayerRendererArms;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MirrorMode;
import org.vmstudio.visor.extensions.client.entity.EntityRenderDispatcherExtension;
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
import org.joml.Vector3fc;
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

    public void renderWorldHands(@NotNull VRDecorator decorator,
                                 @NotNull PoseStack poseStack,
                                 @NotNull HandRenderState handStateMain,
                                 @NotNull HandRenderState handStateOffhand,
                                 float partialTicks
    ) {
        renderHands(decorator, poseStack, handStateMain, handStateOffhand, partialTicks,false);
    }

    public void renderGuiHands(@NotNull VRDecorator decorator,
                               @NotNull PoseStack poseStack,
                               @NotNull HandRenderState handStateMain,
                               @NotNull HandRenderState handStateOffhand,
                               float partialTicks
    ) {
        renderHands(decorator, poseStack, handStateMain, handStateOffhand, partialTicks,true);
    }


    public void renderHands(@NotNull VRDecorator decorator,
                            @NotNull PoseStack poseStack,
                            @NotNull HandRenderState handStateMain,
                            @NotNull HandRenderState handStateOffhand,
                            float partialTicks,
                            boolean isGuiStage){
        RenderSystem.backupProjectionMatrix();

        ((GameRendererExtension) MC.gameRenderer).visor$resetProjectionMatrix(partialTicks);

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        Collection<VRHandEffect> effects = effectsRegistry.getComponentsMap().values();

        if(handStateMain == HandRenderState.GUI_HAND
                && isGuiStage){
            renderHand(
                    HandType.MAIN,
                    poseStack, partialTicks,
                    true,
                    renderPass, effects, decorator
            );
        } else if(handStateMain == HandRenderState.WORLD_HAND
                && !isGuiStage){
            renderHand(
                    HandType.MAIN,
                    poseStack, partialTicks,
                    false,
                    renderPass, effects, decorator
            );
        }

        if(handStateOffhand == HandRenderState.GUI_HAND
                && isGuiStage){
            renderHand(
                    HandType.OFFHAND,
                    poseStack, partialTicks,
                    true,
                    renderPass, effects, decorator
            );
        } else if(handStateOffhand == HandRenderState.WORLD_HAND
                && !isGuiStage){
            renderHand(
                    HandType.OFFHAND,
                    poseStack, partialTicks,
                    false,
                    renderPass, effects, decorator
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
                            @NotNull PoseStack poseStack,
                            float partialTicks,
                            boolean isGui,
                            VRRenderPass renderPass,
                            Collection<VRHandEffect> effects,
                            VRDecorator decorator) {

        poseStack.pushPose();

        poseStack.setIdentity();
        RenderPoseHelper.applyCameraOrientation(renderPass, poseStack);
        RenderPoseHelper.applyHandPose(hand, poseStack);

        var stageEffects = groupEffectsByStage(effects, decorator, hand, isGui);

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        renderHandEffects(
                stageEffects.get(VRHandEffect.RenderStage.BEFORE_HANDS),
                hand,
                renderPass,
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
                renderPass,
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
        RenderSystem.depthMask(false);

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
        RenderSystem.depthMask(true);

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

        renderWorldArmWithItem(
                MC.player, partialTicks,
                interactionHand,
                MC.player.getAttackAnim(partialTicks),
                item,  poseStack,
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


    private void renderWorldArmWithItem(AbstractClientPlayer pPlayer,
                                        float pPartialTicks,
                                        InteractionHand pHand,
                                        float pSwingProgress,
                                        ItemStack itemStack,
                                        PoseStack poseStack,
                                        MultiBufferSource pBuffer,
                                        int pCombinedLight
    ) {
        boolean mainHand = pHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = mainHand
                ? pPlayer.getMainArm()
                : pPlayer.getMainArm().getOpposite();
        var equipProgress = ((ItemInHandRendererExtension) MC.gameRenderer.itemInHandRenderer)
                .visor$getEquipProgress(pHand, pPartialTicks);


        boolean renderArm =
                VRRenderState.getRenderPass() != VRRenderPass.THIRD_PERSON
                        || (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY
                        && VRClientSettings.isMixedRealityRenderHands());

        poseStack.pushPose();
        if (renderArm && !pPlayer.isInvisible()) {
            renderWorldArm(
                    poseStack,
                    pBuffer,
                    pCombinedLight,
                    equipProgress,
                    pSwingProgress,
                    humanoidarm
            );
        }

        if (itemStack.isEmpty()) {
            poseStack.popPose();
            return;
        }

        if (pPlayer.swingingArm == pHand) {
            applySwingPose(
                    swingType,
                    poseStack,
                    humanoidarm,
                    pSwingProgress
            );
        }

        // Apply vanilla ItemInHandLayer base transforms
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean isLeftHand = humanoidarm == HumanoidArm.LEFT;
        poseStack.translate((float)(isLeftHand ? -1 : 1) / 16.0F, 0.125F, -0.625F);

        // Let addons modify item pose
        HandType handType = mainHand ? HandType.MAIN : HandType.OFFHAND;
        applyItemHandPose(pPlayer, handType, itemStack, poseStack, equipProgress, pPartialTicks);

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
            MC.gameRenderer.itemInHandRenderer.renderItem(
                    pPlayer,
                    itemStack,
                    ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                    false,
                    poseStack,
                    pBuffer,
                    pCombinedLight
            );
        }

        poseStack.popPose();
    }

    private void renderWorldArm(PoseStack poseStack,
                                MultiBufferSource multiBufferSource,
                                int i, float equipProgress, float swingProgress,
                                HumanoidArm humanoidArm
    ) {
        boolean mainHand = humanoidArm != HumanoidArm.LEFT;
        float handFactor = mainHand ? 1.0F : -1.0F;
        AbstractClientPlayer player = MC.player;
        RenderSystem.setShaderTexture(
                0,
                player.getSkinTextureLocation()
        );
        VRPlayerRendererArms rendererArms = ((EntityRenderDispatcherExtension) MC.getEntityRenderDispatcher())
                .visor$getArmSkinMap()
                .get(player.getModelName());

        poseStack.pushPose();

        boolean swingingArm = (player.swingingArm == InteractionHand.OFF_HAND
                && !mainHand)
                || (player.swingingArm == InteractionHand.MAIN_HAND
                && mainHand);
        if (swingingArm) {
            applySwingPose(swingType, poseStack, humanoidArm, swingProgress);
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);
        boolean slim = player.getModelName()
                .equals("slim");


        poseStack.translate(
                (slim ? -0.34375F : -0.375F) * handFactor,
                0.0F,
                slim ? 0.78125F : 0.75F
        );
        poseStack.mulPose(Axis.XP.rotationDegrees(-90));
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        if (mainHand) {
            rendererArms.renderRightHand(
                    poseStack, multiBufferSource,
                    i, player
            );
        } else {
            rendererArms.renderLeftHand(
                    poseStack, multiBufferSource,
                    i, player
            );
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
                                   boolean isSimple,
                                   float partialTicks) {
        if (effects == null || effects.isEmpty()) return;

        effects.forEach(it->
                it.render(hand, renderPass, poseStack, isSimple, partialTicks)
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