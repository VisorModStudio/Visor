package me.phoenixra.visor.core.client.input;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.device.VRDeviceController;

import me.phoenixra.visor.api.client.input.InputManager;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.provider.openxr.XrProvider;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InputManagerImpl implements InputManager {


    @Getter
    private final ActionSetRegistry actionSetRegistry;

    @Getter
    private VisorActionSet activeSet;

    public InputManagerImpl(){
        actionSetRegistry = new ActionSetRegistry();

    }

    public void preTick(){
        VisorActionSet newActiveSet = null;
        for(var entry : actionSetRegistry.getSortedElements()){
            if(entry.isEnabledAndCanActivate()){
                newActiveSet = entry;
                break;
            }
        }
        if(activeSet != null && activeSet != newActiveSet){
            activeSet.clear();
        }

        activeSet = newActiveSet;

        if(activeSet != null) {
            activeSet.preTick();
        }
    }
    public void update(){
        if(activeSet == null){
            return;
        }
        var provider = (XrProvider)ClientContext.visor.getVrProvider();
        var currentProfile = provider.getInputHandler().getProfileSetHolder()
                .getActiveProfileSet();
        if(currentProfile == null) {
            return;
        }
        activeSet.updateState(
                currentProfile,
                isLeftHanded()
        );
    }

    @Override
    public boolean isLeftHanded() {
        return VRClientSettings.isLeftHanded();
    }

    @Override
    public void triggerHapticPulse(@NotNull ControllerHand hand,
                                   float frequency,
                                   float amplitude,
                                   long durationNanoSec) {
        if(VisorState.getState().isNotActive()){
            return;
        }
        String controllerId = VRDeviceController.getDefaultId(
                hand.getType(isLeftHanded())
        );
        ClientContext.visor.getVrProvider().getInputHandler()
                .getDevice(controllerId, VRDeviceController.class)
                .triggerHapticPulse(frequency, amplitude, durationNanoSec);
    }



    public List<VisorElementRegistry<?>> getElementRegistries(){
        return List.of(
                actionSetRegistry
        );
    }
}
