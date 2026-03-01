package org.vmstudio.visor.core.client.render;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRCameraType;
import org.vmstudio.visor.modified.client.WindowModified;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MirrorMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRRenderState {

    @Getter @NotNull
    private static RenderPhase phase = RenderPhase.VANILLA;

    @Getter @Nullable
    private static VRCameraType cameraType = null;



    @Getter
    private static MainTarget vanillaTarget = null;


    public static void initVanillaTarget(MainTarget target){
        if(vanillaTarget != null){
            throw new RuntimeException("Vanilla target already has been initialized!");
        }
        vanillaTarget = target;
    }


    public static void startVanillaPhase() {
        phase = RenderPhase.VANILLA;
        cameraType = null;
        MC.mainRenderTarget = vanillaTarget;
    }

    public static void startVRGuiPhase() {
        phase = RenderPhase.VR_GUI;
        cameraType = VRCameraType.GUI;
        MC.mainRenderTarget = getTargetForCamera(VRCameraType.GUI);
    }

    public static void startVRWorldPhase(@NotNull VRCameraType cameraType) {
        if(!cameraType.isWorld()){
            throw new RuntimeException(
                    "Tried to start VR_WORLD phase " +
                            "for camera type that is not rendering world: "+cameraType
            );
        }
        phase = RenderPhase.VR_WORLD;
        VRRenderState.cameraType = cameraType;
        MC.mainRenderTarget = getTargetForCamera(cameraType);
    }

    public static void startVRMirrorPhase(){
        phase = RenderPhase.VR_MIRROR;
        cameraType = null;
        MC.mainRenderTarget = ClientContext.renderer.mainTarget.getMirrorTarget();
    }

    public static RenderTarget getTargetForCamera(VRCameraType cameraType){
        if(VisorState.get().isNotInitialized()
                || cameraType == null){
            return vanillaTarget;
        }
        return switch (cameraType){
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
                || MC.screen instanceof ReceivingLevelScreen
                || MC.screen instanceof ProgressScreen
                || MC.screen instanceof GenericDirtMessageScreen
                || MC.getOverlay() != null;
    }


    public static List<VRCameraType> getActiveCameraTypes() {


        List<VRCameraType> list = new ArrayList<>();
        list.add(VRCameraType.EYE_LEFT);
        list.add(VRCameraType.EYE_RIGHT);

        var windowModif =  ((WindowModified) (Object)
                Minecraft.getInstance().getWindow());

        if (windowModif.visor$getActualScreenWidth() > 0
                && windowModif.visor$getActualScreenHeight() > 0) {
            MirrorMode mirrorMode = VRClientSettings.getMirrorMode();
            if (mirrorMode == MirrorMode.FIRST_PERSON) {
                list.add(VRCameraType.FIRST_PERSON);
            } else if (mirrorMode == MirrorMode.THIRD_PERSON) {
                list.add(VRCameraType.THIRD_PERSON);
            } else if (mirrorMode == MirrorMode.MIXED_REALITY) {
                if (VRClientSettings.isMixedRealityWithFirstPerson() && VRClientSettings.isMixedRealityAsGrid2x2()) {
                    list.add(VRCameraType.FIRST_PERSON);
                }

                list.add(VRCameraType.THIRD_PERSON);
            }
        }

        return list;
    }
}
