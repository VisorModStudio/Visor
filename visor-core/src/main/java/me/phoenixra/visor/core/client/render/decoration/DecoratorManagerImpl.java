package me.phoenixra.visor.core.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.visor.api.client.render.decoration.VRDecoratorManager;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.client.render.decoration.effects.view.VRGameEffect;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.decoration.hand.VRHandRendererImpl;
import me.phoenixra.visor.core.client.render.decoration.registry.DecoratorRegistry;
import me.phoenixra.visor.core.client.render.decoration.registry.VRGameEffectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.List;


public class DecoratorManagerImpl implements VRDecoratorManager {
    @Getter
    private final DecoratorRegistry registry;
    @Getter
    private final VRGameEffectRegistry effectsRegistry;

    @Getter
    private VRDecorator currentDecorator;

    public DecoratorManagerImpl(){
        this.registry = new DecoratorRegistry();
        this.effectsRegistry = new VRGameEffectRegistry();

        ClientContext.handRenderer = new VRHandRendererImpl();

    }


    @Override
    public void render(PoseStack poseStack, float partialTicks) {
        if(currentDecorator != null) {
            currentDecorator.render(poseStack, partialTicks);
        }
    }

    @Override
    public void tick() {
        VRDecorator newScene = null;
        for(var entry : registry.getSortedElements()){
            if(entry.isDisplayable()){
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
            currentDecorator.onExit();
        }
        newScene.onStart();
        currentDecorator = newScene;
    }

    @Override
    public @Nullable VRDecorator getDecorator(@NotNull String id) {
        return registry.getElement(id);
    }


    public List<VisorElementRegistry<?>> getElementRegistries(){
        return List.of(
                registry,
                effectsRegistry,
                ClientContext.handRenderer.getItemPosesRegistry(),
                ClientContext.handRenderer.getEffectsRegistry()
        );
    }


    public void renderGameEffects(PoseStack poseStack,
                                  float partialTick) {
        for (VRGameEffect effect : effectsRegistry.getElementsMap().values()) {
            if (!effect.isEnabled()) continue;
            if (!effect.isVisible()) continue;

            effect.render(
                    VRRenderState.getCurrentVRDisplay(),
                    poseStack,
                    partialTick
            );
        }
    }

}
