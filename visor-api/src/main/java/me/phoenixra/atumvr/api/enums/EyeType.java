package me.phoenixra.atumvr.api.enums;

import lombok.Getter;

@Getter
public enum EyeType {
    LEFT(0),
    RIGHT(1);

    private final int index;

    EyeType(int index){
       this.index = index;
    }

    public static EyeType asIndex(int index){
        if(index == EyeType.LEFT.index) return LEFT;
        return RIGHT;
    }
}
