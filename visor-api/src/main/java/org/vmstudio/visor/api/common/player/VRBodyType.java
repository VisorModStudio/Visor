package org.vmstudio.visor.api.common.player;

public enum VRBodyType {
    HANDS_ONLY,
    FULL_BODY;

    VRBodyType(){
    }

    public boolean isFullBody(){
        return this == FULL_BODY;
    }
}
