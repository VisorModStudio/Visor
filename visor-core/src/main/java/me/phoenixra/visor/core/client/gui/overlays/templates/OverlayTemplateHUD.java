package me.phoenixra.visor.core.client.gui.overlays.templates;



import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.overlay.template.RegisterOverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsLocation;


import me.phoenixra.visor.api.client.gui.overlay.template.framework.OverlayTemplateFrameBuffer;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;


import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterOverlayTemplate(id = OverlayTemplateHUD.ID)
public class OverlayTemplateHUD extends OverlayTemplateFrameBuffer implements VREventListener {
    public static final String ID = "hud";

    public OverlayTemplateHUD(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id);
        setEnabled(true);
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void enableHUD(AllowClientFeatureVREvent event){
        if(event.getFeature() == ClientFeature.GUI_DISABLE_HUD) {
            if(isVisible()){
                event.setCanceled(true);
            }
        }
    }

    @Override
    public void onRender(float partialTicks) {
        renderTarget = ClientContext.renderer.guiTarget.getTarget();
    }

    @Override
    public void onPreTick() {
        renderTarget = ClientContext.renderer.guiTarget.getTarget();
        super.onPreTick();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean updateVisibility() {
        return MC.screen == null
                && MC.player != null;
    }

    @Override
    protected @NotNull List<OverlayOptions> createOptions() {
        return List.of(
                new OverlayOptionsLocation(
                        this,
                        it->{
                            it.setTickModelView(true);
                            it.setAimRotation(false);
                            it.setPositionAnchor(PoseAnchor.HMD);
                            it.setFormulaPosX(null);
                            it.setFormulaPosY("-0.1");
                            it.setFormulaPosZ("-1.2");
                            it.setRotationAnchor(PoseAnchor.HMD);
                            it.setFormulaRotationX(null);
                            it.setFormulaRotationY(null);
                            it.setFormulaRotationZ(null);
                        }

                )
        );
    }
}
