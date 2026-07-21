package org.vmstudio.visor.core.client.input.actions.game;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.*;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionVec2;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.Map;

public class GameActionRotate extends VRActionVec2 {
    public static final String ID = "rotate";

    private static final float ROTATION_THRESHOLD = 0.5F;


    private boolean alreadyRotated;

    public GameActionRotate(VRActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    public void preTick() {
        onStateChanged(getState());
    }
    @Override
    protected void onStateChanged(@NotNull Vector2f newState) {
        if(alreadyRotated){
            if(Math.abs(newState.x) < ROTATION_THRESHOLD){
                alreadyRotated = false;
            }
            return;
        }

        final float inputPosX = newState.x;
        float rotationStep = (float) Math.toRadians(
                VRClientSettings.getWorldRotationStep()
        );

        if(rotationStep == 0){
            if (inputPosX != 0.0F) {
                float delta = -(inputPosX * VRClientSettings.getWorldRotationSmoothSensitivity());
                float currentRotation = ClientContext.localPlayer.getRotationY();
                ClientContext.localPlayer.setRotationY(currentRotation + delta);
            }
            return;
        }

        if (Math.abs(inputPosX) > ROTATION_THRESHOLD) {


            float currentRotation = ClientContext.localPlayer.getRotationY();

            float newRotation = currentRotation
                    - rotationStep * Math.signum(inputPosX);
            ClientContext.localPlayer.setRotationY(newRotation);
            alreadyRotated = true;
        }



    }


    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.VEC2_THUMBSTICK_RIGHT,
                        ValveIndexProfile.VEC2_THUMBSTICK_LEFT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        OculusTouchProfile.VEC2_THUMBSTICK_RIGHT,
                        OculusTouchProfile.VEC2_THUMBSTICK_LEFT
                ),
                VRInteractionProfileType.VIVE,
                new ActionBinding(
                        ViveProfile.VEC2_TRACKPAD_RIGHT,
                        ViveProfile.VEC2_TRACKPAD_LEFT
                ),
                VRInteractionProfileType.VIVE_COSMOS,
                new ActionBinding(
                        ViveCosmosProfile.VEC2_THUMBSTICK_RIGHT,
                        ViveCosmosProfile.VEC2_THUMBSTICK_LEFT
                ),
                VRInteractionProfileType.HP_MIXED_REALITY,
                new ActionBinding(
                        HpMixedRealityProfile.VEC2_THUMBSTICK_RIGHT,
                        HpMixedRealityProfile.VEC2_THUMBSTICK_LEFT
                ),
                VRInteractionProfileType.WINDOWS_MOTION,
                new ActionBinding(
                        WindowsMotionProfile.VEC2_THUMBSTICK_RIGHT,
                        WindowsMotionProfile.VEC2_THUMBSTICK_LEFT
                )
        );
    }
}