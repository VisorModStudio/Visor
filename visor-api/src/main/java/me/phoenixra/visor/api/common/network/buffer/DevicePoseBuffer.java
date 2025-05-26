package me.phoenixra.visor.api.common.network.buffer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public record DevicePoseBuffer(Vec3 position,
                               Quaternionfc orientation) implements BufferSerializable {

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        serializeVec(buffer, this.position);
        serializeQuat(buffer, this.orientation);
    }
    public static DevicePoseBuffer deserialize(FriendlyByteBuf byteBuf) {
        return new DevicePoseBuffer(
                deserializeFVec3(byteBuf),
                deserializeVRQuaternion(byteBuf)
        );
    }

    public static void serializeVec(FriendlyByteBuf buffer, Vec3 vec3) {
        buffer.writeFloat((float) vec3.x);
        buffer.writeFloat((float) vec3.y);
        buffer.writeFloat((float) vec3.z);
    }

    public static void serializeQuat(FriendlyByteBuf buffer, Quaternionfc quat) {
        buffer.writeFloat(quat.x());
        buffer.writeFloat(quat.y());
        buffer.writeFloat(quat.z());
        buffer.writeFloat(quat.w());
    }


    public static Vec3 deserializeFVec3(FriendlyByteBuf buffer) {
        return new Vec3(
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
