package org.vmstudio.visor.core.client.input;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.input.device.AtumVRDeviceController;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.core.input.profile.XRProfileManager;
import org.vmstudio.visor.api.client.input.VRInputManager;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.client.input.action.framework.VRActionVec2;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.component.ComponentRegistry;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.input.actions.ActionLeftMouse;
import org.vmstudio.visor.core.client.input.actions.ActionMiddleMouse;
import org.vmstudio.visor.core.client.input.actions.ActionRightMouse;
import org.vmstudio.visor.core.client.input.actions.ActionScrollMouse;
import org.vmstudio.visor.core.client.provider.openxr.XrProvider;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VRInputManagerImpl implements VRInputManager {


    @Getter
    private final ActionSetRegistry actionSetRegistry;

    @Getter
    private VRActionSet activeSet;

    @Setter
    private long pausedActionsTicks = -1;


    public VRInputManagerImpl(){
        actionSetRegistry = new ActionSetRegistry();

    }

    public void preTick(){
        VRActionSet newActiveSet = null;
        for(var entry : actionSetRegistry.getSortedComponents()){
            if(entry.isEnabledAndCanActivate()){
                newActiveSet = entry;
                break;
            }
        }

        if(activeSet != null && activeSet != newActiveSet){
            activeSet.clear();
        }

        activeSet = newActiveSet;

        var provider = ((XrProvider)ClientContext.visor.getVrProvider());
        boolean canUpdate = pausedActionsTicks <= 0 && provider.getInputHandler().getActionListener() == null;
        if(canUpdate && activeSet != null) {
            activeSet.preTick();
        }
        if(pausedActionsTicks > 0) {
            pausedActionsTicks--;
        }
    }

    public void update(){
        if(activeSet == null){
            return;
        }
        var provider = (XrProvider)ClientContext.visor.getVrProvider();
        var currentProfile = provider.getInputHandler().getProfileSetHolder()
                .getActiveProfile();
        boolean canUpdate = pausedActionsTicks <= 0 && provider.getInputHandler().getActionListener() == null;

        if(!canUpdate || currentProfile == null) {
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
    public @NotNull XRProfileManager getProfileManager() {
        return ((XrProvider)ClientContext.visor.getVrProvider())
                .getInputHandler().getProfileSetHolder();
    }

    @Override
    public @Nullable VRInteractionProfileType getActiveProfile() {
        var provider = (XrProvider)ClientContext.visor.getVrProvider();
        var profileSet = provider.getInputHandler().getProfileSetHolder()
                .getActiveProfile();
        if(profileSet == null){
            return null;
        }
        return profileSet.getType();
    }

    @Override
    public @Nullable VRActionButton getActionLeftMouse(@NotNull HandType hand) {
        var actionSet = getActiveSet();
        if(actionSet == null){
            return null;
        }
        return  (VRActionButton) (hand == HandType.MAIN
                ? actionSet.getAction(ActionLeftMouse.ID_MAIN)
                : actionSet.getAction(ActionLeftMouse.ID_OFFHAND));
    }

    @Override
    public @Nullable VRActionButton getActionRightMouse(@NotNull HandType hand) {
        var actionSet = getActiveSet();
        if(actionSet == null){
            return null;
        }
        return  (VRActionButton) (hand == HandType.MAIN
                ? actionSet.getAction(ActionRightMouse.ID_MAIN)
                : actionSet.getAction(ActionRightMouse.ID_OFFHAND));
    }

    @Override
    public @Nullable VRActionButton getActionMiddleMouse(@NotNull HandType hand) {
        var actionSet = getActiveSet();
        if(actionSet == null){
            return null;
        }
        return  (VRActionButton) (hand == HandType.MAIN
                ? actionSet.getAction(ActionMiddleMouse.ID_MAIN)
                : actionSet.getAction(ActionMiddleMouse.ID_OFFHAND));
    }

    @Override
    public @Nullable VRActionVec2 getActionScrollMouse(@NotNull HandType hand) {
        var actionSet = getActiveSet();
        if(actionSet == null){
            return null;
        }
        return  (VRActionVec2) (hand == HandType.MAIN
                ? actionSet.getAction(ActionScrollMouse.ID_MAIN)
                : actionSet.getAction(ActionScrollMouse.ID_OFFHAND));
    }

    @Override
    public void triggerHapticPulse(@NotNull HandType hand,
                                   float frequency,
                                   float amplitude,
                                   long durationNanoSec) {
        if(VisorState.get().isNotActive()){
            return;
        }
        String controllerId = AtumVRDeviceController.getId(
                hand.asControllerType(isLeftHanded())
        );
        ClientContext.visor.getVrProvider().getInputHandler()
                .getDevice(controllerId, AtumVRDeviceController.class)
                .triggerHapticPulse(frequency, amplitude, durationNanoSec);
    }



    public List<ComponentRegistry<?>> getComponentRegistries(){
        return List.of(
                actionSetRegistry
        );
    }
}
