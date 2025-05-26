package me.phoenixra.atumvr.core.input.action.profileset;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.pose.VRPoseRecord;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.types.HapticPulseAction;
import me.phoenixra.atumvr.core.input.action.types.multi.PoseMultiAction;

import java.util.List;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class SharedActionSet extends OpenXRActionSet {
    // Haptics
    private HapticPulseAction hapticPulse;

    // Hand poses
    private PoseMultiAction handPoseAim;
    private PoseMultiAction handPoseGrip;


    public SharedActionSet(OpenXRProvider provider) {
        super(provider, "shared", "Shared set", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {
        var supportedProfiles = XRInteractionProfile.getSupported(provider);
        // -------- HAPTICS --------
        hapticPulse = new HapticPulseAction(
                provider, this,
                "haptic_pulse", "Haptic Pulse"
        ).putDefaultBindings(supportedProfiles, "output/haptic");

        // -------- HAND POSES --------
        handPoseAim = new PoseMultiAction(
                provider, this,
                "hand_aim", "Hand Aim",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  VRPoseRecord.EMPTY)
                                .putDefaultBindings(supportedProfiles, "input/aim/pose"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, VRPoseRecord.EMPTY)
                                .putDefaultBindings(supportedProfiles, "input/aim/pose")
                )
        );

        handPoseGrip = new PoseMultiAction(
                provider, this,
                "hand_grip", "Hand Grip",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  VRPoseRecord.EMPTY)
                                .putDefaultBindings(supportedProfiles, "input/grip/pose"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, VRPoseRecord.EMPTY)
                                .putDefaultBindings(supportedProfiles, "input/grip/pose")
                )
        );

        return List.of(hapticPulse, handPoseAim, handPoseGrip);
    }
}
