package me.phoenixra.visor.core.client.render.gameview;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.visor.api.client.render.gameview.VRGameView;
import me.phoenixra.visor.api.client.render.gameview.IVRGameViewHandler;
import me.phoenixra.visor.api.common.addon.VRElementRegistry;
import me.phoenixra.visor.core.client.render.gameview.hand.VRHandRenderer;
import me.phoenixra.visor.core.client.render.gameview.registry.VRGameViewRegistry;
import me.phoenixra.visor.core.client.render.VRRenderState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.phoenixra.visor.core.client.ClientContext;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClient.MC;

public class VRGameViewHandler implements IVRGameViewHandler {
    @Getter
    private final VRGameViewRegistry registry;


    @Getter
    private VRGameView currentView;
    @Getter
    private VRGameView emptyView;

    public VRGameViewHandler(){
        this.registry = new VRGameViewRegistry();
        ClientContext.handRenderer = new VRHandRenderer();

    }

    public void initView(VRGameView emptyView){
        this.emptyView = emptyView;
        currentView = emptyView;
    }


    @Override
    public void renderView(PoseStack poseStack, float partialTicks) {
        currentView.render(poseStack, partialTicks);
    }

    @Override
    public void tick() {
        VRGameView newScene;
        if(VRRenderState.isInMainMenu()){
            newScene = getView(VIEW_MAIN_MENU);
        }else if(MC.screen != null){
            newScene = getView(VIEW_INGAME_SCREEN);
        }else{
            newScene = getView(VIEW_INGAME);
        }
        if(newScene != currentView){
            onSceneChanged(newScene);
        }


        currentView.tick();
    }

    private void onSceneChanged(@NotNull VRGameView newScene) {
        currentView.onExit();
        newScene.onEnter();
        currentView = newScene;
    }

    @Override
    public @Nullable VRGameView getView(@NotNull String id) {
        return registry.getAddonComponent(id);
    }


    public List<VRElementRegistry<?>> getElementRegistries(){
        return List.of(
                registry,
                ClientContext.handRenderer.getHandItemPosesRegistry()
        );
    }

}
