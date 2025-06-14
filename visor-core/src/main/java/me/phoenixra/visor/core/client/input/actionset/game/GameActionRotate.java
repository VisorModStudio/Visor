package me.phoenixra.visor.core.client.input.actionset.game;

import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.types.ValveIndexSet;
import me.phoenixra.visor.api.client.input.action.BindingPath;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionVec2;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.types.movement.TaskInputRotation;
import org.joml.Vector2f;

import java.util.Map;

public class GameActionRotate extends VisorActionVec2 {
    public static final String ID = "rotate";

    private static final float ROTATION_THRESHOLD = 0.5F;

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
        if (Math.abs(inputPosX) > ROTATION_THRESHOLD) {

            float rotationIncrementer = (float) Math.toRadians(
                    VRClientSettings.getWorldRotationIncrement()
            );
            float currentRotation = ClientContext.player.getRotationY();

            float newRotation = currentRotation
                    - rotationIncrementer * Math.signum(inputPosX);
            TaskInputRotation.getInstance().setInputRotation(newRotation);
            alreadyRotated = true;
        }




    }

    @Override
    protected Map<XRInteractionProfile, BindingPath> loadDefaults() {
        return Map.of(
                XRInteractionProfile.VALVE_INDEX,
                new BindingPath(
                        ValveIndexSet.VEC2_THUMBSTICK_RIGHT,
                        ValveIndexSet.VEC2_THUMBSTICK_LEFT
                )
        );
    }
}
