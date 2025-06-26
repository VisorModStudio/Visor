package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;


@Getter
public class VROverlayOptionsMenu extends VROverlayScreenInScreen<OverlayOptionsScreen<?>> {
    public static final String ID = "options_menu";

    private VROverlaySettings settingsMenu;

    private OverlayOptions category;
    public VROverlayOptionsMenu(@NotNull VisorAddon owner,
                                @NotNull String id) {
        super(owner, id, null);
    }

    @Override
    protected void init() {
        super.init();

        cursorEdgeX = screen.getMouseEdgeX();
        cursorEdgeY = screen.getMouseEdgeY();

        cursorEdgeWidth = screen.getMouseEdgeWidth();
        cursorEdgeHeight = screen.getMouseEdgeHeight();
    }

    @Override
    public void updatePose(float partialTicks) {
        if(screen == null) return;

        var newPos = PoseAnchor.getAnchorPos(
                settingsMenu.getPose().getPosition(),
                settingsMenu.getPose().getRotation(),
                screen.getPositionOffset()
        );

        var newRotation = PoseAnchor.getAnchorRotation(
                settingsMenu.getPose().getRotation(),
                screen.getRotationOffset()
        );
        getPose().update(
                newPos, newRotation,
                settingsMenu.getPose().getScale()
        );
    }


    @Override
    public void setEnabled(boolean flag) {
        if(settingsMenu == null && flag){
            return;
        }
        super.setEnabled(flag);
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

        if(ClientContext.overlayManager
                .getOverlay(category.getOwner().getId()) != null) {
            //save category to file if overlay wasn't removed from registry
            category.save();
        }
        if(screen != null){
            screen.removed();
        }

        settingsMenu = null;
        screen = null;
        category = null;
    }

    @Override
    public boolean updateVisibility() {
        return true;
    }


    public void openMenu(@NotNull VROverlaySettings settingsMenu,
                         @NotNull OverlayOptions optionCategory){
        if(isEnabled()
                && (this.settingsMenu == settingsMenu
                && this.category == optionCategory)){
            //already opened
            return;
        }
        setEnabled(false);
        this.settingsMenu = settingsMenu;
        this.category = optionCategory;
        this.screen = optionCategory.getScreen(
                2.084f * settingsMenu.getPose().getScale(),
                1.7f * settingsMenu.getPose().getScale()
        );

        setEnabled(true);
        updatePose(1);
    }

    public OverlayOptionsScreen<?> getOptionsScreen(){
        return this.screen;
    }


    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        VROverlayDemo demo = (VROverlayDemo) ClientContext.overlayManager
                .getOverlay(VROverlayDemo.ID);
        if(demo != null && demo.getMovingByAnchor() != null){
            return true;
        }
        return super.mouseClicked(x, y, buttonType);
    }

}
