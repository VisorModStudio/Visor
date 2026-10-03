package org.vmstudio.visor.core.client.input.actions.game;

import com.mojang.blaze3d.platform.InputConstants;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.*;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GameActionJump extends VRActionButton {
    public static final String ID = "jump";



    public GameActionJump(VRActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        InputHelper.pressKey(InputConstants.KEY_SPACE);
    }

    @Override
    protected void onRelease() {
        InputHelper.releaseKey(InputConstants.KEY_SPACE);
    }



    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.BUTTON_A_RIGHT,
                        ValveIndexProfile.BUTTON_A_LEFT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        OculusTouchProfile.BUTTON_B,
                        OculusTouchProfile.BUTTON_Y
                ),
                VRInteractionProfileType.VIVE,
                new ActionBinding(
                        ViveProfile.BUTTON_TRACKPAD_RIGHT,
                        ViveProfile.BUTTON_TRACKPAD_LEFT
                ),
                VRInteractionProfileType.VIVE_COSMOS,
                new ActionBinding(
                        ViveCosmosProfile.BUTTON_B,
                        ViveCosmosProfile.BUTTON_Y
                ),
                VRInteractionProfileType.HP_MIXED_REALITY,
                new ActionBinding(
                        HpMixedRealityProfile.BUTTON_B,
                        HpMixedRealityProfile.BUTTON_Y
                ),
                VRInteractionProfileType.WINDOWS_MOTION,
                new ActionBinding(
                        WindowsMotionProfile.BUTTON_MENU_RIGHT,
                        WindowsMotionProfile.BUTTON_MENU_LEFT
                ),
                VRInteractionProfileType.STEAM_FRAME,
                new ActionBinding(
                        SteamFrameProfile.BUTTON_B,
                        SteamFrameProfile.BUTTON_DPAD_UP
                )
        );
    }

}
