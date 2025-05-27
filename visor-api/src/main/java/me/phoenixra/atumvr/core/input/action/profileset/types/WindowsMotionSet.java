package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class WindowsMotionSet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.WINDOWS_MOTION;


    // Menu button
    private BoolButtonMultiAction menuButton;

    // Grip
    private BoolButtonMultiAction gripButton;

    // Trigger
    private FloatMultiAction triggerValue;

    // Thumb stick
    private Vec2MultiAction thumbStick;
    private BoolButtonMultiAction thumbStickButton;

    // Trackpad
    private Vec2MultiAction trackpad;
    private BoolButtonMultiAction trackpadTouch;
    private BoolButtonMultiAction trackpadButton;

    public WindowsMotionSet(OpenXRProvider provider) {
        super(provider, "windows_motion", "Windows Motion Controller", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- MENU BUTTON --------
        menuButton = new BoolButtonMultiAction(
                provider, this,
                "menu_button", "Menu Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/menu/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/menu/click")
                )
        );

        // -------- GRIP --------
        gripButton = new BoolButtonMultiAction(
                provider, this,
                "grip_button", "Grip Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/squeeze/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/squeeze/click")
                )
        );

        // -------- TRIGGER BUTTON --------
        triggerValue = new FloatMultiAction(
                provider, this,
                "trigger_value", "Trigger Value",
                0.7f,
                0.65f,
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );

        // -------- THUMB STICK --------
        thumbStick = new Vec2MultiAction(
                provider, this,
                "thumbstick", "Thumbstick",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  new Vector2f(0, 0))
                                .putDefaultBindings(PROFILE, "input/thumbstick"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, new Vector2f(0, 0))
                                .putDefaultBindings(PROFILE, "input/thumbstick")
                )
        );

        thumbStickButton = new BoolButtonMultiAction(
                provider, this,
                "thumbstick_button", "Thumbstick Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );

        // -------- TRACKPAD --------
        trackpad = new Vec2MultiAction(
                provider, this,
                "trackpad", "Trackpad",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  new Vector2f(0, 0))
                                .putDefaultBindings(PROFILE, "input/trackpad"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, new Vector2f(0, 0))
                                .putDefaultBindings(PROFILE, "input/trackpad")
                )
        );

        trackpadTouch = new BoolButtonMultiAction(
                provider, this,
                "trackpad_touch", "Trackpad Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trackpad/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trackpad/touch")
                )
        );

        trackpadButton = new BoolButtonMultiAction(
                provider, this,
                "trackpad_button", "Trackpad Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trackpad/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trackpad/click")
                )
        );

        return List.of(
                menuButton,
                gripButton,
                triggerValue,
                thumbStick, thumbStickButton,
                trackpad, trackpadTouch, trackpadButton
        );
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
