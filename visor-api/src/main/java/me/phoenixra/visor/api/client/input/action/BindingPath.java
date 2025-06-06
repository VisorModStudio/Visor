package me.phoenixra.visor.api.client.input.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.VRActionDataVec2;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;

@Data @AllArgsConstructor
public class BindingPath {
    private String defaultPath;
    private String leftHandedPath;

    public String getPath(boolean leftHanded){
        return leftHanded ? leftHandedPath : defaultPath;
    }
    public VRActionDataButton getButton(OpenXRProfileSet profile,
                                        boolean leftHanded){
        return profile.getButton(
                getPath(leftHanded)
        );
    }
    public VRActionDataVec2 getVec2(OpenXRProfileSet profile,
                                    boolean leftHanded){
        return profile.getVec2(
                getPath(leftHanded)
        );
    }
}
