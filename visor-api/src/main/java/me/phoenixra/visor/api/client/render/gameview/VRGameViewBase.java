package me.phoenixra.visor.api.client.render.gameview;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

public abstract class VRGameViewBase implements VRGameView {
    @Getter
    private final VisorAddon owner;
    @Getter
    private final String id;


    @Getter @Setter
    private boolean enabled;

    public VRGameViewBase(@NotNull VisorAddon owner,
                          @NotNull String id){
        this.owner = owner;
        this.id = id;
    }


}
