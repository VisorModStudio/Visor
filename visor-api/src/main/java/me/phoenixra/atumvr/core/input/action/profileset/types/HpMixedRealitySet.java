package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.List;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class HpMixedRealitySet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.HP_MIXED_REALITY;



    // Menu button (shared)
    private BoolMultiAction menuButton;

    // Primary / Secondary
    private BoolMultiAction primaryButton; // A & X
    private BoolMultiAction secondaryButton; // B & Y

    // Squeeze
    private FloatMultiAction gripValue;

    // Trigger
    private FloatMultiAction triggerValue;

    // Thumb stick
    private Vec2MultiAction thumbstick;
    private BoolMultiAction thumbstickButton;

    public HpMixedRealitySet(OpenXRProvider provider) {
        super(provider, "hp_mixed_reality", "HP Mixed Reality Controller", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {

        // -------- MENU BUTTON --------
        menuButton = new BoolMultiAction(
                provider, this,
                "menu_button", "Menu Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/menu/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/menu/click")
                )
        );

        // -------- PRIMARY & SECONDARY BUTTONS --------
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

        // -------- GRIP --------
        gripValue = new FloatMultiAction(
                provider, this,
                "squeeze_value", "Squeeze Value",
                0.9f,   // press threshold
                0.85f,  // release threshold
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
                0.7f,   // press threshold
                0.65f,  // release threshold
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );

        // -------- THUMB STICK --------
        thumbstick = new Vec2MultiAction(
                provider, this,
                "thumbstick", "Thumbstick",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  new Vector2f(0,0))
                                .putDefaultBindings(PROFILE, "input/thumbstick"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, new Vector2f(0,0))
                                .putDefaultBindings(PROFILE, "input/thumbstick")
                )
        );

        thumbstickButton = new BoolMultiAction(
                provider, this,
                "thumbstick_button", "Thumbstick Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );

        return List.of(
                menuButton,
                primaryButton, secondaryButton,
                gripValue,
                triggerValue,
                thumbstick, thumbstickButton
        );
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
