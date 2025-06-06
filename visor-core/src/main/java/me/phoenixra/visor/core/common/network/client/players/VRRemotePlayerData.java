package me.phoenixra.visor.core.common.network.client.players;

import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

public record VRRemotePlayerData(
        Quaternionfc offhandRotation,
        Vec3 offhandDirection,
        Vec3 offhandPosition,
        Quaternionfc mainHandRotation,
        Vec3 mainHandDirection,
        Vec3 mainHandPosition,
        Quaternionfc hmdRotation,
        Vec3 hmdDirection,
        Vec3 hmdPosition,
        float worldScale,
        float heightScale,
        int headsetModel,
        boolean leftHanded
) {

    public double getBodyYawRad() {
        Vec3 vec3 = this.offhandPosition.subtract(this.mainHandPosition)
                .yRot((-(float) Math.PI / 2F));

        if (this.leftHanded) {
            vec3 = vec3.scale(-1.0D);
        }

        Vec3 vec31 = VRMathUtils.lerpVector(vec3, this.hmdDirection, 0.5D);
        return Mth.atan2(-vec31.x, vec31.z);
    }

}
