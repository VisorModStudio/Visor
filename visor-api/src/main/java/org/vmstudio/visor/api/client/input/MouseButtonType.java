package org.vmstudio.visor.api.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import lombok.Getter;

public enum MouseButtonType {
    LEFT(InputConstants.MOUSE_BUTTON_LEFT),
    RIGHT(InputConstants.MOUSE_BUTTON_RIGHT),
    MIDDLE(InputConstants.MOUSE_BUTTON_MIDDLE);

    @Getter
    private final int id;

    MouseButtonType(int id){
        this.id = id;
    }

    public static MouseButtonType fromId(int id){
        if(id == InputConstants.MOUSE_BUTTON_LEFT){
            return LEFT;
        }
        if(id == InputConstants.MOUSE_BUTTON_RIGHT){
            return RIGHT;
        }
        return MIDDLE;
    }
}
