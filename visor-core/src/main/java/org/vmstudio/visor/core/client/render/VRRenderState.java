package org.vmstudio.visor.core.client.render;

import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.api.client.render.RenderPhase;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MirrorMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRRenderState {

    @Getter @NotNull
    private static RenderPhase phase = RenderPhase.VANILLA;

    @Getter
    private static VRRenderPass renderPass = VRRenderPass.NULL;



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
        renderPass = VRRenderPass.NULL;
        MC.mainRenderTarget = vanillaTarget;
    }

    public static void startVRGuiPhase() {
        phase = RenderPhase.VR_GUI;
        renderPass = VRRenderPass.GUI;
        MC.mainRenderTarget = getTargetForPass(VRRenderPass.GUI);
    }

    public static void startVRWorldPhase(@NotNull VRRenderPass renderPass) {
        if(!renderPass.isWorld()){
            throw new RuntimeException(
                    "Tried to start VR_WORLD phase " +
                            "for render pass that is not rendering world: "+renderPass
            );
        }
        phase = RenderPhase.VR_WORLD;
        VRRenderState.renderPass = renderPass;
        MC.mainRenderTarget = getTargetForPass(renderPass);
    }

    public static void startVRMirrorPhase(){
        phase = RenderPhase.VR_MIRROR;
        renderPass = VRRenderPass.NULL;
        MC.mainRenderTarget = ClientContext.renderer.mainTarget.getMirrorTarget();
    }

    public static RenderTarget getTargetForPass(VRRenderPass renderPass){
        if(VisorState.get().isNotInitialized()
                || renderPass == null){
            return vanillaTarget;
        }
        return switch (renderPass){
            case NULL -> vanillaTarget;
            case GUI ->
                    ClientContext.renderer.guiTarget.getTarget();
            case EYE_LEFT, EYE_RIGHT ->
                    ClientContext.renderer.mainTarget.getTarget();
            case CENTER ->
                    ClientContext.renderer.firstPersonTarget.getTarget();
            case THIRD_PERSON ->
                    ClientContext.renderer.thirdPersonTarget.getTarget();
        };
    }

    public static boolean isSelfModelRender(Entity entity) {
        return canRenderSelfModel(entity) && isSelfModelAllowed();
    }

    public static boolean isSelfModelRenderCamera() {
        if(!canRenderSelfModel(MC.getCameraEntity())){
            return false;
        }
        return isSelfModelAllowed()
                || renderPass.isThirdPerson();
    }
    public static boolean isSelfModelPlayer(Entity entity) {
        if(VisorState.get().isNotActive()){
            return false;
        }
        if(entity == null){
            return false;
        }
        return entity == MC.player
                && entity == MC.getCameraEntity();
    }

    private static boolean canRenderSelfModel(Entity entity) {
        if(VisorState.get().isNotActive()){
            return false;
        }
        if(entity != MC.player
                || entity != MC.getCameraEntity()){
            return false;
        }
        if(entity == null){
            return false;
        }
        return !((LivingEntity) entity).isSleeping();
    }

    private static boolean isSelfModelAllowed() {
        return ClientContext.localPlayer.getBodyType().isSelfModelVisible()
                && renderPass.isFirstPerson();
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


    public static List<VRRenderPass> getActivePasses() {


        List<VRRenderPass> list = new ArrayList<>();
        list.add(VRRenderPass.EYE_LEFT);
        list.add(VRRenderPass.EYE_RIGHT);

        var windowModif =  ((WindowExtension) (Object)
                Minecraft.getInstance().getWindow());

        if (windowModif.visor$getActualScreenWidth() > 0
                && windowModif.visor$getActualScreenHeight() > 0) {
            MirrorMode mirrorMode = VRClientSettings.getMirrorMode();
            if (mirrorMode == MirrorMode.FIRST_PERSON) {
                list.add(VRRenderPass.CENTER);
            } else if (mirrorMode == MirrorMode.THIRD_PERSON) {
                list.add(VRRenderPass.THIRD_PERSON);
            } else if (mirrorMode == MirrorMode.MIXED_REALITY) {
                if (VRClientSettings.isMixedRealityWithFirstPerson() && VRClientSettings.isMixedRealityAsGrid2x2()) {
                    list.add(VRRenderPass.CENTER);
                }

                list.add(VRRenderPass.THIRD_PERSON);
            }
        }

        return list;
    }
}
