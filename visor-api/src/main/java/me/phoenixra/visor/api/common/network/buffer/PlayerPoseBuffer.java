package me.phoenixra.visor.api.common.network.buffer;


import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.VRClientPlayer;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseDataType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;


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

    public static PlayerPoseBuffer create(VRClientPlayer vrPlayer,
                                          boolean leftHanded) {
        return new PlayerPoseBuffer(
                getHmdPose(vrPlayer),
                getControllerPose(vrPlayer, ControllerHand.MAIN),
                getControllerPose(vrPlayer, ControllerHand.OFFHAND),
                leftHanded
        );
    }

    private static DevicePoseBuffer getHmdPose(VRClientPlayer vrPlayer) {

        PoseData postTickPose = vrPlayer
                .getPoseData(PoseDataType.POST_TICK);
        Vector3f position = postTickPose
                .getHmd().getPosition()
                .sub(vrPlayer.getMcPlayer().position().toVector3f(), new Vector3f());
        Quaternionf orientation = postTickPose.getHmd().getRotation()
                .getNormalizedRotation(new Quaternionf());

        return new DevicePoseBuffer(position, orientation);
    }

    private static DevicePoseBuffer getControllerPose(VRClientPlayer vrPlayer,
                                                      ControllerHand controllerType
    ) {
        PoseData postTickPose = vrPlayer
            .getPoseData(PoseDataType.POST_TICK);
        Vector3f position = postTickPose
                .getController(controllerType).getPosition()
                .sub(vrPlayer.getMcPlayer().position().toVector3f(), new Vector3f());
        Quaternionf orientation = postTickPose
                .getController(controllerType)
                .getRotation().getNormalizedRotation(new Quaternionf());

        return new DevicePoseBuffer(position, orientation);
    }


}
