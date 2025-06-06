package me.phoenixra.visor.api.common;

import me.phoenixra.atumvr.api.enums.ControllerType;

public enum ControllerHand {
    MAIN,
    OFFHAND;


    public ControllerType getType(boolean leftHanded){
        if(leftHanded){
            return this == MAIN ? ControllerType.LEFT : ControllerType.RIGHT;
        }else{
            return this == MAIN ? ControllerType.RIGHT : ControllerType.LEFT;
        }
    }
    public ControllerHand reversed(){
        if(this == OFFHAND) return MAIN;
        else return OFFHAND;
    }

    public static ControllerHand fromInt(int id){
        if(id == 0) return MAIN;
        return OFFHAND;
    }

}
