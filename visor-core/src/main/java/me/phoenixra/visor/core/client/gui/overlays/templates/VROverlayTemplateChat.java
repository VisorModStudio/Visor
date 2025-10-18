package me.phoenixra.visor.core.client.gui.overlays.templates;



import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;

import me.phoenixra.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@RegisterVROverlayTemplate(
        id = VROverlayTemplateChat.ID,
        name = VROverlayTemplateChat.NAME,
        description = VROverlayTemplateChat.DESCRIPTION,
        isCreateDefault = true
)
public class VROverlayTemplateChat extends VROverlayTemplateScreen {
    public static final String ID = "chat";
    public static final String NAME = "visor.overlay.template."+ID+".name";
    public static final String DESCRIPTION = "visor.overlay.template."+ID+".description";

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
    public boolean supportsDepth() {
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createTemplateOptions() {
        return List.of(
                new OverlayOptionsGlobal(
                        this,
                        it->{
                            it.setOptionsUpdaterType(OverlayOptionsGlobal.OptionsUpdaterType.TICK);
                        }
                ),
                new OverlayOptionsPose(
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
                            it.setFormulaScale("0.5");
                        }

                )
        );
    }

}
