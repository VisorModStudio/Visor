package me.phoenixra.visor.core.client.provider.openxr;

import lombok.Getter;
import me.phoenixra.atumvr.api.enums.ControllerType;
import me.phoenixra.atumvr.api.input.device.VRDeviceController;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.input.OpenXRInputHandler;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.action.profileset.ProfileSetHolder;
import me.phoenixra.atumvr.core.input.device.OpenXRDevice;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceController;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceHMD;

import org.lwjgl.system.MemoryStack;

import java.util.List;

public class XrInputHandler extends OpenXRInputHandler {
    @Getter
    private ProfileSetHolder profileSetHolder;


    public XrInputHandler(OpenXRProvider provider) {
        super(provider);
    }

    @Override
    protected List<? extends OpenXRActionSet> generateActionSets(MemoryStack stack) {
        profileSetHolder = new ProfileSetHolder(getVrProvider());

        return profileSetHolder.getAllSets();
    }

    @Override
    protected List<? extends OpenXRDevice> generateDevices(MemoryStack stack) {
        return List.of(
                new OpenXRDeviceHMD(getVrProvider()),
                new OpenXRDeviceController(
                        getVrProvider(),
                        ControllerType.LEFT,
                        profileSetHolder.getSharedSet().getHandPoseAim(),
                        profileSetHolder.getSharedSet().getHandPoseGrip(),
                        profileSetHolder.getSharedSet().getHapticPulse()
                ),
                new OpenXRDeviceController(
                        getVrProvider(),
                        ControllerType.RIGHT,
                        profileSetHolder.getSharedSet().getHandPoseAim(),
                        profileSetHolder.getSharedSet().getHandPoseGrip(),
                        profileSetHolder.getSharedSet().getHapticPulse()
                )
        );
    }

}
