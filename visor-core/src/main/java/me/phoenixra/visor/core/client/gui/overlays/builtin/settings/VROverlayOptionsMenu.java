package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


@Getter
public class VROverlayOptionsMenu extends VROverlayScreenInScreen<OverlayOptionsScreen<?>> {
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
        VROverlayHelper.anchorWithOverlay(
                this,
                0,1,
                true,
                overlaySettings,
                0,-1,
                true,
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


    public void openMenu(@NotNull VROverlaySettings settingsMenu,
                         @NotNull OverlayOptionGroup optionCategory){
        if(isEnabled()
                && (this.overlaySettings == settingsMenu
                && this.optionsGroup == optionCategory)){
            //already opened
            return;
        }
        setEnabled(false);
        this.overlaySettings = settingsMenu;
        this.optionsGroup = optionCategory;
        this.screen = optionCategory.getScreen();

        setEnabled(true);
        updatePose(1);
    }

    public OverlayOptionsScreen<?> getOptionsScreen(){
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
