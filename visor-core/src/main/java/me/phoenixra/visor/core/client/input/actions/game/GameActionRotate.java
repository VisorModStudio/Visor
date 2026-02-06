package me.phoenixra.visor.core.client.input.actions.game;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionVec2;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import org.joml.Vector2f;

import java.util.Map;

public class GameActionRotate extends VisorActionVec2 {
    public static final String ID = "rotate";

    private static final float ROTATION_THRESHOLD = 0.5F;

    @Getter
    private final boolean required = false;


    private boolean alreadyRotated;

    public GameActionRotate(VisorActionSet actionSet) {
        super(actionSet, ID);
    }

    @Override
    protected void onStateChanged(Vector2f newState) {
        if(alreadyRotated){
            if(Math.abs(newState.x) < ROTATION_THRESHOLD){
                alreadyRotated = false;
            }
            return;
        }

        final float inputPosX = newState.x;
        float rotationIncrementer = (float) Math.toRadians(
                VRClientSettings.getWorldRotationIncrement()
        );

        if(rotationIncrementer == 0){
            if (inputPosX != 0.0F) {
                float currentRotation = ClientContext.localPlayer.getRotationY();
                float newRotation = currentRotation - (inputPosX * VRClientSettings.getWorldRotationSmoothSensitivity());
                ClientContext.localPlayer.setRotationY(newRotation);
            }
            return;
        }

        if (Math.abs(inputPosX) > ROTATION_THRESHOLD) {


            float currentRotation = ClientContext.localPlayer.getRotationY();

            float newRotation = currentRotation
                    - rotationIncrementer * Math.signum(inputPosX);
            ClientContext.localPlayer.setRotationY(newRotation);
            alreadyRotated = true;
        }



    }

    @Override
    protected Map<VRInteractionProfileType, ActionBinding> loadDefaults() {
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
                )
        );
    }
}
