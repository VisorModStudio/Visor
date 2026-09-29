package org.vmstudio.visor.core.client.render.decoration;

import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import com.mojang.blaze3d.vertex.PoseStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McModelViewStack;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderStateHelper;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.render.HandRenderStateVREvent;
import org.vmstudio.visor.api.client.events.render.RenderPipelineStageVREvent;
import org.vmstudio.visor.api.client.render.RenderPipelineCallback;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.client.render.decoration.VRDecorationRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.client.render.decoration.hand.HandRenderState;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.decoration.hand.VRHandRenderer;
import org.vmstudio.visor.core.client.render.decoration.registry.DecoratorRegistry;
import org.vmstudio.visor.core.client.render.decoration.registry.VRBodyTypeRegistry;
import org.vmstudio.visor.core.client.render.decoration.registry.VRGameEffectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.vmstudio.visor.compatibility.ShaderCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.helpers.RenderEffectsHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import org.vmstudio.visor.core.client.render.camera.VRCameraOverlaps;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class DecorationRendererImpl implements VRDecorationRenderer {
    @Getter
    private final DecoratorRegistry registry;
    @Getter
    private final VRGameEffectRegistry effectsRegistry;
    @Getter
    private final VRBodyTypeRegistry vrBodyTypeRegistry;


    @Getter
    private VRDecorator currentDecorator;


    private HandRenderState handStateMain = HandRenderState.OFF;
    private HandRenderState handStateOffhand = HandRenderState.OFF;

    public DecorationRendererImpl(){
        this.registry = new DecoratorRegistry();
        this.vrBodyTypeRegistry = new VRBodyTypeRegistry();
        this.effectsRegistry = new VRGameEffectRegistry();

        ClientContext.handRenderer = new VRHandRenderer();

        //REGISTERING RENDERING PIPELINE
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_SOLID,
                (poseStack, partialTicks) -> runPipelineStage(
                        RenderPipelineStage.AFTER_SOLID, this::renderAfterSolid, poseStack, partialTicks
                )
        );
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_TRANSLUCENT,
                (poseStack, partialTicks) -> runPipelineStage(
                        RenderPipelineStage.AFTER_TRANSLUCENT, this::renderAfterTranslucent, poseStack, partialTicks
                )
        );
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_WORLD,
                (poseStack, partialTicks) -> runPipelineStage(
                        RenderPipelineStage.AFTER_WORLD, this::renderAfterWorld, poseStack, partialTicks
                )
        );
    }

    private void runPipelineStage(RenderPipelineStage stage,
                                  RenderPipelineCallback visorStage,
                                  PoseStack poseStack,
                                  float partialTicks) {
        if (VRRenderState.getPhase().isVanilla()) {
            callStageEvent(stage, poseStack, partialTicks);
            return;
        }
        // drain GL errors caused outside VR, probably mods
        RenderStateHelper.drainExternalGLErrors(stage.name());
        runLevelStage(() -> {
            visorStage.render(poseStack, partialTicks);
            callStageEvent(stage, poseStack, partialTicks);
            GLUtils.checkGLError("post " + stage.name() + " events stage");
        });
    }

    private void runStage(Runnable stage) {
        //? if >=1.20.5 {
        if (VRRenderState.getPhase().isVanilla()) {
            stage.run();
            return;
        }
        //? if >=1.21.9 {
        MC.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        //?}
        MC.renderBuffers().bufferSource().endBatch();
        McModelViewStack.push();
        McModelViewStack.identity();
        McModelViewStack.apply();
        try {
            stage.run();
        } finally {
            McModelViewStack.pop();
            McModelViewStack.apply();
        }
        //?} else {
        /*stage.run();
        *///?}
    }

    private void runLevelStage(Runnable stage) {
        if (VRRenderState.getPhase().isVanilla()) {
            stage.run();
            return;
        }
        runStage(() -> {
            RenderPoseHelper.setupEyeSpaceLevelLights(VRRenderState.getRenderPass());
            try {
                stage.run();
            } finally {
                RenderPoseHelper.restoreLevelLights();
            }
        });
    }

    @Override
    public void renderMainMenu(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;

        // drain GL errors caused outside VR, probably mods
        RenderStateHelper.drainExternalGLErrors("VR main menu");
        runLevelStage(() -> {
            renderAfterSolid(poseStack, partialTicks);
            callStageEvent(RenderPipelineStage.AFTER_SOLID, poseStack, partialTicks);

            renderAfterTranslucent(poseStack, partialTicks);
            callStageEvent(RenderPipelineStage.AFTER_TRANSLUCENT, poseStack, partialTicks);

            renderAfterWorld(poseStack, partialTicks);
            callStageEvent(RenderPipelineStage.AFTER_WORLD, poseStack, partialTicks);
        });
    }

    private void callStageEvent(RenderPipelineStage stage,
                                PoseStack poseStack,
                                float partialTicks) {
        VisorAPI.eventBus().callEvent(new RenderPipelineStageVREvent(
                stage,
                VRRenderState.getPhase(),
                VRRenderState.getRenderPass(),
                poseStack,
                partialTicks
        ));
    }

    @Override
    public void tick() {
        VRDecorator newScene = null;
        for(var entry : registry.getSortedComponents()){
            if(entry.isEnabledAndCanActivate()){
                newScene = entry;
                break;
            }
        }

        if(newScene != null
                && newScene != currentDecorator){
            onDecoratorChanged(newScene);
        }


        if(currentDecorator != null) {
            currentDecorator.tick();
        }
    }



    public void updateRenderState(){
        if (currentDecorator == null) return;
        if(currentDecorator.isFullControl()){
            currentDecorator.updateRenderState();
            return;
        }

        //don't render world hands in third person
        if(VRRenderState.getRenderPass() == VRRenderPass.THIRD_PERSON){
            if(VRClientSettings.getMirrorMode() != MirrorMode.MIXED_REALITY){
                handStateMain = HandRenderState.OFF;
                handStateOffhand = HandRenderState.OFF;
                return;
            }
        }

        var cursorHandler = ClientContext.cursorHandler;

        if(!ClientContext.rawPoseHandler.getControllerData(HandType.MAIN)
                .isTracking()){
            handStateMain = HandRenderState.OFF;
        }else{
            boolean isCursorHand = cursorHandler.isHandFocused(HandType.MAIN)
                    && (cursorHandler.getCursorHand() == HandType.MAIN
                    || cursorHandler.isTwoHandedCursor());
            boolean isGuiHand = !currentDecorator.supportsWorldHands()
                    || isCursorHand
                    || (MC.player != null && MC.player.isSpectator());
            handStateMain = isGuiHand
                    ? HandRenderState.GUI_HAND
                    : HandRenderState.WORLD_HAND;

            var event = new HandRenderStateVREvent(
                    HandType.MAIN, handStateMain
            );
            VisorAPI.eventBus().callEvent(event);
            handStateMain = event.getState();

        }


        if(!ClientContext.rawPoseHandler.getControllerData(HandType.OFFHAND)
                .isTracking()){
            handStateOffhand = HandRenderState.OFF;
        }else{
            boolean isCursorHand = cursorHandler.isHandFocused(HandType.OFFHAND)
                    && (cursorHandler.getCursorHand() == HandType.OFFHAND
                    || cursorHandler.isTwoHandedCursor());
            boolean isGuiHand = !currentDecorator.supportsWorldHands()
                    || isCursorHand
                    || (MC.player != null && MC.player.isSpectator());
            handStateOffhand = isGuiHand
                    ? HandRenderState.GUI_HAND
                    : HandRenderState.WORLD_HAND;

            var event = new HandRenderStateVREvent(
                    HandType.OFFHAND, handStateOffhand
            );
            VisorAPI.eventBus().callEvent(event);
            handStateOffhand = event.getState();

        }
        currentDecorator.updateRenderState();
    }

    private void onDecoratorChanged(@NotNull VRDecorator newScene) {
        if(currentDecorator != null) {
            currentDecorator.clear();
        }
        newScene.init();
        currentDecorator = newScene;
    }


    private void renderAfterSolid(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;
        if(currentDecorator.isFullControl()){
            currentDecorator.setupRendering(poseStack, partialTicks);
            currentDecorator.renderAfterSolid(poseStack, partialTicks);
            return;
        }

        currentDecorator.setupRendering(poseStack, partialTicks);

        McGlState.turnOffLightLayer();
        if (!ShaderCompatHelper.isShaderActive()) {
            ClientContext.guiManager.renderDepthOverlays(poseStack, partialTicks);
        }
        //VR BODY
        ClientContext.localPlayer
                .getBodyType()
                .getRenderer()
                .renderDecoration(currentDecorator, poseStack, partialTicks);
        ClientContext.handRenderer.renderHandEffectsOnly(
                currentDecorator,
                poseStack,
                handStateMain, handStateOffhand,
                false,
                partialTicks
        );
        currentDecorator.renderAfterSolid(poseStack, partialTicks);

        GLUtils.checkGLError("post AFTER_SOLID stage");
    }

    private void renderAfterTranslucent(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;
        if(currentDecorator.isFullControl()){
            currentDecorator.renderAfterTranslucent(poseStack, partialTicks);
            return;
        }

        currentDecorator.renderAfterTranslucent(poseStack, partialTicks);

        GLUtils.checkGLError("post AFTER_TRANSLUCENT stage");
    }


    private void renderAfterWorld(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;

        // a foreign AFTER_LEVEL listener can leave the default framebuffer bound
        // (RenderTarget.copyDepthFrom does), iris rebinds the eye target itself
        if (!ShaderCompatHelper.isShaderActive()) {
            McRenderTarget.bindWrite(McRenderTarget.mainTarget());
        }

        if(currentDecorator.isFullControl()){
            currentDecorator.renderAfterWorld(poseStack, partialTicks);
            return;
        }

        // In-block dimming
        if (MC.level != null) {
            if (VRRenderState.getRenderPass().isFirstPerson()) {
                //First person passes
                float proximity = VRCameraOverlaps.getBlockProximity();
                if (proximity > 0.0f) {
                    RenderEffectsHelper.renderInBlockVignette(proximity);
                }
            } else if (VRCameraOverlaps.isInBlock()) {
                // Third person passes
                RenderEffectsHelper.renderInBlockEffect();
            }
        }

        renderGameEffects(currentDecorator, poseStack, partialTicks);
        currentDecorator.renderAfterWorld(poseStack, partialTicks);

        GLUtils.checkGLError("post AFTER_WORLD stage");
    }

    public void renderAfterPostProcessing(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;
        if (currentDecorator.isFullControl()) {
            currentDecorator.renderAfterPostProcessing(poseStack, partialTicks);
            return;
        }
        ClientContext.guiManager.renderHudOverlays(poseStack, partialTicks);
        ClientContext.handRenderer.renderCursor(poseStack, partialTicks);
        ClientContext.handRenderer.renderGuiHands(
                poseStack,
                handStateMain, handStateOffhand,
                partialTicks
        );
        ClientContext.handRenderer.renderHandEffectsOnly(
                currentDecorator,
                poseStack,
                handStateMain, handStateOffhand,
                true,
                partialTicks
        );

        currentDecorator.renderAfterPostProcessing(poseStack, partialTicks);
        GLUtils.checkGLError("post AFTER_POST_PROCESSING stage");
    }

    public void renderShaderUi(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null || currentDecorator.isFullControl()) {
            return;
        }
        ClientContext.guiManager.renderDepthOverlays(poseStack, partialTicks);
        GLUtils.checkGLError("post shader UI stage");
    }

    private void renderGameEffects(VRDecorator decorator,
                                   PoseStack poseStack,
                                   float partialTick) {
        VRDecorator currentDecorator = ClientContext.decorationRenderer.getCurrentDecorator();
        for (VRGameEffect effect : effectsRegistry.getComponentsMap().values()) {
            if(!effect.isGlobal()
                    && !decorator.gameEffects().contains(effect.getId())){
                continue;
            }
            if (!effect.isEnabledAndVisible(currentDecorator)) continue;

            effect.render(
                    VRRenderState.getRenderPass(),
                    poseStack,
                    partialTick
            );
        }
    }

    @Override
    public @NotNull HandRenderState getHandState(@NotNull HandType handType) {
        return handType == HandType.MAIN
                ? handStateMain : handStateOffhand;
    }

    @Override
    public @Nullable VRDecorator getDecorator(@NotNull String id) {
        return registry.getComponent(id);
    }


    public List<ComponentRegistry<?>> getComponentRegistries(){
        return List.of(
                registry,
                vrBodyTypeRegistry,
                effectsRegistry,
                ClientContext.handRenderer.getItemPosesRegistry(),
                ClientContext.handRenderer.getEffectsRegistry()
        );
    }



}