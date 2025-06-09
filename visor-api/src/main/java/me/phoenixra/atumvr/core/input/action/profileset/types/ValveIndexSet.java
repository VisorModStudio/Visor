package me.phoenixra.atumvr.core.input.action.profileset.types;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.VRActionDataVec2;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRMultiAction;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.types.multi.BoolButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatButtonMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.FloatMultiAction;
import me.phoenixra.atumvr.core.input.action.types.multi.Vec2MultiAction;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.*;

import static me.phoenixra.atumvr.core.input.action.OpenXRAction.LEFT_HAND_PATH;
import static me.phoenixra.atumvr.core.input.action.OpenXRAction.RIGHT_HAND_PATH;

@Getter
public class ValveIndexSet extends OpenXRProfileSet {
    private static final XRInteractionProfile PROFILE = XRInteractionProfile.VALVE_INDEX;

    //----------BUTTON----------
    public static final String BUTTON_SYSTEM_LEFT = "button.system.left";
    public static final String BUTTON_SYSTEM_RIGHT = "button.system.right";
    public static final String BUTTON_SYSTEM_TOUCH_LEFT = "button.system.touch.left";
    public static final String BUTTON_SYSTEM_TOUCH_RIGHT = "button.system.touch.right";


    public static final String BUTTON_A_LEFT = "button.a.left";
    public static final String BUTTON_A_RIGHT = "button.a.right";
    public static final String BUTTON_A_TOUCH_LEFT = "button.a.touch.left";
    public static final String BUTTON_A_TOUCH_RIGHT = "button.a.touch.right";


    public static final String BUTTON_B_LEFT = "button.b.left";
    public static final String BUTTON_B_RIGHT = "button.b.right";
    public static final String BUTTON_B_TOUCH_LEFT = "button.b.touch.left";
    public static final String BUTTON_B_TOUCH_RIGHT = "button.b.touch.right";


    public static final String BUTTON_GRIP_LEFT = "button.grip.left";
    public static final String BUTTON_GRIP_RIGHT = "button.grip.right";
    public static final String BUTTON_GRIP_FORCE_LEFT = "button.grip.force.left";
    public static final String BUTTON_GRIP_FORCE_RIGHT = "button.grip.force.right";



    public static final String BUTTON_TRIGGER_LEFT = "button.trigger.left";
    public static final String BUTTON_TRIGGER_RIGHT = "button.trigger.right";
    public static final String BUTTON_TRIGGER_CLICK_LEFT = "button.trigger.click.left";
    public static final String BUTTON_TRIGGER_CLICK_RIGHT = "button.trigger.click.right";
    public static final String BUTTON_TRIGGER_TOUCH_LEFT = "button.trigger.touch.left";
    public static final String BUTTON_TRIGGER_TOUCH_RIGHT = "button.trigger.touch.right";

    public static final String BUTTON_THUMBSTICK_LEFT = "button.thumbstick.left";
    public static final String BUTTON_THUMBSTICK_RIGHT = "button.thumbstick.right";
    public static final String BUTTON_THUMBSTICK_TOUCH_LEFT = "button.thumbstick.touch.left";
    public static final String BUTTON_THUMBSTICK_TOUCH_RIGHT = "button.thumbstick.touch.right";


    public static final String BUTTON_TRACKPAD_TOUCH_LEFT = "button.trackpad.touch.left";
    public static final String BUTTON_TRACKPAD_TOUCH_RIGHT = "button.trackpad.touch.right";
    public static final String BUTTON_TRACKPAD_FORCE_LEFT = "button.trackpad.force.left";
    public static final String BUTTON_TRACKPAD_FORCE_RIGHT = "button.trackpad.force.right";


    //----------VEC2----------
    public static final String VEC2_THUMBSTICK_LEFT = "vec2.thumbstick.left";
    public static final String VEC2_THUMBSTICK_RIGHT = "vec2.thumbstick.right";

    public static final String VEC2_TRACKPAD_LEFT = "vec2.trackpad.left";
    public static final String VEC2_TRACKPAD_RIGHT = "vec2.trackpad.right";

    // System Buttons
    private BoolButtonMultiAction systemButton;
    private BoolButtonMultiAction systemButtonTouch;

    // Button A
    private BoolButtonMultiAction buttonA;
    private BoolButtonMultiAction buttonTouchA;

    // Button B
    private BoolButtonMultiAction buttonB;
    private BoolButtonMultiAction buttonTouchB;

