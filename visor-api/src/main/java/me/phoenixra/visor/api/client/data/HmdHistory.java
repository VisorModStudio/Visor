package me.phoenixra.visor.api.client.data;

import me.phoenixra.visor.api.common.utils.QuaternionFloatHistory;
import me.phoenixra.visor.api.common.utils.Vec3History;
import org.jetbrains.annotations.NotNull;

public interface HmdHistory {


    @NotNull
    Vec3History getPositionHistory();
    @NotNull
    Vec3History getPivotHistory();
    @NotNull
    QuaternionFloatHistory getRotationHistory();
}
