package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import me.phoenixra.atumvr.core.input.action.types.single.BoolAction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class OculusTouchSet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.OCULUS_TOUCH;


    // Single-hand only buttons
    private OpenXRSingleAction<Boolean> menuButton;
    private OpenXRSingleAction<Boolean> systemButton;

    // Primary / Secondary buttons
    private BoolMultiAction primaryButton;
    private BoolMultiAction primaryButtonTouch;
    private BoolMultiAction secondaryButton;
    private BoolMultiAction secondaryButtonTouch;

    // Squeeze & Trigger
    private FloatMultiAction gripValue;
    private FloatMultiAction triggerValue;
    private BoolMultiAction triggerTouch;

    // Thumb stick
    private Vec2MultiAction thumbStick;
    private BoolMultiAction thumbStickButton;
    private BoolMultiAction thumbStickTouch;

    // Thumb rest touch
    private BoolMultiAction thumbRestTouch;


    public OculusTouchSet(OpenXRProvider provider) {
        super(provider, "oculus_touch", "Oculus Touch Controller", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- MENU & SYSTEM BUTTONS --------
        menuButton = new BoolAction(
                provider, this,
                "menu_button", "Menu Button"
        ).putDefaultBindings(PROFILE, LEFT_HAND_PATH+"/input/menu/click");

        systemButton = new BoolAction(
                provider, this,
                "system_button", "System Button"
        ).putDefaultBindings(PROFILE, RIGHT_HAND_PATH+"/input/system/click");

        // -------- PRIMARY BUTTONS (X/A) --------
        primaryButton = new BoolMultiAction(
                provider, this,
                "primary_button", "Primary Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/x/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/a/click")
                )
        );
        primaryButtonTouch = new BoolMultiAction(
                provider, this,
                "primary_button_touch", "Primary Button Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/x/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/a/touch")
                )
        );
        // -------- SECONDARY (Y/B) --------
        secondaryButton = new BoolMultiAction(
                provider, this,
                "secondary_button", "Secondary Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/y/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/b/click")
                )
        );
        secondaryButtonTouch = new BoolMultiAction(
                provider, this,
                "secondary_button_touch", "Secondary Button Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/y/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/b/touch")
                )
        );

        // -------- GRIP --------
        gripValue = new FloatMultiAction(
                provider, this,
                "grip_value", "Grip Value",
                0.9f,   // press
                0.85f,  // release
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/squeeze/value"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/squeeze/value")
                )
        );

        // -------- TRIGGER BUTTON --------
        triggerValue = new FloatMultiAction(
                provider, this,
                "trigger_value", "Trigger Value",
                0.7f,   // press
                0.65f,  // release
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );
        triggerTouch = new BoolMultiAction(
                provider, this,
                "trigger_touch", "Trigger Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trigger/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trigger/touch")
                )
        );

        // -------- THUMB STICK --------
        thumbStick = new Vec2MultiAction(
                provider, this,
                "thumbstick", "Thumbstick",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  new Vector2f(0,0))
                                .putDefaultBindings(PROFILE, "input/thumbstick"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, new Vector2f(0,0))
                                .putDefaultBindings(PROFILE, "input/thumbstick")
                )
        );
        thumbStickButton = new BoolMultiAction(
                provider, this,
                "thumbstick_button", "Thumbstick Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );
        thumbStickTouch = new BoolMultiAction(
                provider, this,
                "thumbstick_touch", "Thumbstick Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/touch")
                )
        );

        // -------- THUMB REST --------
        thumbRestTouch = new BoolMultiAction(
                provider, this,
                "thumbrest_touch", "Thumbrest Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbrest/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbrest/touch")
                )
        );

        return List.of(
                menuButton, systemButton,
                primaryButton, primaryButtonTouch,
                secondaryButton, secondaryButtonTouch,
                gripValue,
                triggerValue, triggerTouch,
                thumbStick, thumbStickButton, thumbStickTouch,
                thumbRestTouch
        );
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
