package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;



@Getter
public class VROverlayOptionsMenu extends VROverlayScreenInScreen<OverlayOptionsScreen<?>> {
    public static final String ID = "options_menu";

    private VROverlaySettings settingsMenu;

    private OverlayOptionCategory category;
    public VROverlayOptionsMenu(@NotNull VisorAddon owner,
                                @NotNull String id) {
        super(owner, id, null);
    }

    @Override
    protected void init() {
        super.init();

        mouseEdgeX = screen.getMouseEdgeX();
        mouseEdgeY = screen.getMouseEdgeY();

        mouseEdgeWidth = screen.getMouseEdgeWidth();
        mouseEdgeHeight = screen.getMouseEdgeHeight();
    }

    @Override
    public void applyModelView(float partialTick) {
        if(screen == null) return;

        VROverlayHelper.anchorOverlayPositionTo(
                this,
                ClientContext.player.getPose(PoseType.RENDER),
                settingsMenu.getPosition(),
                settingsMenu.getRotation(),
                screen.getPositionOffset()

        );
        VROverlayHelper.anchorOverlayRotationTo(
                this,
                settingsMenu.getRotation(),
                screen.getRotationOffset()
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
                         @NotNull OverlayOptionCategory optionCategory){
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
                2.084f * settingsMenu.getOverlayScale(),
                1.7f * settingsMenu.getOverlayScale()
        );
        this.setOverlayScale(settingsMenu.getOverlayScale());
        setEnabled(true);
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

    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(

        );
    }
}
