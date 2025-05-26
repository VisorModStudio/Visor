package me.phoenixra.atumvr.core.enums;

import lombok.Getter;
import org.lwjgl.openxr.XR10;

import java.util.HashMap;

public enum XRInputActionType {

    BOOLEAN(XR10.XR_ACTION_TYPE_BOOLEAN_INPUT, XR10.XR_TYPE_ACTION_STATE_BOOLEAN),

    FLOAT(XR10.XR_ACTION_TYPE_FLOAT_INPUT, XR10.XR_TYPE_ACTION_STATE_FLOAT),

    VECTOR2F(XR10.XR_ACTION_TYPE_VECTOR2F_INPUT, XR10.XR_TYPE_ACTION_STATE_VECTOR2F),

    POSE(XR10.XR_ACTION_TYPE_POSE_INPUT, XR10.XR_TYPE_ACTION_STATE_POSE),


    HAPTIC(XR10.XR_ACTION_TYPE_VIBRATION_OUTPUT, XR10.XR_ACTION_TYPE_VIBRATION_OUTPUT);

    @Getter
    private final int id;

    @Getter
    private final int stateId;

    private static final HashMap<Integer, XRInputActionType> values = new HashMap<>();


    XRInputActionType(int id, int stateId){
        this.id = id;
        this.stateId = stateId;
    }

    public static XRInputActionType fromId(int id){
        if(values.isEmpty()){
            for(XRInputActionType event : values()){
                values.put(event.id,event);
            }
        }
        return values.get(id);
    }
}
