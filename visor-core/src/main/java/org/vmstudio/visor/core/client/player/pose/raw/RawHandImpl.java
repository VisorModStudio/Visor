package org.vmstudio.visor.core.client.player.pose.raw;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.pose.RawHand;
import org.vmstudio.visor.api.common.player.VRHandDataSource;
import org.vmstudio.visor.api.common.player.VRHandJointType;

public class RawHandImpl implements RawHand {

    @Getter @Setter
    private boolean tracking;

    @Getter @Setter
    private @NotNull VRHandDataSource dataSource = VRHandDataSource.UNKNOWN;

    private final RawHandJointImpl[] joints = new RawHandJointImpl[VRHandJointType.COUNT];

    public RawHandImpl(){
        for (int i = 0; i < joints.length; i++) {
            joints[i] = new RawHandJointImpl(VRHandJointType.fromIndex(i));
        }
    }

    @Override
    public @NotNull RawHandJointImpl getJoint(@NotNull VRHandJointType type) {
        return joints[type.ordinal()];
    }
}
