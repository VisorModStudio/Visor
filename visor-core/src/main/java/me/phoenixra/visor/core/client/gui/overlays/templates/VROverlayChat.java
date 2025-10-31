package me.phoenixra.visor.core.client.gui.overlays.templates;



import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
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
        id = VROverlayChat.ID,
        name = VROverlayChat.NAME,
        description = VROverlayChat.DESCRIPTION,
        isCreateDefault = true
)
public class VROverlayChat extends VROverlayTemplateScreen {
    public static final String ID = "chat";
    public static final String NAME = "visor.overlay.template."+ID+".name";
    public static final String DESCRIPTION = "visor.overlay.template."+ID+".description";

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
                new OverlayOptionsMisc(
                        this,
                        it->{
                            it.setOptionsUpdaterType(OverlayOptionsMisc.OptionsUpdaterType.TICK);
                        }
                ),
                new OverlayOptionsPose(
                        this,
                        it->{
                            it.setTickPose(true);
                            it.setAimedRotation(false);

                            it.setPositionAnchor(PoseAnchor.OFFHAND);
                            it.setPositionOffset(
                                    -0.15f,
                                    0.06f,
                                    -0.13f + 0.06f
                            );
                            it.setRotationAnchor(PoseAnchor.OFFHAND);
                            it.setRotationOffset(
                                    (float) (-Math.PI / 2),
                                    (float) (Math.PI / 2),
                                    0
                            );
                            it.setScale(0.5f);
                        }

                )
        );
    }

}
