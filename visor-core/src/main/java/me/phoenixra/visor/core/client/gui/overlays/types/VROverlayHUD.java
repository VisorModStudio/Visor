package me.phoenixra.visor.core.client.gui.overlays.types;



import me.phoenixra.visor.api.client.gui.overlay.ModelViewAnchor;
import me.phoenixra.visor.api.client.gui.overlay.RegisterOverlayType;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;


import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;


import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@RegisterOverlayType(id = VROverlayHUD.ID_TYPE)
public class VROverlayHUD extends VROverlayFrameBuffer {
    public static final String ID_TYPE = "hud";

    public VROverlayHUD(@NotNull VisorAddon owner,
                        @NotNull String id) {
        super(owner, id, null); //renderTarget we need not initialized yet
        setEnabled(true);
    }

    @Override
    public void onRender(float partialTicks) {

    }

    @Override
    public void onTick() {
        renderTarget = ClientContext.renderer.guiTarget.getTarget();
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
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(
                new OverlayOptionsModelView(
                        this,
                        it->{
                            it.setTickModelView(true);
                            it.setAimRotation(false);
                            it.setPositionAnchor(ModelViewAnchor.HMD);
                            it.setFormulaPosX(null);
                            it.setFormulaPosY("-0.1");
                            it.setFormulaPosZ("-1.2");
                            it.setRotationAnchor(ModelViewAnchor.HMD);
                            it.setFormulaRotationX(null);
                            it.setFormulaRotationY(null);
                            it.setFormulaRotationZ(null);
                        }

                )
        );
    }
}
