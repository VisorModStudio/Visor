package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.OpenXRSingleAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import me.phoenixra.atumvr.core.input.action.types.single.BoolButtonAction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class ViveCosmosSet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.VIVE_COSMOS;

    // Single-hand only buttons
    private OpenXRSingleAction<Boolean> menuButton;
    private OpenXRSingleAction<Boolean> systemButton;

    // Primary/Secondary Buttons
    private BoolButtonMultiAction primaryButton;  //A & X
    private BoolButtonMultiAction secondaryButton;  // B & Y

    // Shoulder & Grip
    private BoolButtonMultiAction shoulderButton;
    private BoolButtonMultiAction gripButton;

    // Trigger
    private FloatMultiAction triggerValue;
    private BoolButtonMultiAction triggerButton;

    // Thumb stick
    private Vec2MultiAction thumbStick;
    private BoolButtonMultiAction thumbStickButton;
    private BoolButtonMultiAction thumbStickTouch;

    public ViveCosmosSet(OpenXRProvider provider) {
        super(provider, "vive_cosmos", "Vive Cosmos Controller", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- SINGLE-HAND BUTTONS --------
        menuButton = new BoolButtonAction(
                provider, this,
                "menu_button", "Menu Button"
        ).putDefaultBindings(PROFILE, LEFT_HAND_PATH+"/input/menu/click");

        systemButton = new BoolButtonAction(
                provider, this,
                "system_button", "System Button"
        ).putDefaultBindings(PROFILE, RIGHT_HAND_PATH+"/input/system/click");

        // -------- PRIMARY & SECONDARY --------
        primaryButton = new BoolButtonMultiAction(
                provider, this,
                "primary_button", "Primary Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/x/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/a/click")
                )
        );

        secondaryButton = new BoolButtonMultiAction(
                provider, this,
                "secondary_button", "Secondary Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/y/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/b/click")
                )
        );

        // -------- SHOULDER & GRIP --------
        shoulderButton = new BoolButtonMultiAction(
                provider, this,
                "shoulder_button", "Shoulder Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/shoulder/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/shoulder/click")
                )
        );

        gripButton = new BoolButtonMultiAction(
                provider, this,
                "squeeze_button", "Squeeze Button",
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
                0.7f,   // press threshold
                0.65f,  // release threshold
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );

        triggerButton = new BoolButtonMultiAction(
                provider, this,
                "trigger_button", "Trigger Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trigger/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trigger/click")
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
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );

        thumbStickTouch = new BoolButtonMultiAction(
                provider, this,
                "thumbstick_touch", "Thumbstick Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/touch")
                )
        );

        return List.of(
                menuButton, systemButton,
                primaryButton, secondaryButton,
                shoulderButton, gripButton,
                triggerValue, triggerButton,
                thumbStick, thumbStickButton, thumbStickTouch
        );
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
