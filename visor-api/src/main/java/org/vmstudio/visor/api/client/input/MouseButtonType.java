package org.vmstudio.visor.api.client.input;

import lombok.Getter;

public enum MouseButtonType {
    LEFT(0),
    RIGHT(1),
    MIDDLE(2);

    @Getter
    private final int id;

    MouseButtonType(int id){
        this.id = id;
    }

    public static MouseButtonType fromId(int id){
        if(id == 0){
            return LEFT;
        }
        if(id == 1){
            return RIGHT;
        }
        return MIDDLE;
    }
}
