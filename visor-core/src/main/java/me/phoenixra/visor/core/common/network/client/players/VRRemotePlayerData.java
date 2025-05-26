package me.phoenixra.visor.core.common.network.client.players;

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


}
