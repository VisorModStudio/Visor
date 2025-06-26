package me.phoenixra.visor.api.client.gui.overlay.template;

import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionCategory;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.Collection;

public interface OverlayTemplate extends VROverlay {



    @Nullable
    <T extends OverlayOptionCategory> T getOption(@NotNull Class<T> type);

    @NotNull
    Collection<OverlayOptionCategory> getOptions();

    @NotNull
    ConfigFile getTypeConfig();


    @ApiStatus.Internal
    @Nullable PoseAnchor getDemoAnchor();
    @ApiStatus.Internal
    void setDemoAnchor(@Nullable PoseAnchor anchor);


    @NotNull
    Component getOverlayName();

    @NotNull
    String getTypeId();
}
