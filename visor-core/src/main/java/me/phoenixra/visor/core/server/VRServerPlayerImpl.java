package me.phoenixra.visor.core.server;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.network.buffer.DevicePoseBuffer;
import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.server.player.VRServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nullable;

public class VRServerPlayerImpl implements VRServerPlayer {
    @Getter
    public ServerPlayer mcPlayer;
    @Nullable
    @Getter
    @Setter
    private PlayerPoseBuffer playerPoseBuffer;

    public Vec3 offset = new Vec3(0.0D, 0.0D, 0.0D);

    public int networkVersion = 1;


    public float worldScale = 1.0F;
    @Getter
    public float heightScale = 1.0F;


    @Getter
    public float bowTension;

    @Getter
    public boolean crawling;

    @Getter
    @Setter
    private boolean vr = false;

    public VRServerPlayerImpl(ServerPlayer player) {
        this.mcPlayer = player;
    }



    @Override
    public @NotNull Vec3 getControllerVectorCustom(@NotNull ControllerHand controller,
                                                   @NotNull Vector3fc direction
    ) {

        var controllerPose = controller == ControllerHand.MAIN
                ? this.playerPoseBuffer.mainHand()
                : this.playerPoseBuffer.offhand();

        if (controllerPose != null) {
            Vector3f vector3 = controllerPose.orientation().transform(direction, new Vector3f());
            return new Vec3(vector3.x(), vector3.y(), vector3.z());
        } else {
            return this.mcPlayer.getLookAngle();
        }
    }

    @Override
    public @NotNull Vec3 getControllerDir(@NotNull ControllerHand controller) {
        return this.getControllerVectorCustom(controller, VRMathUtils.forwardVector);
    }

    @Override
    public @NotNull Vec3 getHmdDir() {
        if (this.playerPoseBuffer != null) {
            Vector3f vector3 = this.playerPoseBuffer.hmd().orientation()
                    .transform(VRMathUtils.forwardVector, new Vector3f());
            return new Vec3(vector3.x(), vector3.y(), vector3.z());
        }
        return this.mcPlayer.getLookAngle();
    }

    @Override
    public @NotNull Vec3 getHmdPos(@NotNull Player player) {
        if (this.playerPoseBuffer != null) {
            return this.playerPoseBuffer.hmd().position()
                    .add(player.position()).add(this.offset);
        }
        return player.position().add(0.0D, 1.62D, 0.0D);
    }

    @Override
    public @NotNull Vec3 getControllerPos(@NotNull ControllerHand controller) {
        if (this.playerPoseBuffer != null) {

            DevicePoseBuffer controllerState = controller == ControllerHand.MAIN
                    ? this.playerPoseBuffer.mainHand()
                    : this.playerPoseBuffer.offhand();

            return controllerState.position().add(this.mcPlayer.position())
                    .add(this.offset);
        }

        return this.mcPlayer.position().add(0.0D, 1.62D, 0.0D);
    }

}
