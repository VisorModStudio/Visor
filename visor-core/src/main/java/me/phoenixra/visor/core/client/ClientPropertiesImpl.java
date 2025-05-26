package me.phoenixra.visor.core.client;

import lombok.Getter;
import me.phoenixra.visor.api.client.ClientProperties;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Getter
public class ClientPropertiesImpl implements ClientProperties {

    //@TODO use these when add input system

    //TICK
    private boolean moveModifiersAllowed;
    private boolean inputMovementAllowed;

    //RENDERING
    private boolean aimEffectsAllowed;
    private boolean vrHandsAllowed;



    public void preTick(){
        moveModifiersAllowed = true;
        inputMovementAllowed = true;

    }

    public void preRender(){

        aimEffectsAllowed = updateAimEffects();
        vrHandsAllowed = updateVrHands();
    }


    private boolean updateAimEffects(){
        if (MC.level == null) {
            return false;
        }
        if (MC.screen != null) {
            return false;
        }

        return true;
    }
    private boolean updateVrHands(){


        return true;
    }
}
