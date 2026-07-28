package org.vmstudio.visor.core.client.provider.openxr;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.input.action.data.VRActionData;
import me.phoenixra.atumvr.api.input.body.AtumVRBodyView;
import me.phoenixra.atumvr.core.XRProvider;
import me.phoenixra.atumvr.core.input.XRInputHandler;
import me.phoenixra.atumvr.core.input.action.XRActionSet;

import me.phoenixra.atumvr.core.input.body.EmulatedBodyPreset;
import me.phoenixra.atumvr.core.input.body.XRCommonBodyView;
import me.phoenixra.atumvr.core.input.device.XRDevice;
import me.phoenixra.atumvr.core.input.device.XRDeviceController;
import me.phoenixra.atumvr.core.input.device.XRDeviceHMD;
import me.phoenixra.atumvr.core.input.profile.XRProfileManager;
import me.phoenixra.atumvr.core.input.profile.tracker.FBBodyTrackingProvider;
import me.phoenixra.atumvr.core.input.profile.tracker.ViveTrackerProvider;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class XrInputHandler extends XRInputHandler {

    @Getter
    private XRProfileManager profileSetHolder;

    @Getter @Setter
    private Consumer<VRActionIdentifier> actionListener;

    public XrInputHandler(XRProvider provider) {
        super(provider);
    }

    @Override
    public void onActionChanged(@NotNull VRActionData actionData) {
        if(actionListener != null){
            actionListener.accept(actionData.getId());
        }
    }


    @Override
    protected List<? extends XRActionSet> generateActionSets(MemoryStack stack) {
        profileSetHolder = new XRProfileManager(getVrProvider());
        return profileSetHolder.getAllActionSets();
    }

    @Override
    protected @NotNull List<? extends AtumVRBodyView> generateBodyViews(@NotNull MemoryStack stack) {
        var viveTrackers = new ViveTrackerProvider(getVrProvider());
        //TRACKERS EMULATION TESTING
        //viveTrackers.setEmulated(true);
        //viveTrackers.setEmulationPreset(EmulatedBodyPreset.T_POSE);
        //--------
        return List.of(
                new XRCommonBodyView(getVrProvider()),
                viveTrackers,
                new FBBodyTrackingProvider(getVrProvider())
        );
    }

    @Override
    protected List<? extends XRDevice> generateDevices(MemoryStack stack) {
        List<XRDevice> devices = new ArrayList<>();
        devices.add(
                new XRDeviceHMD(getVrProvider())
        );
        devices.add(
                new XRDeviceController(
                        getVrProvider(),
                        ControllerType.LEFT,
                        profileSetHolder.getCommonSet().getHandPoseAim(),
                        profileSetHolder.getCommonSet().getHandPoseGrip(),
                        profileSetHolder.getCommonSet().getHapticPulse()
                )
        );
        devices.add(
                new XRDeviceController(
                        getVrProvider(),
                        ControllerType.RIGHT,
                        profileSetHolder.getCommonSet().getHandPoseAim(),
                        profileSetHolder.getCommonSet().getHandPoseGrip(),
                        profileSetHolder.getCommonSet().getHapticPulse()
                )
        );
        return devices;
    }

}
