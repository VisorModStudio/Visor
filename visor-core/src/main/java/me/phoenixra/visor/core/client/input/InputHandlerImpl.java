package me.phoenixra.visor.core.client.input;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.device.VRDeviceController;

import me.phoenixra.visor.api.client.input.InputHandler;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.provider.openxr.XrVRProvider;
import me.phoenixra.visor.core.client.settings.VRClientSettings;

import java.util.List;

public class InputHandlerImpl implements InputHandler {


    @Getter
    private final ActionSetRegistry actionSetRegistry;

    private VisorActionSet activeSet;

    public InputHandlerImpl(){
        actionSetRegistry = new ActionSetRegistry();

    }

    public void preTick(){
        activeSet = null;
        for(var entry : actionSetRegistry.getSortedActionSet()){
            if(entry.canActivate()){
                activeSet = entry;
                break;
            }
        }

        if(activeSet != null) {
            activeSet.preTick();
        }
    }
    public void update(){
        if(activeSet == null){
            return;
        }
        var provider = (XrVRProvider)ClientContext.visor.getVrProvider();
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
    public void triggerHapticPulse(ControllerHand hand,
                                   float frequency,
                                   float amplitude,
                                   long durationNanoSec) {
        if(VisorState.getStateMode().isNotActive()){
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
