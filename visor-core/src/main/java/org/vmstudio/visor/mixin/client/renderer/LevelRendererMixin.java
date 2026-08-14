package org.vmstudio.visor.mixin.client.renderer;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;
import org.vmstudio.visor.core.client.render.helpers.VREffectsHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.render.helpers.CullFrustumHelper;

import org.vmstudio.visor.core.client.ClientContext;

import java.util.*;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@Mixin(value = LevelRenderer.class, priority = 999)
public abstract class LevelRendererMixin implements ResourceManagerReloadListener, AutoCloseable, LevelRendererExtension {

    @Final
    @Shadow
    private Minecraft minecraft;


    @Unique
    private Entity visor$renderedEntity;

    @Unique
    private RenderTarget visor$savedRenderTarget;

    @Final
    @Shadow
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Final
    @Shadow
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    @Unique
    private Map<Long, Long> visor$damagedBlocksVr;

    @Unique
    private Map<Long, BlockDestructionProgress> visor$damagedBlocksVrSave;

    @Unique
    private List<Runnable> visor$swingTasks;


    @Inject(method = "<init>", at = @At("RETURN"))
    private void visor$initFields(Minecraft mc, EntityRenderDispatcher erd,
                                  BlockEntityRenderDispatcher berd,
                                  RenderBuffers rb, CallbackInfo ci) {
        visor$damagedBlocksVr = Collections.synchronizedMap(new HashMap<>());
        visor$damagedBlocksVrSave = Collections.synchronizedMap(new HashMap<>());
        visor$swingTasks = Collections.synchronizedList(new ArrayList<>());
    }

    /* ****************** *\
  //--------RENDERING--------\\
    \* ****************** */

    @ModifyVariable(method = "prepareCullFrustum", at = @At("HEAD"), index = 3, argsOnly = true)
    private Matrix4f visor$widenCullFrustum(Matrix4f projection) {
        return CullFrustumHelper.widenCullProjection(projection);
    }

    @Redirect(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;isDetached()Z")
    )
    private boolean visor$renderSpectatedVRSelfView(Camera camera) {
        if (VRRenderState.isSpectatedVRView(camera.getEntity())) {
            return true;
        }
        return camera.isDetached();
    }

    @Inject(at = @At("HEAD"), method = "renderEntity")
    public void visor$captureEntityRestore(CallbackInfo ci,
                                              @Local(argsOnly = true) Entity entity,
                                              @Share("capturedEntity") LocalRef<Entity> capturedEntity
    ) {
        if (VRRenderState.getPhase().isNotVanilla()
                && entity == minecraft.getCameraEntity()) {
            capturedEntity.set(entity);
            ((GameRendererExtension) minecraft.gameRenderer)
                    .visor$applyCachedCameraEntityPosition(entity);
        }
        this.visor$renderedEntity = entity;
    }

