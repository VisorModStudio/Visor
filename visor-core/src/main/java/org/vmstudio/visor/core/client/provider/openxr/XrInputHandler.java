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

import me.phoenixra.atumvr.core.input.body.XRCommonBodyView;
import me.phoenixra.atumvr.core.input.device.XRDevice;
import me.phoenixra.atumvr.core.input.device.XRDeviceController;
import me.phoenixra.atumvr.core.input.device.XRDeviceHMD;
import me.phoenixra.atumvr.core.input.haptics.XRBodyHapticsProvider;
import me.phoenixra.atumvr.core.input.haptics.bhaptics.BHapticsProvider;
import me.phoenixra.atumvr.core.input.profile.XRProfileManager;
import me.phoenixra.atumvr.core.input.profile.tracker.FBBodyTrackingProvider;
import me.phoenixra.atumvr.core.input.profile.tracker.ViveTrackerProvider;
import me.phoenixra.atumvr.core.input.profile.tracker.hand.EXTHandTrackingProvider;
import me.phoenixra.atumvr.core.input.profile.tracker.hand.XRHandsProvider;
import me.phoenixra.atumvr.core.input.treadmill.XRTreadmillProvider;
import me.phoenixra.atumvr.core.input.treadmill.infinadeck.InfinadeckTreadmillProvider;
import me.phoenixra.atumvr.core.input.treadmill.kat.KATLegacyTreadmillProvider;
import me.phoenixra.atumvr.core.input.treadmill.kat.KATTreadmillProvider;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.system.MemoryStack;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.provider.RegisterBodyHapticsVREvent;
import org.vmstudio.visor.api.client.events.provider.RegisterBodyTrackersVREvent;
import org.vmstudio.visor.api.client.events.provider.RegisterHandTrackersVREvent;
import org.vmstudio.visor.api.client.events.provider.RegisterTreadmillsVREvent;

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
        var event = new RegisterBodyTrackersVREvent(
                List.of(
                        new XRCommonBodyView(getVrProvider()),
                        viveTrackers,
                        new FBBodyTrackingProvider(getVrProvider())
                )
        );
        VisorAPI.eventBus().callEvent(event);
        return event.getProviders();
    }

    @Override
    protected @NotNull List<? extends XRHandsProvider> generateHandsProviders(@NotNull MemoryStack stack) {
        var event = new RegisterHandTrackersVREvent(
                List.of(
                        new EXTHandTrackingProvider(getVrProvider())
                )
        );
        VisorAPI.eventBus().callEvent(event);
        return event.getProviders();
    }

    @Override
    protected @NotNull List<? extends XRTreadmillProvider> generateTreadmillProviders(@NotNull MemoryStack stack) {
        var event = new RegisterTreadmillsVREvent(
                List.of(
                        new KATTreadmillProvider(getVrProvider()),
                        new KATLegacyTreadmillProvider(getVrProvider()),
                        new InfinadeckTreadmillProvider(getVrProvider())
                )
        );
        VisorAPI.eventBus().callEvent(event);
        return event.getProviders();
    }

    @Override
    protected @NotNull List<? extends XRBodyHapticsProvider> generateBodyHapticsProviders(@NotNull MemoryStack stack) {
        var event = new RegisterBodyHapticsVREvent(
                List.of(
                        new BHapticsProvider(getVrProvider())
                )
        );
        VisorAPI.eventBus().callEvent(event);
        return event.getProviders();
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
