// #!MC-VERSION:: 1.21.10+
package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.player.BackLayerPlacement;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.player.AbstractClientPlayer;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderState;

@Mixin(WingsLayer.class)
public abstract class ElytraLayerMixin<S extends HumanoidRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
    // ElytraModel.setupAnim drops the wings by this while crouching,
    //VR don't need that, so, cancelled
    @Unique
    private static final float VANILLA_CROUCH_WING_DROP = 3F;
    // keeps the wings out of the player's view while flying
    @Unique
    private static final float FALL_FLYING_DROP = 2F;

    @Unique
    private final BackLayerPlacement visor$placement = new BackLayerPlacement();

    @Unique
    private final Vector3f visor$offset = new Vector3f();

    public ElytraLayerMixin(RenderLayerParent<S, M> renderer) {
        super(renderer);
    }

    @WrapOperation(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$attachElytraToBack(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) HumanoidRenderState state) {
        AbstractClientPlayer player = VRPlayerRenderState.playerOf(state);
        var vrPlayer = player == null ? null : VRClientPlayers.getPlayer(player.getUUID());
        visor$attach(instance, x, y, z, original, vrPlayer, state.isFallFlying, state.isCrouching);
    }

    @Unique
    private void visor$attach(PoseStack instance, float x, float y, float z, Operation<Void> original,
                              VRClientPlayer vrPlayer, boolean fallFlying, boolean crouching) {
        if (!(getParentModel() instanceof PlayerModel model) || vrPlayer == null) {
            original.call(instance, x, y, z);
            return;
        }

        visor$placement.aim(model.body, false);
        float verticalNudge = 0F;
        if (fallFlying) {
            verticalNudge = FALL_FLYING_DROP;
        } else if (crouching) {
            verticalNudge = -VANILLA_CROUCH_WING_DROP;
        }

        visor$offset.set(0F, verticalNudge, BackLayerPlacement.restingDepth(model.body));
        visor$placement.place(vrPlayer, model.body, visor$offset, visor$offset);
        original.call(instance, visor$offset.x, -visor$offset.y, -visor$offset.z);

        McRenderUtils.rotate(instance, Axis.XP.rotation(visor$placement.pitch()));
        McRenderUtils.rotate(instance, Axis.YP.rotation(visor$placement.yaw()));
    }
}
