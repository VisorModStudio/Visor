package me.phoenixra.atumvr.core.enums;

import lombok.Getter;
import org.lwjgl.openxr.XR10;

import java.util.HashMap;

public enum XRSessionStateChange {
    UNKNOWN(XR10.XR_SESSION_STATE_UNKNOWN),
    IDLE(XR10.XR_SESSION_STATE_IDLE),
    READY(XR10.XR_SESSION_STATE_READY),
    SYNCHRONIZED(XR10.XR_SESSION_STATE_SYNCHRONIZED),
    VISIBLE(XR10.XR_SESSION_STATE_VISIBLE),
    FOCUSED(XR10.XR_SESSION_STATE_FOCUSED),
    STOPPING(XR10.XR_SESSION_STATE_STOPPING),
    LOSS_PENDING(XR10.XR_SESSION_STATE_LOSS_PENDING),
    EXITING(XR10.XR_SESSION_STATE_EXITING);



    @Getter
    private final int id;

    private static final HashMap<Integer, XRSessionStateChange> values = new HashMap<>();


    XRSessionStateChange(int id){
        this.id = id;
    }

    public static XRSessionStateChange fromId(int id){
        if(values.isEmpty()){
            for(XRSessionStateChange event : values()){
                values.put(event.id,event);
            }
        }
        return values.get(id);
    }
}
