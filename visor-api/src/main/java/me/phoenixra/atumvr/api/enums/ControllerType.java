package me.phoenixra.atumvr.api.enums;


import lombok.Getter;

@Getter
public enum ControllerType {
    LEFT(0),
    RIGHT(1);


    private final int index;

    ControllerType(int index){
        this.index = index;
    }

    public ControllerType reversed(){
        if(this == LEFT) return RIGHT;
        else return LEFT;
    }

    public static ControllerType asIndex(int index){
        if(index == ControllerType.LEFT.getIndex()) return LEFT;
        return RIGHT;
    }


}
