package me.phoenixra.visor.api.common.network.buffer;


import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.VRClientPlayer;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
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

    public static PlayerPoseBuffer create(VRClientPlayer clientPlayer,
                                          boolean leftHanded) {
        return new PlayerPoseBuffer(
                getHmdPose(clientPlayer),
                getControllerPose(clientPlayer, ControllerHand.MAIN),
                getControllerPose(clientPlayer, ControllerHand.OFFHAND),
                leftHanded
        );
    }

    private static DevicePoseBuffer getHmdPose(VRClientPlayer clientPlayer) {
        PoseData postTickPose = clientPlayer
                .getPose(PoseType.POST_TICK);
        Vec3 position = postTickPose
                .getHmd().getPosition()
                .subtract(Minecraft.getInstance().player.position());
        Quaternionf orientation = postTickPose.getHmd().getRotationMatrix()
                .getNormalizedRotation(new Quaternionf());

        return new DevicePoseBuffer(position, orientation);
    }

    private static DevicePoseBuffer getControllerPose(VRClientPlayer clientPlayer,
                                                      ControllerHand controller
    ) {
        PoseData postTickPose = clientPlayer
            .getPose(PoseType.POST_TICK);
        Vec3 position = postTickPose
                .getController(controller).getPosition()
                .subtract(Minecraft.getInstance().player.position());
        Quaternionf orientation = postTickPose
                .getController(controller)
                .getRotationMatrix().getNormalizedRotation(new Quaternionf());

        return new DevicePoseBuffer(position, orientation);
    }


}
