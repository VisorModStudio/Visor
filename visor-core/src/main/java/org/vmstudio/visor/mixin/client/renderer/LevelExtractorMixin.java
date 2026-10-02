// #!MC-VERSION:: 26.2+
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LevelExtractor.class, priority = 999)
public abstract class LevelExtractorMixin implements LevelRendererExtension {

    // ---- Shadow fields ----
    @Final @Shadow
    private Minecraft minecraft;

    // ---- Unique fields ----
    @Unique
    private Entity visor$currentRenderEntity;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    // 1.21.2 moved the detached-camera check into collectVisibleEntities, 1.21.9 renamed it
    @WrapOperation(
            method = "extractVisibleEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z")
    )
    private boolean visor$renderSpectatedVRSelfView(Camera camera, Operation<Boolean> original) {
        if (VRRenderState.isSpectatedVRView(McVersionClientUtils.cameraEntity(camera))) {
            return true;
        }
        return original.call(camera);
    }

    @Inject(at = @At("HEAD"), method = "extractEntity")
    public void visor$captureEntityRestore(CallbackInfoReturnable<EntityRenderState> cir,
                                           @Local(argsOnly = true) Entity entity,
                                           @Share("vrCameraEntity") LocalRef<Entity> vrCameraEntity
    ) {
        if (VRRenderState.getPhase().isNotVanilla()
                && entity == minecraft.getCameraEntity()) {
            vrCameraEntity.set(entity);
            VRCameraEntitySwap.applyCachedCameraEntityPosition(entity);
        }
        this.visor$currentRenderEntity = entity;
    }

    @Inject(at = @At("TAIL"), method = "extractEntity")
    public void visor$captureEntitySetup(CallbackInfoReturnable<EntityRenderState> cir,
                                         @Local(argsOnly = true) Entity entity,
                                         @Share("vrCameraEntity") LocalRef<Entity> vrCameraEntity
    ) {
        if (vrCameraEntity.get() != null) {
            VRCameraEntitySwap.setupCameraEntityAsVRCamera();
        }
        this.visor$currentRenderEntity = null;
    }

    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void visor$onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (VisorState.get().isInitialized()) {
            ClientContext.renderer.prepareReinit(
                    "resource manager reloaded"
            );
        }
    }


    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */

    @Override
    @Unique
    public Entity visor$getCurrentRenderEntity() {
        return this.visor$currentRenderEntity;
    }

    @Override
    @Unique
    public void visor$damageBlockProgress(@NotNull Player player, @NotNull BlockPos blockPos, int destroyStage) {
        if (this.minecraft.level != null) {
            ((LevelRendererExtension) this.minecraft.level).visor$damageBlockProgress(player, blockPos, destroyStage);
        }
    }
}
