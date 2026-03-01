package org.vmstudio.visor.api.common.network.buffer;

import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record PoseElementBuffer(Vector3fc position,
                                Quaternionfc orientation) implements BufferSerializable {

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        serializeVec(buffer, this.position);
        serializeQuat(buffer, this.orientation);
    }
    public static PoseElementBuffer deserialize(FriendlyByteBuf byteBuf) {
        return new PoseElementBuffer(
                deserializeVec(byteBuf),
                deserializeVRQuaternion(byteBuf)
        );
    }

    public static void serializeVec(FriendlyByteBuf buffer, Vector3fc vec3) {
        buffer.writeFloat(vec3.x());
        buffer.writeFloat(vec3.y());
        buffer.writeFloat(vec3.z());
    }

    public static void serializeQuat(FriendlyByteBuf buffer, Quaternionfc quat) {
        buffer.writeFloat(quat.x());
        buffer.writeFloat(quat.y());
        buffer.writeFloat(quat.z());
        buffer.writeFloat(quat.w());
    }


    public static Vector3f deserializeVec(FriendlyByteBuf buffer) {
        return new Vector3f(
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }

    public static Quaternionf deserializeVRQuaternion(FriendlyByteBuf buffer) {
        return new Quaternionf(
                buffer.readFloat(), buffer.readFloat(), buffer.readFloat(),
                buffer.readFloat()
        );
    }

}
