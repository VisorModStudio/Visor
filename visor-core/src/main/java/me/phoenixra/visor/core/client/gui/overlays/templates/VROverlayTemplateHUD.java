package me.phoenixra.visor.core.client.gui.overlays.templates;



import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;


import me.phoenixra.visor.api.client.gui.overlays.framework.template.VROverlayTemplateFrameBuffer;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;


import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterVROverlayTemplate(
        id = VROverlayTemplateHUD.ID,
        name = VROverlayTemplateHUD.NAME,
        description = VROverlayTemplateHUD.DESCRIPTION,
        isCreateDefault = true
)
public class VROverlayTemplateHUD extends VROverlayTemplateFrameBuffer implements VREventListener {
    public static final String ID = "hud";
    public static final String NAME = "visor.overlay.template."+ID+".name";
    public static final String DESCRIPTION = "visor.overlay.template."+ID+".description";


    public VROverlayTemplateHUD(@NotNull VisorAddon owner,
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
    public boolean updateVisibility() {
        return MC.screen == null
                && MC.player != null;
    }

    @Override
    public boolean supportsVisibilityUpdateOnRender() {
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createTemplateOptions() {
        return List.of(
                new OverlayOptionsPose(
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
                            it.setFormulaScale("1.0");
                        }

                )
        );
    }
}
