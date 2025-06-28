package me.phoenixra.visor.core.client.gui.overlays.templates;



import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.template.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsLocation;

import me.phoenixra.visor.api.client.gui.overlay.template.framework.VROverlayTemplateScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@RegisterVROverlayTemplate(id = VROverlayTemplateChat.ID)
public class VROverlayTemplateChat extends VROverlayTemplateScreen {
    public static final String ID = "chat";

    public VROverlayTemplateChat(@NotNull VisorAddon owner,
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
    public boolean updateVisibility() {
        if(minecraft.level == null) return false;
        if(minecraft.isPaused()
                || ClientContext.overlayManager.getKeyboardAccessor().isVisible()) return false;
        if (!ClientContext.rawPoseHandler.getControllerData(ControllerHand.OFFHAND)
                .isTracking()) {
            return false;
        }


        return !minecraft.gui.getChat().trimmedMessages.isEmpty() &&
                minecraft.options.chatVisibility().get() != ChatVisiblity.HIDDEN;
    }


    @Override
    public boolean supportsCursor() {
        return false;
    }


    @Override
    protected @NotNull List<OverlayOptions> createOptions() {
        return List.of(
                new OverlayOptionsGlobal(
                        this,
                        it->{
                            it.setUpdateOptionsType(OverlayOptionsGlobal.UpdateOptionsType.TICK);
                            it.setFormulaOverlayScale("0.5");
                        }
                ),
                new OverlayOptionsLocation(
                        this,
                        it->{
                            it.setTickModelView(true);
                            it.setAimRotation(false);

                            it.setPositionAnchor(PoseAnchor.OFFHAND);
                            it.setFormulaPosX("-0.15 * %main_hand%");
                            it.setFormulaPosY("0.06");
                            it.setFormulaPosZ("-0.13 * %main_hand% + (0.06 * %right_handed%)");
                            it.setRotationAnchor(PoseAnchor.OFFHAND);
                            it.setFormulaRotationX("-pi/2 * %main_hand%");
                            it.setFormulaRotationY("pi/2 * %main_hand%");
                            it.setFormulaRotationZ("pi * %left_handed%");
                        }

                )
        );
    }

}
