package org.vmstudio.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;

import java.util.function.Consumer;

public class OverlayOptionsVisibility extends OverlayOptionGroup<OverlayOptionsVisibility> {
    public static final String ID = "visibility";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    @Getter
    private boolean visible = true;



    public OverlayOptionsVisibility(@NotNull VROverlay owner,
                                    @NotNull Consumer<OverlayOptionsVisibility> defaultSettings){
        super(owner, defaultSettings);
    }



    @Override
    public void update(boolean force) {
    }

    @Override
    protected void onLoad(@NotNull Config config){

        visible = config.getBoolOrDefault("visible", true);


    }
    @Override
    public void onSave(@NotNull Config config){
        config.set("visible", visible);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        changesNotSaved = true;
    }

    @Override
    public boolean supportsCopying() {
        return true;
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return VisorAPI.client().getGuiManager().getOverlayManager().getOptionsScreenFor(
                this
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;

    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
