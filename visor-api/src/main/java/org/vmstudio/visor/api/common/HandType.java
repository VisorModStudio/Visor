package org.vmstudio.visor.api.common;

import me.phoenixra.atumvr.api.enums.ControllerType;
import org.vmstudio.visor.api.common.player.VRBodyPart;
import net.minecraft.world.InteractionHand;

public enum HandType {
    MAIN,
    OFFHAND;


    public VRBodyPart asBodyPart(){
        return this == MAIN ? VRBodyPart.MAIN_HAND : VRBodyPart.OFFHAND;
    }

    public InteractionHand asInteractionHand(){
        return this == MAIN ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public ControllerType getControllerType(boolean leftHanded){
        if(leftHanded){
            return this == MAIN ? ControllerType.LEFT : ControllerType.RIGHT;
        }else{
            return this == MAIN ? ControllerType.RIGHT : ControllerType.LEFT;
        }
    }

    public HandType reversed(){
        if(this == OFFHAND) return MAIN;
        else return OFFHAND;
    }

    public static HandType fromInt(int id){
        if(id == 0) return MAIN;
        return OFFHAND;
    }
    public static HandType fromMc(InteractionHand mcHand){
        return mcHand == InteractionHand.MAIN_HAND ? MAIN : OFFHAND;
    }

}
