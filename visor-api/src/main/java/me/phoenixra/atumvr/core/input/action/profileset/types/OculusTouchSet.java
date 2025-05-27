package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import me.phoenixra.atumvr.core.input.action.types.single.BoolButtonAction;
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
    private BoolButtonMultiAction primaryButton;
    private BoolButtonMultiAction primaryButtonTouch;
    private BoolButtonMultiAction secondaryButton;
    private BoolButtonMultiAction secondaryButtonTouch;

    // Squeeze & Trigger
    private FloatButtonMultiAction gripValue;
    private FloatButtonMultiAction triggerValue;
    private BoolButtonMultiAction triggerTouch;

    // Thumb stick
    private Vec2MultiAction thumbStick;
    private BoolButtonMultiAction thumbStickButton;
    private BoolButtonMultiAction thumbStickTouch;

    // Thumb rest touch
    private BoolButtonMultiAction thumbRestTouch;


    public OculusTouchSet(OpenXRProvider provider) {
        super(provider, "oculus_touch", "Oculus Touch Controller", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- MENU & SYSTEM BUTTONS --------
        menuButton = new BoolButtonAction(
                provider, this,
                "menu_button", "Menu Button"
        ).putDefaultBindings(PROFILE, LEFT_HAND_PATH+"/input/menu/click");

        systemButton = new BoolButtonAction(
                provider, this,
                "system_button", "System Button"
        ).putDefaultBindings(PROFILE, RIGHT_HAND_PATH+"/input/system/click");

        // -------- PRIMARY BUTTONS (X/A) --------
        primaryButton = new BoolButtonMultiAction(
                provider, this,
                "primary_button", "Primary Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/x/click"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/a/click")
                )
        );
        primaryButtonTouch = new BoolButtonMultiAction(
                provider, this,
                "primary_button_touch", "Primary Button Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/x/touch"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/a/touch")
                )
        );
        // -------- SECONDARY (Y/B) --------
        secondaryButton = new BoolButtonMultiAction(
                provider, this,
                "secondary_button", "Secondary Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/y/click"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/b/click")
                )
        );
        secondaryButtonTouch = new BoolButtonMultiAction(
                provider, this,
                "secondary_button_touch", "Secondary Button Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/y/touch"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/b/touch")
                )
        );

        // -------- GRIP --------
        gripValue = new FloatButtonMultiAction(
                provider, this,
                "grip_value", "Grip Value",
                0.9f,   // press
                0.85f,  // release
                List.of(
                        new FloatButtonMultiAction.SubActionFloatButton(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/squeeze/value"),
                        new FloatButtonMultiAction.SubActionFloatButton(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/squeeze/value")
                )
        );

        // -------- TRIGGER BUTTON --------
        triggerValue = new FloatButtonMultiAction(
                provider, this,
                "trigger_value", "Trigger Value",
                0.7f,   // press
                0.65f,  // release
                List.of(
                        new FloatButtonMultiAction.SubActionFloatButton(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value"),
                        new FloatButtonMultiAction.SubActionFloatButton(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );
        triggerTouch = new BoolButtonMultiAction(
                provider, this,
                "trigger_touch", "Trigger Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trigger/touch"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
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
        thumbStickButton = new BoolButtonMultiAction(
                provider, this,
                "thumbstick_button", "Thumbstick Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );
        thumbStickTouch = new BoolButtonMultiAction(
                provider, this,
                "thumbstick_touch", "Thumbstick Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/touch"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/touch")
                )
        );

        // -------- THUMB REST --------
        thumbRestTouch = new BoolButtonMultiAction(
                provider, this,
                "thumbrest_touch", "Thumbrest Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbrest/touch"),
                        new BoolButtonMultiAction.SubActionBoolButton(RIGHT_HAND_PATH, false)
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
