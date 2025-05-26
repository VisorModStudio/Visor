package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.pose.VRPoseRecord;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.HapticPulseAction;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.PoseMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class ValveIndexSet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.VALVE_INDEX;


    // System Buttons
    private BoolMultiAction systemButton;
    private BoolMultiAction systemButtonTouch;

    // Button A
    private BoolMultiAction primaryButton;
    private BoolMultiAction primaryButtonTouch;

    // Button B
    private BoolMultiAction secondaryButton;
    private BoolMultiAction secondaryButtonTouch;

    // Grip
    private FloatMultiAction gripValue;
    private OpenXRMultiAction<Float> gripForce;

    // Trigger button
    private FloatMultiAction triggerValue;
    private BoolMultiAction triggerButton;
    private BoolMultiAction triggerButtonTouch;

    // Thumb Stick
    private Vec2MultiAction thumbStick;
    private BoolMultiAction thumbStickButton;
    private BoolMultiAction thumbStickButtonTouch;

    // Trackpad
    private Vec2MultiAction trackpad;
    private BoolMultiAction trackpadTouch;
    private OpenXRMultiAction<Float> trackpadForce;

    public ValveIndexSet(OpenXRProvider provider) {
        super(provider, "valve_index", "Valve Index", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- SYSTEM BUTTONS --------
        systemButton = new BoolMultiAction(
                provider,
                this,
                "system_button", "System button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/click"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/click")
                )
        );

        systemButtonTouch = new BoolMultiAction(
                provider,
                this,
                "system_button_touch", "System button touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/touch"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/touch")
                )
        );

        // -------- BUTTON PRIMARY --------

        primaryButton = new BoolMultiAction(
                provider,
                this,
                "primary_button", "Primary Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/click"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/click")
                )
        );

        primaryButtonTouch = new BoolMultiAction(
                provider,
                this,
                "primary_button_touch", "Primary Button Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/touch"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/touch")
                )
        );

        // -------- BUTTON SECONDARY --------

        secondaryButton = new BoolMultiAction(
                provider,
                this,
                "secondary_button", "Secondary Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/click"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/click")
                )
        );

        secondaryButtonTouch = new BoolMultiAction(
                provider,
                this,
                "secondary_button_touch", "Secondary Button Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/touch"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/touch")
                )
        );

        // -------- GRIP --------
        gripValue = new FloatMultiAction(
                provider,
                this,
                "grip_value",
                "Grip Value",
                0.9f,
                0.85f,
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/value"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/value")
                )
        );

        gripForce = new FloatMultiAction(
                provider,
                this,
                "grip_force",
                "Grip Force",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/force"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/force")
                )
        );


        // -------- TRIGGER BUTTON --------
        triggerValue = new FloatMultiAction(
                provider,
                this,
                "trigger_value",
                "Trigger Value",
                0.7f,
                0.65f,
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trigger/value"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );

        triggerButton = new BoolMultiAction(
                provider,
                this,
                "trigger_button", "Trigger Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/click"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/click")
                )
        );

        triggerButtonTouch = new BoolMultiAction(
                provider,
                this,
                "trigger_button_touch", "Trigger Button Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/touch"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/touch")
                )
        );



        // -------- THUMB STICK --------

        thumbStick = new Vec2MultiAction(
                provider,
                this,
                "thumbstick", "Thumbstick",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/thumbstick"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/thumbstick")
                )
        );

        thumbStickButton = new BoolMultiAction(
                provider,
                this,
                "thumbstick_button", "ThumbStick Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/click"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );

        thumbStickButtonTouch = new BoolMultiAction(
                provider,
                this,
                "thumbstick_button_touch", "ThumbStick Button Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/touch"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/touch")
                )
        );

        // -------- TRACKPAD --------

        trackpad = new Vec2MultiAction(
                provider,
                this,
                "trackpad", "Trackpad",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/trackpad"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/trackpad")
                )
        );

        trackpadTouch = new BoolMultiAction(
                provider,
                this,
                "trackpad_touch", "Trackpad Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trackpad/touch"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trackpad/touch")
                )
        );

        trackpadForce = new FloatMultiAction(
                provider,
                this,
                "trackpad_force",
                "Trackpad Force",
                List.of(
                        new OpenXRMultiAction.SubAction<>(
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trackpad/force"),

                        new OpenXRMultiAction.SubAction<>(
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trackpad/force")
                )
        );

        return List.of(
                systemButton, systemButtonTouch,
                primaryButton, primaryButtonTouch,
                secondaryButton, secondaryButtonTouch,
                gripValue, gripForce,
                triggerValue, triggerButton, triggerButtonTouch,
                thumbStick, thumbStickButton, thumbStickButtonTouch,
                trackpad, trackpadTouch, trackpadForce
        );
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
