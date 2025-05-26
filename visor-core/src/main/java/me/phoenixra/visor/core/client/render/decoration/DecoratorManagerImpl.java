package me.phoenixra.visor.core.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.visor.api.client.render.decoration.VRDecoratorManager;
import me.phoenixra.visor.api.client.render.decoration.VRDecorator;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.render.decoration.decorators.DecoratorEmpty;
import me.phoenixra.visor.core.client.render.decoration.hand.VRHandRendererImpl;
import me.phoenixra.visor.core.client.render.decoration.registry.DecoratorRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.List;


public class DecoratorManagerImpl implements VRDecoratorManager {
    @Getter
    private final DecoratorRegistry registry;


    @Getter
    private VRDecorator currentDecorator;

    public DecoratorManagerImpl(){
        this.registry = new DecoratorRegistry();
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
        for(var entry : registry.getSortedDecorators()){
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
        return registry.getAddonComponent(id);
    }


    public List<VisorElementRegistry<?>> getElementRegistries(){
        return List.of(
                registry,
                ClientContext.handRenderer.getHandItemPosesRegistry()
        );
    }

}
