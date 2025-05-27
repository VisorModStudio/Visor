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
public class ViveSet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.VIVE;


    // System & Menu Buttons
    private BoolButtonMultiAction systemButton;
    private BoolButtonMultiAction menuButton;

    // Grip
    private BoolButtonMultiAction gripButton;

    // Trigger
    private FloatMultiAction triggerValue;
    private BoolButtonMultiAction triggerButton;

    // Trackpad
    private Vec2MultiAction trackpad;
    private BoolButtonMultiAction trackpadTouch;
    private BoolButtonMultiAction trackpadButton;

    public ViveSet(OpenXRProvider provider) {
        super(provider, "vive", "Vive Controller", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- SYSTEM & MENU BUTTONS --------
        systemButton = new BoolButtonMultiAction(
                provider,
                this,
                "system_button",
                "System Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/system/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/system/click")
                )
        );

        menuButton = new BoolButtonMultiAction(
                provider,
                this,
                "menu_button",
                "Menu Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/menu/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/menu/click")
                )
        );

        // -------- GRIP --------
        gripButton = new BoolButtonMultiAction(
                provider,
                this,
                "grip_button",
                "Grip Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/squeeze/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/squeeze/click")
                )
        );

        // -------- TRIGGER BUTTON --------
        triggerValue = new FloatMultiAction(
                provider,
                this,
                "trigger_value",
                "Trigger Value",
                0.7f,   // click threshold
                0.65f,  // release threshold
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, 0f)
                                .putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );

        triggerButton = new BoolButtonMultiAction(
                provider,
                this,
                "trigger_button",
                "Trigger Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trigger/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trigger/click")
                )
        );

        // -------- TRACKPAD --------
        trackpad = new Vec2MultiAction(
                provider,
                this,
                "trackpad",
                "Trackpad",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  new Vector2f(0, 0))
                                .putDefaultBindings(PROFILE, "input/trackpad"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, new Vector2f(0, 0))
                                .putDefaultBindings(PROFILE, "input/trackpad")
                )
        );

        trackpadTouch = new BoolButtonMultiAction(
                provider,
                this,
                "trackpad_touch",
                "Trackpad Touch",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trackpad/touch"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trackpad/touch")
                )
        );

        trackpadButton = new BoolButtonMultiAction(
                provider,
                this,
                "trackpad_button",
                "Trackpad Button",
                List.of(
                        new OpenXRMultiAction.SubAction<>(LEFT_HAND_PATH,  false)
                                .putDefaultBindings(PROFILE, "input/trackpad/click"),
                        new OpenXRMultiAction.SubAction<>(RIGHT_HAND_PATH, false)
                                .putDefaultBindings(PROFILE, "input/trackpad/click")
                )
        );

        return List.of(
                systemButton, menuButton,
                gripButton,
                triggerValue, triggerButton,
                trackpad, trackpadTouch, trackpadButton
        );
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
