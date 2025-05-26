package me.phoenixra.visor.api.client.input.button;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.profileset.ProfileSetHolder;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.KeyMapping;


public class VisorActionButton implements VisorAction{

    @Getter
    private final KeyMapping keyMapping;
    @Getter
    private final VisorAddon owner;
    @Getter
    private final String id;

    @Getter @Setter
    private boolean enabled = true;

    @Getter
    private boolean active;

    private boolean pressed = false;

    protected int releaseDelayed = 0;
    protected int pressDelayed = 0;


    public VisorActionButton(VisorAddon owner,
                             KeyMapping keyMapping
    ) {
        this.owner = owner;
        this.keyMapping = keyMapping;
        this.id = keyMapping.getName();
    }


    @Override
    public void tick() {

    }

    @Override
    public void updateState(OpenXRProfileSet currentProfile) {

    }


}
