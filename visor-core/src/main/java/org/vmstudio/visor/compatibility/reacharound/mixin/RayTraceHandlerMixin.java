package org.vmstudio.visor.compatibility.reacharound.mixin;

import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Pseudo;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.VisorClientState;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.player.VisorPlayer;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.core.client.VisorState;

@Mixin(targets = "com.spanser.reacharound.client.handler.RayTraceHandler", remap = false)
@MixinGate(classes = "com.spanser.reacharound.client.handler.RayTraceHandler")
@Pseudo

public class RayTraceHandlerMixin {

    @Inject(method = "getEntityParams", at = @At("HEAD"), cancellable = true)

    private static void visor$useControllerPose( Entity player,
                                                 CallbackInfoReturnable<Pair<Vec3,Vec3>> cir
    ){
        if(VisorState.get().isActive()){

            VRLocalPlayer vrPlayer = VisorAPI.client().getVRLocalPlayer();
            HandType activeHand = vrPlayer.getActiveHand();
            VRPlayerPoseClient playerPoseData = vrPlayer.getPoseData();

            VRPose handPose = playerPoseData.getHand(activeHand);


            cir.setReturnValue(Pair.of(handPose.getPositionVec3(), handPose.getDirectionVec3()));


        }
    }

}