    // Grip
    private FloatButtonMultiAction gripValue;
    private FloatButtonMultiAction gripForce;

    // Trigger button
    private FloatButtonMultiAction triggerValue;
    private BoolButtonMultiAction triggerButton;
    private BoolButtonMultiAction triggerButtonTouch;

    // Thumb Stick
    private Vec2MultiAction thumbStick;
    private BoolButtonMultiAction thumbStickButton;
    private BoolButtonMultiAction thumbStickButtonTouch;

    // Trackpad
    private Vec2MultiAction trackpad;
    private BoolButtonMultiAction trackpadTouch;
    private FloatButtonMultiAction trackpadForce;


    private Map<String, VRActionDataButton> buttonMap;
    private Map<String, VRActionDataVec2> vec2Map;

    public ValveIndexSet(OpenXRProvider provider) {
        super(provider, "valve_index", "Valve Index", 0);
    }

    @Override
    protected List<OpenXRAction> loadActions(OpenXRProvider provider) {


        // -------- SYSTEM BUTTONS --------
        systemButton = new BoolButtonMultiAction(
                provider,
                this,
                "button.system", "System button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_SYSTEM_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/click"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_SYSTEM_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/click")
                )
        );

        systemButtonTouch = new BoolButtonMultiAction(
                provider,
                this,
                "button.system.touch", "System button touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_SYSTEM_TOUCH_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/touch"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_SYSTEM_TOUCH_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/system/touch")
                )
        );

        // -------- BUTTON PRIMARY --------

        buttonA = new BoolButtonMultiAction(
                provider,
                this,
                "button.primary", "Primary Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_A_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/click"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_A_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/click")
                )
        );

        buttonTouchA = new BoolButtonMultiAction(
                provider,
                this,
                "button.a.touch", "'A' Button Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_A_TOUCH_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/touch"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_A_TOUCH_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/a/touch")
                )
        );

        // -------- BUTTON SECONDARY --------

        buttonB = new BoolButtonMultiAction(
                provider,
                this,
                "button.b", "'B' Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_B_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/click"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_B_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/click")
                )
        );

        buttonTouchB = new BoolButtonMultiAction(
                provider,
                this,
                "button.b.touch", "B Button Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_B_TOUCH_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/touch"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_B_TOUCH_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/b/touch")
                )
        );

        // -------- GRIP --------
        gripValue = new FloatButtonMultiAction(
                provider,
                this,
                "button.grip",
                "Grip Value",
                0.9f,
                0.85f,
                List.of(
                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_GRIP_LEFT,
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/value"),

                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_GRIP_RIGHT,
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/value")
                )
        );

        gripForce = new FloatButtonMultiAction(
                provider,
                this,
                "button.grip.force",
                "Grip Force button",
                0.9f,
                0.85f,
                List.of(
                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_GRIP_FORCE_LEFT,
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/force"),

                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_GRIP_FORCE_RIGHT,
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/squeeze/force")
                )
        );


        // -------- TRIGGER BUTTON --------
        triggerValue = new FloatButtonMultiAction(
                provider,
                this,
                "button.trigger",
                "Trigger Value",
                0.7f,
                0.65f,
                List.of(
                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_TRIGGER_LEFT,
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trigger/value"),

                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_TRIGGER_RIGHT,
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trigger/value")
                )
        );

        triggerButton = new BoolButtonMultiAction(
                provider,
                this,
                "button.trigger.click", "Trigger Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_TRIGGER_CLICK_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/click"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_TRIGGER_CLICK_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/click")
                )
        );

        triggerButtonTouch = new BoolButtonMultiAction(
                provider,
                this,
                "button.trigger.touch", "Trigger Button Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_TRIGGER_TOUCH_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/touch"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_TRIGGER_TOUCH_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trigger/touch")
                )
        );



        // -------- THUMB STICK --------

        thumbStick = new Vec2MultiAction(
                provider,
                this,
                "vec2.thumbstick", "Thumbstick",
                List.of(
                        new Vec2MultiAction.SubActionVec2(
                                VEC2_THUMBSTICK_LEFT,
                                LEFT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/thumbstick"),

                        new Vec2MultiAction.SubActionVec2(
                                VEC2_THUMBSTICK_RIGHT,
                                RIGHT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/thumbstick")
                )
        );

        thumbStickButton = new BoolButtonMultiAction(
                provider,
                this,
                "button.thumbstick", "ThumbStick Button",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_THUMBSTICK_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/click"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_THUMBSTICK_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/click")
                )
        );

        thumbStickButtonTouch = new BoolButtonMultiAction(
                provider,
                this,
                "button.thumbstick.touch", "ThumbStick Button Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_THUMBSTICK_TOUCH_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/touch"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_THUMBSTICK_TOUCH_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/thumbstick/touch")
                )
        );

        // -------- TRACKPAD --------

        trackpad = new Vec2MultiAction(
                provider,
                this,
                "vec2.trackpad", "Trackpad",
                List.of(
                        new Vec2MultiAction.SubActionVec2(
                                VEC2_TRACKPAD_LEFT,
                                LEFT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/trackpad"),

                        new Vec2MultiAction.SubActionVec2(
                                VEC2_TRACKPAD_RIGHT,
                                RIGHT_HAND_PATH,
                                new Vector2f(0,0)
                        ).putDefaultBindings(PROFILE, "input/trackpad")
                )
        );

        trackpadTouch = new BoolButtonMultiAction(
                provider,
                this,
                "button.trackpad.touch", "Trackpad Touch",
                List.of(
                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_TRACKPAD_TOUCH_LEFT,
                                LEFT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trackpad/touch"),

                        new BoolButtonMultiAction.SubActionBoolButton(
                                BUTTON_TRACKPAD_TOUCH_RIGHT,
                                RIGHT_HAND_PATH,
                                false
                        ).putDefaultBindings(PROFILE, "input/trackpad/touch")
                )
        );

        trackpadForce = new FloatButtonMultiAction(
                provider,
                this,
                "button.trackpad.force",
                "Trackpad Force button",
                0.3f,
                0.1f,
                List.of(
                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_TRACKPAD_FORCE_LEFT,
                                LEFT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trackpad/force"),

                        new FloatButtonMultiAction.SubActionFloatButton(
                                BUTTON_TRACKPAD_FORCE_RIGHT,
                                RIGHT_HAND_PATH,
                                0f
                        ).putDefaultBindings(PROFILE, "input/trackpad/force")
                )
        );


        List<VRActionDataButton> listButton = new ArrayList<>();
        listButton.addAll(systemButton.getSubActionsAsButton());
        listButton.addAll(systemButtonTouch.getSubActionsAsButton());
        listButton.addAll(buttonA.getSubActionsAsButton());
        listButton.addAll(buttonTouchA.getSubActionsAsButton());
        listButton.addAll(buttonB.getSubActionsAsButton());
        listButton.addAll(buttonTouchB.getSubActionsAsButton());
        listButton.addAll(gripValue.getSubActionsAsButton());
        listButton.addAll(gripForce.getSubActionsAsButton());
        listButton.addAll(triggerValue.getSubActionsAsButton());
        listButton.addAll(triggerButton.getSubActionsAsButton());
        listButton.addAll(triggerButtonTouch.getSubActionsAsButton());
        listButton.addAll(thumbStickButton.getSubActionsAsButton());
        listButton.addAll(thumbStickButtonTouch.getSubActionsAsButton());
        listButton.addAll(trackpadTouch.getSubActionsAsButton());
        listButton.addAll(trackpadForce.getSubActionsAsButton());

        buttonMap = new HashMap<>();
        for(var entry : listButton){
            buttonMap.put(entry.getId(), entry);
        }

        List<VRActionDataVec2> listVec2 = new ArrayList<>();
        listVec2.addAll(thumbStick.getSubActionsAsVec2());
        listVec2.addAll(trackpad.getSubActionsAsVec2());

        vec2Map = new HashMap<>();
        for(var entry : listVec2){
            vec2Map.put(entry.getId(), entry);
        }


        return List.of(
                systemButton, systemButtonTouch,
                buttonA, buttonTouchA,
                buttonB, buttonTouchB,
                gripValue, gripForce,
                triggerValue, triggerButton, triggerButtonTouch,
                thumbStick, thumbStickButton, thumbStickButtonTouch,
                trackpad, trackpadTouch, trackpadForce
        );
    }

    @Override
    public Collection<String> getButtonIds() {
        return Collections.unmodifiableCollection(buttonMap.keySet());
    }

    @Override
    public VRActionDataButton getButton(@NotNull String id) {
        return buttonMap.get(id);
    }


    @Override
    public Collection<String> getVec2Ids() {
        return Collections.unmodifiableCollection(buttonMap.keySet());
    }

    @Override
    public VRActionDataVec2 getVec2(@NotNull String id) {
        return vec2Map.get(id);
    }

    @Override
    public @NotNull XRInteractionProfile getType() {
        return PROFILE;
    }
}
