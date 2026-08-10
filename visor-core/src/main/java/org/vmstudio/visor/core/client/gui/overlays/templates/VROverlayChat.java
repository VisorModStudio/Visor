package org.vmstudio.visor.core.client.gui.overlays.templates;



import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsVisibility;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreen;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
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

        return !minecraft.gui.getChat().trimmedMessages.isEmpty() &&
                minecraft.options.chatVisibility().get() != ChatVisiblity.HIDDEN;
    }


    @Override
    public boolean supportsCursor() {
        return false;
    }

    @Override
    public boolean isHudLayer() {
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createTemplateOptions() {
        return List.of(
                new OverlayOptionsVisibility(
                        this,
                        it -> it.setVisible(true)
                ),
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

                            it.setPositionAnchor(PoseAnchor.HMD);
                            it.setPositionOffset(
                                    0.392f,
                                    0.214f,
                                    -1.706f
                            );
                            it.setRotationAnchor(PoseAnchor.HMD);
                            it.setRotationOffset(
                                    0,
                                    0,
                                    0
                            );
                            it.setScale(1.2f);
                        }

                )
        );
    }
}
