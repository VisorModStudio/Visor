package org.vmstudio.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayScreenInScreen;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


@Getter
public class VROverlayOptionsMenu extends VROverlayScreenInScreen<OptionsScreen<?>> {
    public static final String ID = "options_menu";

    private VROverlaySettings overlaySettings;

    @Getter
    private OverlayOptionGroup<?> optionsGroup;

    public VROverlayOptionsMenu(@NotNull VisorAddon owner,
                                @NotNull String id) {
        super(owner, id, null);
    }

    @Override
    public void init() {
        super.init();

        cursorBoundsX = screen.getCursorBoundsX();
        cursorBoundsY = screen.getCursorBoundsY();

        cursorBoundsWidth = screen.getCursorBoundsWidth();
        cursorBoundsHeight = screen.getCursorBoundsHeight();
    }

    @Override
    public void onUpdatePose(float partialTicks) {
        if(screen == null) return;
        getPose().updateOnlyScale(overlaySettings.getPose().getScale());
        int[] boundsTarget = new int[4];
        int[] boundsAnchor = new int[4];

        boundsTarget[0] = cursorBoundsX;
        boundsTarget[1] = cursorBoundsY;
        boundsTarget[2] = cursorBoundsWidth;
        boundsTarget[3] = cursorBoundsHeight;

        boundsAnchor[0] = overlaySettings.getCursorBoundsX()
                - overlaySettings.getCursorBoundsOffsetX();
        boundsAnchor[1] = overlaySettings.getCursorBoundsY()
                - overlaySettings.getCursorBoundsOffsetY();
        boundsAnchor[2] = overlaySettings.getCursorBoundsWidth()
                - overlaySettings.getCursorBoundsOffsetWidth();
        boundsAnchor[3] = overlaySettings.getCursorBoundsHeight()
                - overlaySettings.getCursorBoundsOffsetHeight();

        VROverlayHelper.anchorWithOverlay(
                this,
                0,1,
                boundsTarget,
                overlaySettings,
                0,-1,
                boundsAnchor,
                new Vector3f(0,0,0),
                new Vector3f((float) Math.toRadians(-30),0,0)

        );
    }


    @Override
    public void setEnabled(boolean flag) {
        if(overlaySettings == null && flag){
            return;
        }
        super.setEnabled(flag);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

        if(screen != null){
            screen.removed();
        }

        overlaySettings = null;
        screen = null;
        optionsGroup = null;
    }

    @Override
    public boolean updateVisibility() {
        return true;
    }

    @Override
    public boolean supportsLight() {
        return false;
    }

    public void openMenu(@NotNull VROverlaySettings settingsMenu,
                         @NotNull OverlayOptionGroup<?> optionsGroup){
        if(optionsGroup.getScreen() == null){
            return;
        }
        if(isEnabled()
                && (this.overlaySettings == settingsMenu
                && this.optionsGroup == optionsGroup)){
            //already opened
            return;
        }
        setEnabled(false);
        this.overlaySettings = settingsMenu;
        this.optionsGroup = optionsGroup;
        this.screen = optionsGroup.getScreen();

        setEnabled(true);
        updatePose(1);
    }

    public OptionsScreen<?> getOptionsScreen(){
        return this.screen;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        VROverlayDemo demo = (VROverlayDemo) ClientContext.overlayManager
                .getOverlay(VROverlayDemo.ID);
        if(demo != null && demo.getMovingByAnchor() != null){
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, buttonType);
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
    }

}
