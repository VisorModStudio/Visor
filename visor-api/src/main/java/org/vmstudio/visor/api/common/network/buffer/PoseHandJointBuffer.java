package org.vmstudio.visor.api.common.network.buffer;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.common.player.VRHandJointType;

public record PoseHandJointBuffer(VRHandJointType type,
                                  Vector3fc position,
                                  Quaternionfc orientation) implements VRDataBuffer {

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        PoseElementBuffer.serializeVec(buffer, this.position);
        PoseElementBuffer.serializeQuat(buffer, this.orientation);
    }

    public static PoseHandJointBuffer deserialize(VRHandJointType type,
                                                  FriendlyByteBuf byteBuf) {
        return new PoseHandJointBuffer(
                type,
                PoseElementBuffer.deserializeVec(byteBuf),
                PoseElementBuffer.deserializeVRQuaternion(byteBuf)
        );
    }
}
