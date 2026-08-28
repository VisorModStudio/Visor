package org.vmstudio.visor.mixin.client.renderer;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskTeleport;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.VRCameraEntityCache;
import org.vmstudio.visor.core.client.render.VRGameCamera;
import org.vmstudio.visor.core.client.render.helpers.RenderHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderEffectsHelper;
import org.vmstudio.visor.core.client.render.VRRenderState;

import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.client.settings.enums.MirrorMode;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

import org.vmstudio.visor.core.client.ClientContext;
import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
        implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension {
    @Shadow
    @Final
    Minecraft minecraft;

    @Shadow
    private boolean renderHand;

    @Shadow private boolean effectActive;

    @Shadow
    private float renderDistance;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float fov;

    @Shadow
    private float oldFov;


    @Shadow
    private int itemActivationTicks;

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double fov);

    @Shadow
    protected abstract double getFov(Camera mainCamera2, float partialTicks, boolean b);

    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f projectionMatrix);

    @Shadow
    public abstract void pick(float f);

    @Shadow
    private long lastActiveTime;

    @Shadow
    @Final
    private Camera mainCamera;



    @Unique
    public Matrix4f visor$thirdPersonProjection = new Matrix4f();
    @Unique
    public float visor$nearClipPlane = 0.02F;
    @Unique
    private float visor$farClipPlane = 128.0F;
    @Unique
    public Vec3 visor$crossVec;
    @Unique
    private HandType visor$pickingHand;
    @Unique
    private final HitResult[] visor$handHitResult = new HitResult[2];
    @Unique
    private final Vec3[] visor$handCrossVec = new Vec3[2];
    @Unique
    private final Entity[] visor$handPickEntity = new Entity[2];
    @Unique
    public boolean visor$onfire;
    @Unique
    public boolean visor$inBlock = false;
    @Unique
    public float visor$blockProximity = 0.0f;

    @Unique
    public VRCameraEntityCache visor$cameraEntityCache = new VRCameraEntityCache();
    @Unique
    private boolean visor$cameraEntityCached;
    @Unique
    private int visor$cameraEntityCacheDepth;



    @Shadow public abstract void render(float f, long l, boolean bl);

    @Shadow
    public abstract void renderItemActivationAnimation(int i, int j, float par1);

    /* ******************* *\
  //--------RENDERING--------\\
    \* ******************* */

    /**
     * Cancels GUI rendering for VRWorld stage and render VR main menu room
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", ordinal = 6), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", cancellable = true)
    public void visor$onRenderGUI(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {

        if (VRRenderState.getPhase().isNotVRWorld()) {
            // Proceed rendering GUI for Vanilla and VRGui stage
            return;
        }

        info.cancel();


        // Render Main Menu View
        if (VRRenderState.getSceneType().isMainMenu()) {

            GL11.glDisable(GL11.GL_STENCIL_TEST);

            PoseStack poseStack = new PoseStack();
            //render VR main menu
            ClientContext.decorationRenderer.renderMainMenu(
                    poseStack,
                    partialTicks
            );
        }
    }

    @Unique
    private boolean visor$isVRGuiVisible;

    @Override
    public boolean visor$isVRGuiVisible(){
        return visor$isVRGuiVisible;
    }

    @Override
    public void visor$setVRGuiVisible(boolean flag){
        visor$isVRGuiVisible = flag;
    }

    /**
     * Draw GUI only after first level render
     */
    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.AFTER, ordinal = 6), method = "render(FJZ)V", ordinal = 0, argsOnly = true)
    private boolean visor$vrGuiVisibility(boolean doRender) {
        if (VRRenderState.getPhase().isVanilla()) {
            return doRender;
        }
        return visor$isVRGuiVisible();
    }

    /**
     * If no crosshair rendered,
     * don't render block outline as well
     * @param cir
     */
    @Inject(at = @At("HEAD"), method = "shouldRenderBlockOutline", cancellable = true)
    public void visor$shouldDrawBlockOutline(CallbackInfoReturnable<Boolean> cir) {
        if (VRRenderState.getPhase().isVRWorld()) {
            cir.setReturnValue(
                    ClientContext.visor.isFeatureEnabled(ClientFeature.AIM_EFFECTS)
            );
        }
    }



    /* **************** *\
  //--------CAMERA--------\\
    \* **************** */
    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Camera"))
    public Camera visor$replaceCamera() {
        return new VRGameCamera();
    }

    @Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
    public void visor$fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info) {
        if (VisorState.get().isActive() && VRRenderState.getSceneType().isMainMenu()) {
            info.setReturnValue(Double.valueOf(this.minecraft.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true)
    public void visor$projection(double d, CallbackInfoReturnable<Matrix4f> info) {
        if (VisorState.get().isNotActive()) {
            return;
        }
        PoseStack posestack = new PoseStack();
        visor$setupClipPlanes();
        ClientContext.renderer.updateProjection();

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if(renderPass == VRRenderPass.EYE_LEFT){
            posestack.mulPoseMatrix(
                    ClientContext.renderer.getEyeProjection(EyeType.LEFT)
            );
            info.setReturnValue(
                    posestack.last().pose()
            );
            return;
        }
        if (renderPass == VRRenderPass.EYE_RIGHT) {
            posestack.mulPoseMatrix(
                    ClientContext.renderer.getEyeProjection(EyeType.RIGHT)
            );
            info.setReturnValue(posestack.last().pose());
            return;
        }
        if (renderPass == VRRenderPass.THIRD_PERSON) {
            if (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY) {
                posestack.mulPoseMatrix(
                        new Matrix4f().setPerspective(
                                VRClientSettings.getMixedRealityFov() * Mth.DEG_TO_RAD,
                                VRClientSettings.getMixedRealityAspectRatio(), this.visor$nearClipPlane,
                                this.visor$farClipPlane
                        )
                );
            }else {
                posestack.mulPoseMatrix(
                        new Matrix4f().setPerspective(
                                VRClientSettings.getThirdPersonFov() * Mth.DEG_TO_RAD,
                                (float) this.minecraft.getWindow().getScreenWidth()
                                        / (float) this.minecraft.getWindow().getScreenHeight(),
                                this.visor$nearClipPlane, this.visor$farClipPlane
                        )
                );
            }
            this.visor$thirdPersonProjection = new Matrix4f(posestack.last().pose());
            info.setReturnValue(posestack.last().pose());
            return;
        }

        if (this.zoom != 1.0F) {
            posestack.translate(this.zoomX, -this.zoomY, 0.0D);
            posestack.scale(this.zoom, this.zoom, 1.0F);
        }
        posestack.mulPoseMatrix(
                new Matrix4f()
                        .setPerspective(
                                (float) d * Mth.DEG_TO_RAD,
                                (float) this.minecraft.getWindow().getScreenWidth()
                                        / (float) this.minecraft.getWindow().getScreenHeight(),
                                this.visor$nearClipPlane,
                                this.visor$farClipPlane
                        )
        );

        info.setReturnValue(posestack.last().pose());
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", remap = false, shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void visor$matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        if(VisorState.get().isNotActive()) return;
        this.resetProjectionMatrix(
                this.getProjectionMatrix(
                        minecraft.options.fov().get()
                )
        );
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
    }


    @WrapMethod(method = "pick")
    private void visor$pickWithVRHands(float partialTick, Operation<Void> original) {
        if(VisorState.get().isNotActive()){
            original.call(partialTick);
            return;
        }
        if (this.minecraft.screen != null && this.minecraft.hitResult != null) {
            return;
        }
        if (this.minecraft.getCameraEntity() == null) {
            this.minecraft.hitResult = visor$fallbackMiss();
            return;
        }

        HandType activeHand = ClientContext.localPlayer.getActiveHand();
        visor$pickWithHand(activeHand, partialTick, original);

        HandType otherHand = activeHand.opposite();
        if (VRServerSettings.isTwoHandedVR()
                && ClientContext.rawPoseHandler.getControllerData(otherHand).isTracking()) {
            HitResult activeHit = this.minecraft.hitResult;
            Entity activePickEntity = this.minecraft.crosshairPickEntity;

            visor$pickWithHand(otherHand, partialTick, original);

            this.minecraft.hitResult = activeHit;
            this.minecraft.crosshairPickEntity = activePickEntity;
            this.visor$crossVec = visor$handCrossVec[activeHand.ordinal()];
        } else {
            visor$handHitResult[otherHand.ordinal()] = null;
            visor$handCrossVec[otherHand.ordinal()] = null;
            visor$handPickEntity[otherHand.ordinal()] = null;
        }
    }

    @Unique
    private BlockHitResult visor$fallbackMiss() {
        var player = this.minecraft.player;
        if (player == null) {
            return BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        }
        return BlockHitResult.miss(player.position(), player.getDirection(), player.blockPosition());
    }

    @Unique
    private void visor$pickWithHand(HandType hand, float partialTick, Operation<Void> original) {
        visor$pickingHand = hand;

        Entity cameraEntity = this.minecraft.getCameraEntity();
        AABB originalBB = cameraEntity.getBoundingBox();
        this.visor$cacheCameraEntity(cameraEntity);
        this.visor$setupCameraEntity(
                ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
                        .getHand(hand)
        );
        double shiftX = cameraEntity.getX() - visor$cameraEntityCache.getX();
        double shiftY = cameraEntity.getY() - visor$cameraEntityCache.getY();
        double shiftZ = cameraEntity.getZ() - visor$cameraEntityCache.getZ();
        cameraEntity.setBoundingBox(originalBB.move(shiftX, shiftY, shiftZ));

        original.call(partialTick);

        // restore entity
        this.visor$restoreCameraEntity(cameraEntity);
        cameraEntity.setBoundingBox(originalBB);

        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            // includes entity hits missed by visor$pickPos
            this.visor$crossVec = hitResult.getLocation();
        }
        visor$handHitResult[hand.ordinal()] = hitResult;
        visor$handCrossVec[hand.ordinal()] = this.visor$crossVec;
        visor$handPickEntity[hand.ordinal()] = this.minecraft.crosshairPickEntity;
        visor$pickingHand = null;
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getXRot()F"),
            method = "renderLevel")
    public float visor$noVanillaCameraPitch(Camera camera) {
        if (VRRenderState.getPhase().isVanilla()) {
            return camera.getXRot();
        }
        return 0F;
    }

    @Redirect(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/Camera;getYRot()F"),
            method = "renderLevel")
    public float visor$noVanillaCameraYaw(Camera camera) {
        if (VRRenderState.getPhase().isVanilla()) {
            return camera.getYRot();
        }
        // -180 cancels the +180 vanilla
        return -180F;
    }

    @Inject(at = @At(value = "NEW", target = "org/joml/Matrix3f", remap = false),
            method = "renderLevel")
    public void visor$orientCameraToPass(float partialTicks, long nanos, PoseStack poseStack, CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            RenderPoseHelper.applyCameraOrientation(
                    VRRenderState.getRenderPass(), poseStack
            );
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel")
    public void visor$pickAndSetupCamera(GameRenderer g, float pPartialTicks) {
        if (VRRenderState.getPhase().isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }
        if (VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            this.pick(pPartialTicks);

            if(MC.screen == null){
                TaskTeleport.updateTeleportDestination(MC.player);
            }
        }

        this.visor$cacheCameraEntity(this.minecraft.getCameraEntity());
        this.visor$setupCameraEntityAsVRCamera();
        this.visor$updateCameraOverlaps(pPartialTicks);
    }

    @Inject(at = @At(value = "TAIL"), method = "renderLevel")
    public void visor$restoreCamera(float f, long j, PoseStack p, CallbackInfo i) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            this.visor$restoreCameraEntity(
                    this.minecraft.getCameraEntity()
            );
        }
    }


    /* ********************* *\
  //--------RAY TRACING--------\\
    \* ********************* */
    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 0)
    public Vec3 visor$pickPos(Vec3 original) {
        if (VisorState.get().isNotActive()) {
            return original;
        }
        LocalPlayerPose renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);

        HandType hand = visor$pickingHand != null
                ? visor$pickingHand
                : ClientContext.localPlayer.getActiveHand();

        HitResult hitResult = visor$pickBlock(
                renderPose.getHand(hand),
                this.minecraft.gameMode.getPickRange(),
                false
        );
        this.minecraft.hitResult = hitResult;
        Vec3 fallbackCrossVec = visor$pointAlongAim(
                renderPose.getHand(hand),
                this.minecraft.gameMode.getPickRange()
        );
        this.visor$crossVec = hitResult != null && hitResult.getType() != HitResult.Type.MISS
                ? hitResult.getLocation()
                : fallbackCrossVec;

        return new Vec3((Vector3f) renderPose.getHand(hand).getPosition());
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 visor$pickDirection(Vec3 original) {
        if (VisorState.get().isNotActive()) {
            return original;
        }
        HandType hand = visor$pickingHand != null
                ? visor$pickingHand
                : ClientContext.localPlayer.getActiveHand();

        return new Vec3(
                (Vector3f) ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                        .getHand(hand).getDirection()
        );
    }



    /* ******************************* *\
      //--------DISABLE VANILLA STUFF--------\\
        \* ******************************* */
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean visor$noPostEffectOnThirdPerson(GameRenderer instance) {
        return this.effectActive && VRRenderState.getRenderPass() != VRRenderPass.THIRD_PERSON;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean visor$noPauseGameIfWindowNotFocused(Minecraft instance) {
        return VisorState.get().isActive() || instance.isWindowActive();
    }


    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void visor$freezeFovInVR(CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            // vanilla tickFov starts from when the view is not modified
            final float neutralFovModifier = 1.0F;
            this.fov = neutralFovModifier;
            this.oldFov = neutralFovModifier;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void visor$skipAutoScreenshotInMenu(Path path, CallbackInfo ci) {
        if (VisorState.get().isActive() && VRRenderState.getSceneType().isMainMenu()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    public void visor$noBobHurt(PoseStack poseStack,
                                float f,
                                CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void visor$noBobView(PoseStack matrixStack,
                                float f,
                                CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void visor$noConfusionOverlayInGUI(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (VRRenderState.getPhase().isVRGui()) {
            ci.cancel();
        }
    }



    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean visor$noVanillaHands(GameRenderer instance) {
        if (VRRenderState.isSpectatedVRView(minecraft.getCameraEntity())) {
            return false;
        }
        return VRRenderState.getPhase().isVanilla() && renderHand;
    }

    @Inject(at = @At("TAIL"), method = "renderLevel")
    public void visor$releaseHiddenAreaMask(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            RenderEffectsHelper.releaseHiddenAreaMask();
        }
    }


    /* ************** *\
  //--------MISC--------\\
    \* ************** */

    //ITEM ACTIVATION ANIMATION
    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void visor$noScaleItem(PoseStack poseStack, float x, float y, float z, int width, int height,
                                   float partialTicks
    ) {
        if (VRRenderState.getPhase().isVanilla()) {
            poseStack.scale(x, y, z);
            return;
        }
        VRRenderPass currentCamera = VRRenderState.getRenderPass();
        var cameraPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getCameraPose(currentCamera);

        float time = (40 - this.itemActivationTicks + partialTicks) / 40.0f;
        float t2 = time * time;
        float t3 = time * t2;
        float curve = 10.25f * t3 * t2 - 24.95f * t2 * t2 + 25.5f * t3 - 13.8f * t2 + 4.0f * time;
        float popScale = 0.5F * Mth.sin(curve * Mth.PI);

        poseStack.translate(0, 0, popScale - 1.0F);
        if (currentCamera == VRRenderPass.THIRD_PERSON) {
            float fov = VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY
                    ? VRClientSettings.getMixedRealityFov()
                    : VRClientSettings.getThirdPersonFov();
            popScale *= fov / 70.0F;
        }
        RenderPoseHelper.applyCameraPose(currentCamera, poseStack);
        poseStack.scale(popScale, popScale, popScale);
        poseStack.mulPose(Axis.YP.rotation(-cameraPose.getYaw()));
        poseStack.mulPose(Axis.XP.rotation(-cameraPose.getPitch()));
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void visor$noItemActivationAnimInGUI(GameRenderer instance, int i, int j, float f) {
        if(VRRenderState.getPhase().isVanilla()) {
            renderItemActivationAnimation(i, j, f);
        }
    }
    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$noItemTranslate(PoseStack poseStack, float x, float y, float z) {
        if(VRRenderState.getPhase().isVanilla()) {
            poseStack.translate(x, y, z);
        }
    }
    //--

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void visor$pauseOncePerFrame(Minecraft instance, boolean bl) {
        if (VisorState.get().isNotActive() || VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            instance.pauseGame(bl);
        }
    }

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long visor$useActiveTimeOncePerFrame() {
        if (VisorState.get().isNotActive() || VRRenderState.getRenderPass() == VRRenderPass.worldUpdater()) {
            return Util.getMillis();
        } else {
            return this.lastActiveTime;
        }
    }





    /* ************************ *\
  //--------PUBLIC METHODS--------\\
    \* ************************ */
    @Override
    @Unique
    public void visor$setupCameraEntity(VRPose vrPose) {
        if (!this.visor$cameraEntityCached) {
            return;
        }
        var position = vrPose.getPosition();
        float x = position.x();
        float y = position.y();
        float z = position.z();

        LivingEntity cameraEntity = (LivingEntity) this.minecraft.getCameraEntity();
        cameraEntity.setPosRaw(x, y, z);
        cameraEntity.xo = cameraEntity.xOld = x;
        cameraEntity.yo = cameraEntity.yOld = y;
        cameraEntity.zo = cameraEntity.zOld = z;

        cameraEntity.setXRot(-vrPose.getPitchDegrees());
        cameraEntity.setYRot(vrPose.getYawDegrees());
        cameraEntity.xRotO = cameraEntity.getXRot();
        cameraEntity.yHeadRot = cameraEntity.getYRot();
        cameraEntity.yHeadRotO = cameraEntity.getYRot();

        // collapse the eye offset so the entity position is the pose position
        cameraEntity.eyeHeight = 0.0001F;
    }

    @Override
    @Unique
    public void visor$cacheCameraEntity(Entity cameraEntity) {
        if (this.minecraft.getCameraEntity() != null) {
            this.visor$cameraEntityCacheDepth++;
            if (!this.visor$cameraEntityCached) {
                LivingEntity livingEntity = cameraEntity instanceof LivingEntity ent ? ent : null;
                visor$cameraEntityCache = new VRCameraEntityCache(
                        cameraEntity.getX(), cameraEntity.getY(),
                        cameraEntity.getZ(),

                        cameraEntity.xOld, cameraEntity.yOld,
                        cameraEntity.zOld,

                        cameraEntity.xo, cameraEntity.yo,
                        cameraEntity.zo,

                        livingEntity != null ? livingEntity.yHeadRot : cameraEntity.getYRot(),
                        cameraEntity.getXRot(),

                        livingEntity != null ? livingEntity.yHeadRotO : cameraEntity.yRotO,
                        cameraEntity.xRotO,

                        cameraEntity.getEyeHeight()
                );
                this.visor$cameraEntityCached = true;
            }
        }
    }

    @Override
    @Unique
    public void visor$restoreCameraEntity(Entity cameraEntity) {
        if (this.visor$cameraEntityCacheDepth > 0) {
            this.visor$cameraEntityCacheDepth--;
        }
        if (cameraEntity != null
                && this.visor$cameraEntityCached
                && this.visor$cameraEntityCacheDepth == 0) {
            visor$cameraEntityCache.apply(cameraEntity);
            this.visor$cameraEntityCached = false;
        }
    }

    @Override
    @Unique
    public void visor$applyCachedCameraEntityPosition(Entity cameraEntity) {
        if (cameraEntity != null && this.visor$cameraEntityCached) {
            this.visor$cameraEntityCache.apply(cameraEntity);
        }
    }



    @Override
    @Unique
    public void visor$setupClipPlanes() {
        this.renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);
        this.visor$farClipPlane = this.renderDistance + 1024.0F;
    }

    @Override
    @Unique
    public float visor$getNearClipPlane() {
        return this.visor$nearClipPlane;
    }

    @Override
    @Unique
    public float visor$getFarClipPlane() {
        return this.visor$farClipPlane;
    }






    @Override
    @Unique
    public boolean visor$isOnFire() {
        return visor$onfire;
    }


    @Override
    @Unique
    public boolean visor$isInBlock() {
        return visor$inBlock;
    }

    @Override
    @Unique
    public float visor$getBlockProximity() {
        return visor$blockProximity;
    }


    @Override
    @Unique
    public void visor$resetProjectionMatrix(float partialTicks) {
        this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
    }


    @Override
    @Unique
    public Vec3 visor$getCrossVec() {
        return visor$crossVec;
    }

    @Override
    @Unique
    public Vec3 visor$getCrossVec(HandType hand) {
        return visor$handCrossVec[hand.ordinal()];
    }

    @Override
    @Unique
    public HitResult visor$getHandHitResult(HandType hand) {
        return visor$handHitResult[hand.ordinal()];
    }

    @Override
    @Unique
    public void visor$applyHandPick(HandType hand) {
        HitResult hitResult = visor$handHitResult[hand.ordinal()];
        Vec3 crossVec = visor$handCrossVec[hand.ordinal()];
        if (hitResult == null || crossVec == null) {
            this.pick(1.0f);
            return;
        }
        this.minecraft.hitResult = hitResult;
        this.minecraft.crosshairPickEntity = visor$handPickEntity[hand.ordinal()];
        this.visor$crossVec = crossVec;
    }

    @Override
    public VRCameraEntityCache visor$getCameraEntityCache() {
        return visor$cameraEntityCache;
    }

    @Override
    @Unique
    public Matrix4f visor$getThirdPersonProjection() {
        return visor$thirdPersonProjection;
    }


    /* ************************* *\
      //--------UTILITY METHODS--------\\
        \* ************************* */
    @Unique
    private void visor$updateCameraOverlaps(float partialTicks) {
        //@TODO add post process for these effects
        this.visor$inBlock = false;
        this.visor$blockProximity = 0.0f;

        this.visor$onfire = false;

        if(minecraft.player.isSpectator()
                || !minecraft.player.isAlive()
                || VRRenderState.getSceneType().isMainMenu()){
            return;
        }
        // fix for immersive portals issue
        if (this.minecraft.level != this.minecraft.player.level()) {
            return;
        }
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass == null) {
            return;
        }
        var cameraPos = RenderPoseHelper.getCameraPosition(
                renderPass,
                ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
        );

        float inBlockEffectStart = 0.3f;
        float distance = RenderHelper.distanceToNearestSolidBlockSurface(
                new Vec3((Vector3f) cameraPos),
                inBlockEffectStart
        );

        this.visor$blockProximity = Math.max(
                0.0f,
                1.0f - distance / inBlockEffectStart
        );
        this.visor$inBlock = distance < visor$nearClipPlane * 2.0f;


        this.visor$onfire = VRRenderState.getRenderPass() != VRRenderPass.THIRD_PERSON
                && this.minecraft.player.isOnFire()
                && !ModLoader.get().renderFireOverlay(
                this.minecraft.player, new PoseStack()
        );
    }

    @Unique
    public Vec3 visor$pointAlongAim(VRPose vrPose,
                                           double distance) {
        var dir = vrPose.getDirection();
        return new Vec3(vrPose
                .getPosition().add(
                        dir.x() * (float) distance,
                        dir.y() * (float) distance,
                        dir.z() * (float) distance,
                        new Vector3f()
                )
        );
    }

    @Unique
    public HitResult visor$pickBlock(VRPose vrPose,
                                     double blockReachDistance,
                                     boolean fluid
    ) {
        return ImmPortalsCompatHelper.pickBlock(MC.level, vrPose, blockReachDistance, fluid, MC.player);
    }

}
