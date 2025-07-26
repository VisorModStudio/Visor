package me.phoenixra.visor.core.client.network.players;

import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.util.Mth;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record VRRemotePlayerData(
        Quaternionfc offhandRotation,
        Vector3fc offhandDirection,
        Vector3fc offhandPosition,
        Quaternionfc mainHandRotation,
        Vector3fc mainHandDirection,
        Vector3fc mainHandPosition,
        Quaternionfc hmdRotation,
        Vector3fc hmdDirection,
        Vector3fc hmdPosition,
        float worldScale,
        float heightScale,
        int headsetModel,
        boolean leftHanded
) {

    public double getBodyYawRad() {
        Vector3fc vec3 = this.offhandPosition.sub(this.mainHandPosition, new Vector3f())
                .rotateY((-(float) Math.PI / 2F));

        if (this.leftHanded) {
            vec3 = vec3.mul(-1.0f, new Vector3f());
        }

        Vector3fc vec31 = VRMathUtils.lerpVector(vec3, this.hmdDirection, 0.5f);
        return Mth.atan2(-vec31.x(), vec31.z());
    }

}
