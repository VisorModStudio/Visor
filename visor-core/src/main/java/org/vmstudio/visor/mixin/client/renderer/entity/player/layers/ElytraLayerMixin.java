package org.vmstudio.visor.mixin.client.renderer.entity.player.layers;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.player.BackLayerPlacement;

@Mixin(ElytraLayer.class)
public abstract class ElytraLayerMixin<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    @Unique
    private final BackLayerPlacement visor$placement = new BackLayerPlacement();

    @Unique
    private final Vector3f visor$offset = new Vector3f();

    public ElytraLayerMixin(RenderLayerParent<T, M> renderer) {
        super(renderer);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$elytraPosition(PoseStack instance, float x, float y, float z, Operation<Void> original, @Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true, ordinal = 2) float partialTick) {
        var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
        if (!(getParentModel() instanceof PlayerModel<?> model) || vrPlayer == null) {
            original.call(instance, x, y, z);
            return;
        }

        visor$placement.aim(model.body, false);
        float verticalNudge = 0F;
        if (entity.isFallFlying()) {
            verticalNudge = 2F;
        } else if (entity.isCrouching()) {
            verticalNudge = -3F;
        }

        visor$offset.set(0F, verticalNudge, BackLayerPlacement.restingDepth(model.body));
        visor$placement.place(entity, vrPlayer, model.body, visor$offset, visor$offset);
        original.call(instance, visor$offset.x, -visor$offset.y, -visor$offset.z);

        instance.mulPose(Axis.XP.rotation(visor$placement.pitch()));
        instance.mulPose(Axis.YP.rotation(visor$placement.yaw()));
    }
}
