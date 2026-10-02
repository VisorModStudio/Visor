// #!MC-VERSION:: 1.21.10+
package org.vmstudio.visor.mixin.client.renderer.particle;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(ItemPickupParticle.class)
public class ItemPickupParticleMixin {

    @Final
    @Shadow
    private Entity target;

    @Unique
    private Vector3fc visor$playerPos;

    private static final String RENDER = "renderCustom";

    // 1.21.9 extracts the pickup particle instead of rendering it, the three lerps moved out of this class:
    // the VR re-anchor to the headset has no hook here any more and is disabled until it is redesigned.

}
