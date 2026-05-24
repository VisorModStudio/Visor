package org.vmstudio.visor.api.common.player;

import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.Nullable;

/**
 * Player with Visor mod installed (VR or NonVR)
 */
public interface VisorPlayer {

    Player getMcPlayer();


    default boolean isVR(){
        return asVR() != null;
    }
    default boolean isNonVR(){
        return asVR() == null;
    }
    @Nullable
    default VRPlayer asVR() {
        return this instanceof VRPlayer vr ? vr : null;
    }

}
