package org.vmstudio.visor.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;

@Mixin(MobRenderer.class)
public class MobRendererMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getRopeHoldPosition(F)Lnet/minecraft/world/phys/Vec3;"), method = "renderLeash")
    public Vec3 visor$vrRenderLeash(Entity instance, float partialTick) {
        if (VRRenderState.getPhase().isNotVRWorld()) {
            return instance.getRopeHoldPosition(partialTick);
        }

        if (!(instance instanceof Player player)) {
            return instance.getRopeHoldPosition(partialTick);
        }

        var vrPlayer = VRClientPlayers.getPlayer(player);
        if (vrPlayer == null) {
            return instance.getRopeHoldPosition(partialTick);
        }

        return new Vec3(
                new Vector3f(
                        vrPlayer.getPoseData(PlayerPoseType.RENDER)
                                .getHand(HandType.MAIN)
                                .getPosition()
                )
        );
    }
}