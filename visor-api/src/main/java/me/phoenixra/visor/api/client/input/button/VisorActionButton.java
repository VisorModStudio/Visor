package me.phoenixra.visor.api.client.input.button;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.KeyMapping;

import java.util.HashMap;
import java.util.Map;


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

    private final Map<XRInteractionProfile, String> bindings;


    public VisorActionButton(VisorAddon owner,
                             KeyMapping keyMapping,
                             Map<XRInteractionProfile, String> bindings
    ) {
        this.owner = owner;
        this.keyMapping = keyMapping;
        this.id = keyMapping.getName();

        this.bindings = bindings;
    }


    @Override
    public void tick() {

    }

    @Override
    public void updateState(OpenXRProfileSet currentProfile) {
        VRActionDataButton dataButton = currentProfile.getButton(
                bindings.get(currentProfile.getType())
        );



    }


}
