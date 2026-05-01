package org.vmstudio.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;

import java.util.function.Consumer;

@Getter @Setter
public class OverlayOptionsResizing extends OverlayOptionGroup<OverlayOptionsResizing> {

    public static final String ID = "resizing";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    private float resizingScale = -1;

    public OverlayOptionsResizing(@NotNull VROverlay owner,
                                  @NotNull Consumer<OverlayOptionsResizing> defaultSettings){
        super(owner, defaultSettings);
    }


    @Override
    public void update(boolean force) {

    }

    @Override
    protected void onLoad(@NotNull Config config){

        resizingScale = Mth.clamp(
                config.getFloatOrDefault(
                        "resizing_scale",
                        1.0f
                ), 0.0001f, 1000);

        changesNotSaved = false;

    }
    @Override
    public void onSave(@NotNull Config config){
        config.set("resizing_scale", resizingScale);
    }

    @Override
    public boolean isModifiable() {
        return false;
    }

    @Override
    public @Nullable OptionsScreen<?> getScreen() {
        return null;
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
