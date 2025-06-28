package me.phoenixra.visor.core.client.render;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.api.client.render.RenderPhase;
import me.phoenixra.visor.api.client.render.VRDisplay;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.gui.screens.WinScreen;
import org.jetbrains.annotations.NotNull;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRRenderState {

    @Getter
    private static RenderPhase currentPhase = RenderPhase.VANILLA;

    @Getter
    private static VRDisplay currentVRDisplay = null;



    @Getter
    private static MainTarget vanillaTarget = null;


    public static void initVanillaTarget(MainTarget target){
        if(vanillaTarget != null){
            throw new RuntimeException("Vanilla target already has been initialized!");
        }
        vanillaTarget = target;
    }


    public static void startVanillaPhase() {
        currentPhase = RenderPhase.VANILLA;
        currentVRDisplay = null;
        MC.mainRenderTarget = vanillaTarget;
    }

    public static void startVRGuiPhase() {
        currentPhase = RenderPhase.VR_GUI;
        currentVRDisplay = VRDisplay.GUI;
        MC.mainRenderTarget = getTargetForDisplay(VRDisplay.GUI);
    }

    public static void startVRWorldPhase(@NotNull VRDisplay display) {
        if(!display.isWorld()){
            throw new RuntimeException(
                    "Tried to start VR_WORLD phase " +
                            "for display not rendering world: "+display
            );
        }
        currentPhase = RenderPhase.VR_WORLD;
        currentVRDisplay = display;
        MC.mainRenderTarget = getTargetForDisplay(display);
    }

    public static void startVRMirrorPhase(){
        currentPhase = RenderPhase.VR_MIRROR;
        currentVRDisplay = null;
        MC.mainRenderTarget = ClientContext.renderer.mainTarget.getMirrorTarget();
    }

    public static RenderTarget getTargetForDisplay(VRDisplay display){
        if(VisorState.getState().isNotInitialized()
                || display == null){
            return vanillaTarget;
        }
        return switch (display){
            case GUI ->
                    ClientContext.renderer.guiTarget.getTarget();
            case EYE_LEFT, EYE_RIGHT ->
                    ClientContext.renderer.mainTarget.getTarget();
            case FIRST_PERSON ->
                    ClientContext.renderer.firstPersonTarget.getTarget();
            case THIRD_PERSON ->
                    ClientContext.renderer.thirdPersonTarget.getTarget();
        };
    }

    public static boolean isInMainMenu(){
        if(MC == null){
            return false;
        }
        return MC.level == null
                || MC.screen instanceof WinScreen
                || MC.screen instanceof ReceivingLevelScreen
                || MC.screen instanceof ProgressScreen
                || MC.screen instanceof GenericDirtMessageScreen
                || MC.getOverlay() != null;
    }
}
