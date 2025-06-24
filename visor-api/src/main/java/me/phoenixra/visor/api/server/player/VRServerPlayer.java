package me.phoenixra.visor.api.server.player;


import me.phoenixra.visor.api.common.ControllerHand;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface VRServerPlayer {
    @NotNull
    ServerPlayer getMcPlayer();


    boolean isCrawling();
    float getHeightScale();
    float getBowTension();
    @NotNull
    Vector3f getHmdDir();
    @NotNull
    Vector3f getHmdPos(@NotNull Player player);

    @NotNull
    default Vector3f getActiveHandPos(){
        return getControllerPos(ControllerHand.MAIN);
    }

    @NotNull
    default Vector3f getActiveHandDir(){
        return getControllerDir(ControllerHand.MAIN);
    }

    @NotNull
    default Vector3f getActiveHandVectorCustom(@NotNull Vector3f direction){
        return getControllerVectorCustom(
                ControllerHand.MAIN,
                direction
        );
    }

    @NotNull
    Vector3f getControllerPos(@NotNull ControllerHand controller);

    @NotNull
    Vector3f getControllerDir(@NotNull ControllerHand controller);

    @NotNull
    Vector3f getControllerVectorCustom(@NotNull ControllerHand controller,
                                   @NotNull Vector3fc direction);

    boolean isVr();
}
