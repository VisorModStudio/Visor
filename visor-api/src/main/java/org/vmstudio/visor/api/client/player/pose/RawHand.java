package org.vmstudio.visor.api.client.player.pose;

import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.player.VRHandDataSource;
import org.vmstudio.visor.api.common.player.VRHandJointType;

public interface RawHand {

    boolean isTracking();

    @NotNull VRHandDataSource getDataSource();

    @NotNull RawHandJoint getJoint(@NotNull VRHandJointType type);
}