    @Inject(at = @At("TAIL"), method = "renderEntity")
    public void visor$captureEntitySetup(CallbackInfo ci,
                                  @Local(argsOnly = true) Entity entity,
                                  @Share("capturedEntity") LocalRef<Entity> capturedEntity
    ) {
        if (capturedEntity.get() != null) {
            ((GameRendererExtension) minecraft.gameRenderer)
                    .visor$setupCameraEntityAsVRCamera();
        }
        this.visor$renderedEntity = null;
    }



    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getRenderDistance()F", shift = Shift.BEFORE),
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V")
    public void visor$stencil(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer,
                             LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo info
    ) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            //@TODO rework to fix Quest 3 issue
            //VREffectsHelper.drawEyeStencil();
        }
    }


    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 0), method = "renderSnowAndRain")
    public double visor$rainAndSnowX(double x) {
        if (VRRenderState.getRenderPass().isEye()) {
            return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition().x();
        }
        return x;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 1), method = "renderSnowAndRain")
    public double visor$rainAndSnowY(double y) {
        if (VRRenderState.getRenderPass().isEye()) {
            return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                    .getHmd().getPosition().y();
        }
        return y;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;floor(D)I", ordinal = 2), method = "renderSnowAndRain")
    public double visor$rainAndSnowZ(double z) {
        if (VRRenderState.getRenderPass().isEye()) {
            return ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getHmd().getPosition().z();
        }
        return z;
    }


    /**
     * That fixes issue with incorrect resolution
     * for post chain effects in some cases
     * (like for FIRST_PERSON, THIRD_PERSON VR cameras
     * that use different resolution from initial)
     */
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("HEAD"))
    private void visor$ensureVanillaPhase(CallbackInfo ci) {
        if (VisorState.get().isActive() && VRRenderState.getPhase().isNotVanilla()) {
            this.visor$savedRenderTarget = MC.mainRenderTarget;
            MC.mainRenderTarget = VRRenderState.getVanillaTarget();
        }
    }
    @Inject(method = {"initOutline", "initTransparency"}, at = @At("TAIL"))
    private void visor$restoreAfterInit(CallbackInfo ci) {
        if (this.visor$savedRenderTarget != null) {
            MC.mainRenderTarget = this.visor$savedRenderTarget;
            this.visor$savedRenderTarget = null;
        }
    }

    /* **************** *\
  //--------EVENTS--------\\
    \* **************** */
    @Inject(at = @At("TAIL"), method = "onResourceManagerReload")
    public void visor$onResourceManagerReload(ResourceManager resourceManager, CallbackInfo ci) {
        if (VisorState.get().isInitialized()) {
            ClientContext.renderer.prepareReinit(
                    "Resources Reload"
            );
        }
    }
    /* ************************* *\
  //--------BETTER SWINGING--------\\
    \* ************************* */

    @Inject(at = @At("HEAD"), method = "setLevel")
    private void visor$clearSwingDamage(ClientLevel level, CallbackInfo ci) {
        visor$damagedBlocksVrSave.keySet().forEach(
                key -> destructionProgress.remove(key.longValue())
        );
        visor$damagedBlocksVr.clear();
        visor$damagedBlocksVrSave.clear();
    }

    @Inject(at = @At("HEAD"), method = "removeProgress", cancellable = true)
    private void visor$removeProgress(BlockDestructionProgress progress,
                                      CallbackInfo ci
    ) {
        //fix of crash bcz of vr swinging
        ci.cancel();
        long blockPos = progress.getPos().asLong();
        Set<BlockDestructionProgress> set = this.destructionProgress.get(blockPos);
        if (set == null) return; //here it is
        set.remove(progress);
        if (set.isEmpty()) {
            this.destructionProgress.remove(blockPos);
        }

    }

    @Inject(at = @At("HEAD"), method = "renderLevel")
    private void visor$betterSwinging(CallbackInfo ci) {
        if (visor$damagedBlocksVr.isEmpty()
                && visor$damagedBlocksVrSave.isEmpty()) {
            return;
        }

        try {

            List<Long> toRemove = new ArrayList<>();
            destructionProgress.forEach((key, value) -> {
                int stage = value.last().getProgress();
                if (stage < 0 || stage >= ModelBakery.DESTROY_TYPES.size()) {
                    toRemove.add(key);
                }
            });
            toRemove.forEach(it -> {
                destructionProgress.remove(it.longValue());
                visor$damagedBlocksVr.remove(it);
                visor$damagedBlocksVrSave.remove(it);
            });
            toRemove.clear();
            for (Map.Entry<Long, Long> entry : visor$damagedBlocksVr.entrySet()) {
                SortedSet<BlockDestructionProgress> set = destructionProgress.get(entry.getKey());
                if (set == null) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                BlockDestructionProgress d = visor$damagedBlocksVrSave.get(entry.getKey());
                if (d == null) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                if (!set.contains(d) || set.size() > 1) {
                    toRemove.add(entry.getKey());
                    continue;
                }
                //if anything happened with packet from server
                if (entry.getValue() + (VRServerSettings.getSwingingRepairDelay() * 50)
                        < System.currentTimeMillis()) {
                    toRemove.add(entry.getKey());
                }
            }
            toRemove.forEach(it -> {
                destructionProgress.remove(it.longValue());
                visor$damagedBlocksVr.remove(it);
                visor$damagedBlocksVrSave.remove(it);
            });
        }catch(Throwable e){
            LoggerUtils.printError(e);
        }
    }
    @Override
    @Unique
    public void visor$damageBlockProgress(@NotNull Player player,
                                          @NotNull BlockPos blockPos,
                                          int destroyStage
    ) {
        if (!VRServerSettings.isBetterSwinging()
                || VisorState.get().isNotActive()) return;

        if (destroyStage == -1) {
            visor$damagedBlocksVr.remove(blockPos.asLong());
            visor$damagedBlocksVrSave.remove(blockPos.asLong());
            destructionProgress.remove(blockPos.asLong());
            return;
        }

        if (destroyStage == -2) {
            visor$damagedBlocksVr.remove(blockPos.asLong());
            visor$damagedBlocksVrSave.remove(blockPos.asLong());
            return;
        }

        final List<Integer> toRemove = new ArrayList<>();

        destroyingBlocks.forEach((id, progress) -> {
            if (progress.getPos().asLong() == blockPos.asLong()) {
                toRemove.add(id);
                destructionProgress.remove(progress.getPos().asLong());
            }
        });

        toRemove.forEach(it -> destroyingBlocks.remove(it.intValue()));

        BlockDestructionProgress progress = new BlockDestructionProgress(
                player.getId(), blockPos
        );
        progress.setProgress(destroyStage);

        SortedSet<BlockDestructionProgress> set =
                destructionProgress.computeIfAbsent(
                        progress.getPos().asLong(), (p_234254_) -> {
                            return Sets.newTreeSet();
                        }
                );

        set.clear();
        set.add(progress);

        visor$damagedBlocksVr.put(blockPos.asLong(), System.currentTimeMillis());
        visor$damagedBlocksVrSave.put(blockPos.asLong(), progress);
    }

    @Inject(at = @At("HEAD"), method = "levelEvent")
    public void visor$hapticOnSound(int i, BlockPos blockPos, int j, CallbackInfo ci) {
        if(VisorState.get().isNotActive()) return;

        if (this.minecraft.player != null
                && this.minecraft.player.isAlive()
                && this.minecraft.player.blockPosition().distSqr(blockPos) < 25.0D) {
            switch (i) {
                case 1019,      // ZOMBIE_ATTACK_WOODEN_DOOR
                     1020,   // ZOMBIE_ATTACK_IRON_DOOR
                     1021    // ZOMBIE_BREAK_WOODEN_DOOR
                        -> {
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.MAIN, 0.0075f);
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.OFFHAND, 0.0075f);
                }
                case 1030 ->    // ANVIL_USE
                        ClientContext.inputManager
                                .triggerHapticPulse(HandType.MAIN, 0.005f);
                case 1031 -> {  // ANVIL_LAND
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.MAIN, 0.0125f);
                    ClientContext.inputManager
                            .triggerHapticPulse(HandType.OFFHAND, 0.0125f);
                }
            }
        }
    }

    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */


    @Override
    @Unique
    public Entity visor$getRenderedEntity() {
        return this.visor$renderedEntity;
    }


    /* ************************* *\
  //--------UTILITY METHODS--------\\
    \* ************************* */
}
