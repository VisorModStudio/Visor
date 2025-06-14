package me.phoenixra.visor.core.client.gui.overlays.types;



import me.phoenixra.visor.api.client.gui.overlay.ModelViewAnchor;
import me.phoenixra.visor.api.client.gui.overlay.RegisterOverlayType;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;

import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@RegisterOverlayType(id = VROverlayChat.ID_TYPE)
public class VROverlayChat extends VROverlayScreen {
    public static final String ID_TYPE = "chat";

    public VROverlayChat(@NotNull VisorAddon owner,
                         @NotNull String id) {
        super(owner, id);
        setEnabled(true);
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        minecraft.gui.getChat().render(
                guiGraphics,
                minecraft.gui.getGuiTicks(),0, 0
        );
    }

    @Override
    protected void onTick() {

    }

    @Override
    public boolean updateVisibility() {
        if(minecraft.level == null) return false;
        if(minecraft.isPaused()
                || ClientContext.overlayManager.isShowingKeyboard()) return false;
        if (!ClientContext.rawPoseHandler.getControllerData(ControllerHand.OFFHAND)
                .isTracking()) {
            return false;
        }


        return !minecraft.gui.getChat().trimmedMessages.isEmpty() &&
                minecraft.options.chatVisibility().get() != ChatVisiblity.HIDDEN;
    }


    @Override
    public boolean isCursorSupported() {
        return false;
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(
                new OverlayOptionsGlobal(
                        this,
                        it->{
                            it.setUpdateOptionsType(OverlayOptionsGlobal.UpdateOptionsType.TICK);
                            it.setFormulaOverlayScale("0.5");
                        }
                ),
                new OverlayOptionsModelView(
                        this,
                        it->{
                            it.setTickModelView(true);
                            it.setAimRotation(false);

                            it.setPositionAnchor(ModelViewAnchor.OFFHAND);
                            it.setFormulaPosX("-0.15 * %main_hand%");
                            it.setFormulaPosY("0.06");
                            it.setFormulaPosZ("-0.13 * %main_hand% + (0.06 * %right_handed%)");
                            it.setRotationAnchor(ModelViewAnchor.OFFHAND);
                            it.setFormulaRotationX("-pi/2 * %main_hand%");
                            it.setFormulaRotationY("pi/2 * %main_hand%");
                            it.setFormulaRotationZ("pi * %left_handed%");
                        }

                )
        );
    }
}
