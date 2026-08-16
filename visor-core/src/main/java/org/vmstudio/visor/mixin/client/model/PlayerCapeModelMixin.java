package org.vmstudio.visor.mixin.client.model;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.extensions.client.entity.EntityRenderStateExtension;

// 1.21.4: the cape flap/lean orientation moved here from CapeLayer. For VR players
// CapeLayerMixin applies its own orientation to the pose stack, so the vanilla one has to be
// undone or the two would stack.
@Mixin(PlayerCapeModel.class)
public class PlayerCapeModelMixin<T extends PlayerRenderState> extends HumanoidModel<T> {

    @Shadow
    @Final
    private ModelPart cape;

    public PlayerCapeModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/PlayerRenderState;)V", at = @At("TAIL"))
    private void visor$resetStateWhenVR(CallbackInfo ci, @Local(argsOnly = true) PlayerRenderState renderState) {
        if (((EntityRenderStateExtension) renderState).visor$getVRPlayer() != null) {
            this.cape.resetPose();
            this.cape.z = 0F;
            this.body.resetPose();
        }
    }
}
