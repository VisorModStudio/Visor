package org.vmstudio.visor.core.client.render;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

@Data @AllArgsConstructor @NoArgsConstructor
public class VRCameraEntityCache {

    private double x, y, z;

    private double lastX, lastY, lastZ;

    private double previousX, previousY, previousZ;

    private float yaw, pitch;

    private float lastYaw, lastPitch;

    private float height;

    public void apply(Entity entity){
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
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yHeadRot = yaw;
            livingEntity.yHeadRotO = lastYaw;
        }
        entity.eyeHeight = height;
    }

    public Vec3 getInterpolatedPos(float partialTicks) {
        return new Vec3(
                Mth.lerp(partialTicks, this.lastX, this.x),
                Mth.lerp(partialTicks, this.lastY, this.y),
                Mth.lerp(partialTicks, this.lastZ, this.z)
        );
    }
}
