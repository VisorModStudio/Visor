// #!MC-VERSION:: 1.20.1-1.21.1
package org.vmstudio.visor.mixin.client.renderer.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.entity.EntityRenderDispatcherExtension;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Shadow
    @Final
    protected EntityRenderDispatcher entityRenderDispatcher;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderDispatcher;cameraOrientation()Lorg/joml/Quaternionf;"), method = "renderNameTag")
    public Quaternionf visor$vrNameTagCameraOrient(EntityRenderDispatcher instance,
                                                   Operation<Quaternionf> original, Entity entity) {
        return visor$nameTagOrientation(instance, original, entity);
    }

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void visor$hideSpectatedVRNameTag(CallbackInfo ci, @Local(argsOnly = true) Entity entity) {
        if (VRRenderState.isSpectatedVRView(entity)) {
            ci.cancel();
        }
    }

    @Unique
    private Quaternionf visor$nameTagOrientation(EntityRenderDispatcher instance,
                                                 Operation<Quaternionf> original, @Nullable Entity entity) {
        if(VRRenderState.getPhase().isNotVRWorld()){
            return original.call(instance);
        }
        float heightScale = 1.0f;
        VRClientPlayer vrPlayer = entity == null ? null : VRClientPlayers.getPlayer(entity);
        if (vrPlayer != null) {
            heightScale = vrPlayer.getModelScale();
        }
        return ((EntityRenderDispatcherExtension) this.entityRenderDispatcher)
                .visor$lookAtCameraOrientation(heightScale, 0.5f * heightScale);
    }
}
