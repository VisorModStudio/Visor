package me.phoenixra.visor.api.client.input.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import me.phoenixra.atumvr.api.input.action.VRActionDataButton;
import me.phoenixra.atumvr.api.input.action.VRActionDataVec2;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data @AllArgsConstructor
public class BindingPath {
    public static final String EMPTY_PATH = "null";
    public static final BindingPath EMPTY = new BindingPath(EMPTY_PATH, EMPTY_PATH);

    private String rightHandedPath;
    private String leftHandedPath;

    public void setPath(@NotNull String path, boolean leftHanded){
        if(leftHanded){
            leftHandedPath = path;
        }else{
            rightHandedPath = path;
        }
    }
    public String getPath(boolean leftHanded){
        return leftHanded ? leftHandedPath : rightHandedPath;
    }
    public @Nullable VRActionDataButton getButton(OpenXRProfileSet profile,
                                        boolean leftHanded){
        return profile.getButton(
                getPath(leftHanded)
        );
    }
    public @Nullable VRActionDataVec2 getVec2(OpenXRProfileSet profile,
                                              boolean leftHanded){
        return profile.getVec2(
                getPath(leftHanded)
        );
    }


}
