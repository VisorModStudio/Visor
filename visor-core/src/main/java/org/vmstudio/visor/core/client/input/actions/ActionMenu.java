package org.vmstudio.visor.core.client.input.actions;

import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.*;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.ActionBinding;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.core.client.gui.screens.VRPauseMenuScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class ActionMenu extends VRActionButton {
    public static final String ID = "menu";


    public ActionMenu(VRActionSet actionSet) {
        super(actionSet, ID);
    }


    @Override
    protected void onPress() {
        if (MC.screen != null) {
            InputHelper.pressKey(GLFW.GLFW_KEY_ESCAPE);
            InputHelper.releaseKey(GLFW.GLFW_KEY_ESCAPE);
        } else {
            if(MC.level == null){
                MC.setScreen(new TitleScreen());
                return;
            }
            MC.setScreen(new VRPauseMenuScreen());
        }
    }

    @Override
    protected void onRelease() {

    }


    @Override
    public boolean isCommon() {
        return true;
    }

    @Override
    public @NotNull Map<VRInteractionProfileType, ActionBinding> getDefaultBindings() {
        return Map.of(
                VRInteractionProfileType.VALVE_INDEX,
                new ActionBinding(
                        ValveIndexProfile.BUTTON_A_LEFT,
                        ValveIndexProfile.BUTTON_A_RIGHT
                ),
                VRInteractionProfileType.OCULUS_TOUCH,
                new ActionBinding(
                        OculusTouchProfile.BUTTON_Y,
                        OculusTouchProfile.BUTTON_B
                ),
                VRInteractionProfileType.VIVE,
                new ActionBinding(
                        ViveProfile.BUTTON_MENU_LEFT,
                        ViveProfile.BUTTON_MENU_RIGHT
                ),
                VRInteractionProfileType.VIVE_COSMOS,
                new ActionBinding(
                        ViveCosmosProfile.BUTTON_Y,
                        ViveCosmosProfile.BUTTON_B
                ),
                VRInteractionProfileType.HP_MIXED_REALITY,
                new ActionBinding(
                        HpMixedRealityProfile.BUTTON_Y,
                        HpMixedRealityProfile.BUTTON_B
                ),
                VRInteractionProfileType.WINDOWS_MOTION,
                new ActionBinding(
                        WindowsMotionProfile.BUTTON_MENU_LEFT,
                        WindowsMotionProfile.BUTTON_MENU_RIGHT
                )
        );
    }


}
