package me.phoenixra.visor.api.common.network.buffer;


import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.IClientPlayer;
import me.phoenixra.visor.api.client.data.IVRClientPose;
import me.phoenixra.visor.api.client.data.VRPoseStage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;


public record PlayerPoseBuffer(DevicePoseBuffer hmd,
                               DevicePoseBuffer mainHand,
                               DevicePoseBuffer offhand,
                               boolean leftHanded) implements BufferSerializable {


    @Override
    public void serialize(FriendlyByteBuf buffer) {
        this.hmd.serialize(buffer);
        this.mainHand.serialize(buffer);
        this.offhand.serialize(buffer);
        buffer.writeBoolean(this.leftHanded);
    }


    public static PlayerPoseBuffer deserialize(FriendlyByteBuf byteBuf) {
        return new PlayerPoseBuffer(
                DevicePoseBuffer.deserialize(byteBuf),
                DevicePoseBuffer.deserialize(byteBuf),
                DevicePoseBuffer.deserialize(byteBuf),
                byteBuf.readBoolean()
        );
    }

    public static PlayerPoseBuffer create(IClientPlayer clientPlayer,
                                          boolean leftHanded) {
        return new PlayerPoseBuffer(
                getHmdPose(clientPlayer),
                getControllerPose(clientPlayer, ControllerHand.MAIN),
                getControllerPose(clientPlayer, ControllerHand.OFFHAND),
                leftHanded
        );
    }

    private static DevicePoseBuffer getHmdPose(IClientPlayer clientPlayer) {
        IVRClientPose postTickPose = clientPlayer
                .getPose(VRPoseStage.POST_TICK);
        Vec3 position = postTickPose
                .getHmd().getPosition()
                .subtract(Minecraft.getInstance().player.position());
        Quaternionf orientation = postTickPose.getHmd().getRotationMatrix()
                .getNormalizedRotation(new Quaternionf());

        return new DevicePoseBuffer(position, orientation);
    }

    private static DevicePoseBuffer getControllerPose(IClientPlayer clientPlayer,
                                                      ControllerHand controller
    ) {
        IVRClientPose postTickPose = clientPlayer
            .getPose(VRPoseStage.POST_TICK);
        Vec3 position = postTickPose
                .getController(controller).getPosition()
                .subtract(Minecraft.getInstance().player.position());
        Quaternionf orientation = postTickPose
                .getController(controller)
                .getRotationMatrix().getNormalizedRotation(new Quaternionf());

        return new DevicePoseBuffer(position, orientation);
    }


}
