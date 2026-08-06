package org.vmstudio.visor.api.common.network.buffer;

import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.common.player.VRHandJointType;

import java.util.ArrayList;
import java.util.List;

public record PoseHandBuffer(@NotNull List<PoseHandJointBuffer> joints) implements VRDataBuffer {

    @Override
    public void serialize(FriendlyByteBuf buffer) {
        PoseHandJointBuffer[] byIndex = new PoseHandJointBuffer[VRHandJointType.COUNT];
        int bitMask = 0;
        for (PoseHandJointBuffer joint : joints) {
            int i = joint.type().ordinal();
            byIndex[i] = joint;
            bitMask |= 1 << i;
        }
        buffer.writeVarInt(bitMask);
        for (PoseHandJointBuffer joint : byIndex) {
            if (joint != null) {
                joint.serialize(buffer);
            }
        }
    }

    public static PoseHandBuffer deserialize(FriendlyByteBuf byteBuf) {
        int bitMask = byteBuf.readVarInt();
        List<PoseHandJointBuffer> joints = new ArrayList<>();
        for (int i = 0; i < VRHandJointType.COUNT; i++) {
            if ((bitMask & (1 << i)) != 0) {
                joints.add(PoseHandJointBuffer.deserialize(VRHandJointType.fromIndex(i), byteBuf));
            }
        }
        return new PoseHandBuffer(joints);
    }

    public static PoseHandBuffer createEmpty() {
        return new PoseHandBuffer(List.of());
    }

    public boolean isEmpty() {
        return joints.isEmpty();
    }

    public @Nullable PoseHandJointBuffer getJoint(@NotNull VRHandJointType type) {
        for (PoseHandJointBuffer joint : joints) {
            if (joint.type() == type) {
                return joint;
            }
        }
        return null;
    }
}
