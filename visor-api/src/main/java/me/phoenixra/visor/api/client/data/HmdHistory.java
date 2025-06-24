package me.phoenixra.visor.api.client.data;

import me.phoenixra.visor.api.common.utils.QuaternionFloatHistory;
import me.phoenixra.visor.api.common.utils.Vector3fHistory;
import org.jetbrains.annotations.NotNull;

public interface HmdHistory {


    @NotNull
    Vector3fHistory getPositionHistory();
    @NotNull
    Vector3fHistory getPivotHistory();
    @NotNull
    QuaternionFloatHistory getRotationHistory();
}
