package me.phoenixra.visor.api.client.gui.overlay.options;


import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public interface OverlayOptionCategory {

    void load();

    void save();

    void saveDefaults();

    void update(boolean force);

    /**
     *
     * @param mainMenuWidth main menu overlay room width
     * @param mainMenuHeight main menu overlay room height
     * @return
     */
    @NotNull
    OverlayOptionsScreen getScreen(float mainMenuWidth, float mainMenuHeight);

    @NotNull
    String getId();

    @NotNull
    Component getDisplayName();
    @NotNull
    VROverlay getOwner();

}
