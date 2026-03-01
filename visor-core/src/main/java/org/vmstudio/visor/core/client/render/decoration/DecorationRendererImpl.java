package org.vmstudio.visor.core.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.atumvr.api.utils.GLUtils;
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

import java.util.List;


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

    }


    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        if(currentDecorator != null) {
            currentDecorator.render(poseStack, partialTicks);
            GLUtils.checkGLError("post vr decoration");
        }
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


    public void renderGameEffects(VRDecorator decorator,
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

}
