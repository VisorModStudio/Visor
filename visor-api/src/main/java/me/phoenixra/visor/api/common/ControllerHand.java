package me.phoenixra.visor.api.common;

public enum ControllerHand {
    MAIN,
    OFFHAND;


    public ControllerHand reversed(){
        if(this == OFFHAND) return MAIN;
        else return OFFHAND;
    }

    public static ControllerHand fromInt(int id){
        if(id == 0) return MAIN;
        return OFFHAND;
    }
}
