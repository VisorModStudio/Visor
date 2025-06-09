package me.phoenixra.visor.api.client.render.decoration.effects.hand;

import lombok.Getter;
import lombok.Setter;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;

public abstract class VRHandEffectBase implements VRHandEffect{
    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled = true;

    public VRHandEffectBase(@NotNull VisorAddon owner){
        this.owner = owner;
    }
}
