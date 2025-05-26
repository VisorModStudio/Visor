package me.phoenixra.visor.api.client.render.decoration.hand;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

public abstract class VRHandItemPoseBase implements VRHandItemPose {
    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled = true;

    public VRHandItemPoseBase(@NotNull VisorAddon owner){
        this.owner = owner;
    }
}
