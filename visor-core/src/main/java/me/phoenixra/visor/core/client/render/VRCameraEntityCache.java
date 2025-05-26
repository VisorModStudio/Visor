package me.phoenixra.visor.core.client.render;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Data @AllArgsConstructor @NoArgsConstructor
public class VRCameraEntityCache {

    private double x, y, z;

    private double lastX, lastY, lastZ;

    private double previousX, previousY, previousZ;

    private float yaw, pitch;

    private float lastYaw, lastPitch;

    private float height;

    public void apply(LivingEntity entity){
        entity.setPosRaw(
                x,y,z
        );
        entity.xOld = lastX;
        entity.yOld = lastY;
        entity.zOld = lastZ;
        entity.xo = previousX;
        entity.yo = previousY;
        entity.zo = previousZ;
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yRotO = lastYaw;
        entity.xRotO = lastPitch;
        entity.yHeadRot =yaw;
        entity.yHeadRotO = lastYaw;
        entity.eyeHeight = height;
    }

}
