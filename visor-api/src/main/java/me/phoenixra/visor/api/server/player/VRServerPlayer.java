package me.phoenixra.visor.api.server.player;


import me.phoenixra.visor.api.common.ControllerHand;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public interface VRServerPlayer {
    @NotNull
    ServerPlayer getMcPlayer();


    boolean isCrawling();
    float getHeight();
    float getBowTension();
    @NotNull
    Vec3 getHmdDir();
    @NotNull
    Vec3 getHmdPos(@NotNull Player player);

    @NotNull
    default Vec3 getActiveHandPos(){
        return getControllerPos(ControllerHand.MAIN);
    }

    @NotNull
    default Vec3 getActiveHandDir(){
        return getControllerDir(ControllerHand.MAIN);
    }

    @NotNull
    default Vec3 getActiveHandVectorCustom(@NotNull Vector3f direction){
        return getControllerVectorCustom(
                ControllerHand.MAIN,
                direction
        );
    }

    @NotNull
    Vec3 getControllerPos(@NotNull ControllerHand controller);

    @NotNull
    Vec3 getControllerDir(@NotNull ControllerHand controller);

    @NotNull
    Vec3 getControllerVectorCustom(@NotNull ControllerHand controller,
                                   @NotNull Vector3fc direction);

    boolean isVr();
}
