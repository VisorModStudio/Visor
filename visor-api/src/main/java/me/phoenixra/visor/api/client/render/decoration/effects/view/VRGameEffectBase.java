package me.phoenixra.visor.api.client.render.decoration.effects.view;

import lombok.Getter;
import lombok.Setter;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

public abstract class VRGameEffectBase implements VRGameEffect {
    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled = true;

    public VRGameEffectBase(@NotNull VisorAddon owner){
        this.owner = owner;
    }

}
