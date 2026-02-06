package me.phoenixra.visor.core.client.input.actions;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.atumvr.api.input.profile.types.OculusTouchProfile;
import me.phoenixra.atumvr.api.input.profile.types.ValveIndexProfile;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.api.client.input.action.framework.VisorActionButton;
import me.phoenixra.visor.core.client.gui.screens.GameMenuScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionMenu extends VisorActionButton {
    public static final String ID = "menu";

    @Getter
    private final boolean required = true;

    public ActionMenu(VisorActionSet actionSet) {
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
            MC.setScreen(new GameMenuScreen());
        }
    }

    @Override
    protected void onRelease() {

    }




    @Override
    protected Map<VRInteractionProfileType, ActionBinding> loadDefaults() {
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
                )
        );
    }


}
