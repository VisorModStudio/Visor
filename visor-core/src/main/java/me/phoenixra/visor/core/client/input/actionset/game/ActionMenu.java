package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.types.VisorActionButton;
import net.minecraft.client.gui.screens.PauseScreen;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionMenu extends VisorActionButton {
    public static final String ID = "menu";

    public ActionMenu(VisorActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        if (MC.screen != null) {
            MC.setScreen(null);
        } else {
            MC.setScreen(new PauseScreen(true));
        }
    }

    @Override
    protected void onRelease() {

    }

    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.BUTTON_PRIMARY_LEFT,
                        ValveIndexSet.BUTTON_PRIMARY_RIGHT
                )
        );
    }

}
