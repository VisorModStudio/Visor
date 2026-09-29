// #!MC-VERSION:: 1.21.10+
package org.vmstudio.visor.mixin.client.model;

import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;


@Mixin(PlayerCapeModel.class)
public abstract class PlayerCapeModelMixin extends PlayerModel {

    @Shadow @Final
    private ModelPart cape;

    public PlayerCapeModelMixin(ModelPart root, boolean slim) {
        super(root, slim);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void visor$vrCapePose(AvatarRenderState state, CallbackInfo ci) {
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        if (player == null || VRClientPlayers.getPlayer(player.getUUID()) == null) {
            return;
        }
        boolean armor = !player.getItemBySlot(EquipmentSlot.CHEST).isEmpty();

        this.body.resetPose();
        this.cape.setRotation(0F, 0F, 0F);
        if (armor) {
            this.cape.setPos(0F, state.isCrouching ? 0.8F : -0.85F, state.isCrouching ? 0.3F : -1.1F);
        } else {
            this.cape.setPos(0F, state.isCrouching ? 1.85F : 0F, state.isCrouching ? 1.4F : 0F);
        }
    }
}
