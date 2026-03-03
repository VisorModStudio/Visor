package org.vmstudio.visor.core.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.api.client.render.decoration.VRDecorationRenderer;
import org.vmstudio.visor.api.client.render.decoration.VRDecorator;
import org.vmstudio.visor.api.client.render.decoration.effects.VRGameEffect;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.decoration.hand.VRHandRenderer;
import org.vmstudio.visor.core.client.render.decoration.registry.DecoratorRegistry;
import org.vmstudio.visor.core.client.render.decoration.registry.VRGameEffectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.helpers.VREffectsHelper;
import org.vmstudio.visor.modified.client.render.GameRendererModified;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class DecorationRendererImpl implements VRDecorationRenderer {
    @Getter
    private final DecoratorRegistry registry;
    @Getter
    private final VRGameEffectRegistry effectsRegistry;

    @Getter
    private VRDecorator currentDecorator;

    public DecorationRendererImpl(){
        this.registry = new DecoratorRegistry();
        this.effectsRegistry = new VRGameEffectRegistry();

        ClientContext.handRenderer = new VRHandRenderer();

        //REGISTERING RENDERING PIPELINE
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_SOLID,
                (poseStack, partialTicks) -> {
                    if (VRRenderState.getPhase().isVanilla()) return;
                    renderAfterSolid(poseStack, partialTicks);
                }
        );
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_TRANSLUCENT,
                (poseStack, partialTicks) -> {
                    if (VRRenderState.getPhase().isVanilla()) return;
                    renderAfterTranslucent(poseStack, partialTicks);
                }
        );
        ModLoader.get().addToRenderPipeline(
                RenderPipelineStage.AFTER_WORLD,
                (poseStack, partialTicks) -> {
                    if (VRRenderState.getPhase().isVanilla()) return;
                    renderAfterWorld(poseStack, partialTicks);
                }
        );
    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;

        renderAfterSolid(poseStack, partialTicks);
        renderAfterTranslucent(poseStack, partialTicks);
        renderAfterWorld(poseStack, partialTicks);
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

    private void onDecoratorChanged(@NotNull VRDecorator newScene) {
        if(currentDecorator != null) {
            currentDecorator.clear();
        }
        newScene.init();
        currentDecorator = newScene;
    }


    private void renderAfterSolid(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;

        currentDecorator.setupRendering(poseStack, partialTicks);

        MC.gameRenderer.lightTexture().turnOffLightLayer();
        ClientContext.guiManager.renderDepthOverlays(poseStack, partialTicks);
        //WORLD HANDS
        if (currentDecorator.usesWorldHands()) {
            ClientContext.handRenderer.renderWorldHands(
                    currentDecorator,
                    poseStack, partialTicks,
                    true, true
            );
        }

        currentDecorator.renderAfterSolid(poseStack, partialTicks);

        GLUtils.checkGLError("post AFTER_SOLID stage");
    }

    private void renderAfterTranslucent(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;

        currentDecorator.renderAfterTranslucent(poseStack, partialTicks);

        GLUtils.checkGLError("post AFTER_TRANSLUCENT stage");
    }


    private void renderAfterWorld(PoseStack poseStack, float partialTicks) {
        if (currentDecorator == null) return;

        renderGameEffects(currentDecorator, poseStack, partialTicks);
        ClientContext.guiManager.renderHudOverlays(poseStack, partialTicks);
        ClientContext.handRenderer.renderCursor(poseStack, partialTicks);
        //GUI HANDS
        if (!currentDecorator.usesWorldHands()) {
            ClientContext.handRenderer.renderGuiHands(
                    currentDecorator,
                    poseStack, partialTicks,
                    true, true
            );
        }

        boolean insideBlock = ((GameRendererModified) MC.gameRenderer).visor$isInBlock();
        if (insideBlock && MC.level != null) {
            VREffectsHelper.renderInBlockEffect();
        }

        currentDecorator.renderAfterWorld(poseStack, partialTicks);

        GLUtils.checkGLError("post AFTER_WORLD stage");
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
                    VRRenderState.getCameraType(),
                    poseStack,
                    partialTick
            );
        }
    }

    @Override
    public @Nullable VRDecorator getDecorator(@NotNull String id) {
        return registry.getComponent(id);
    }


    public List<ComponentRegistry<?>> getComponentRegistries(){
        return List.of(
                registry,
                effectsRegistry,
                ClientContext.handRenderer.getItemPosesRegistry(),
                ClientContext.handRenderer.getEffectsRegistry()
        );
    }



}