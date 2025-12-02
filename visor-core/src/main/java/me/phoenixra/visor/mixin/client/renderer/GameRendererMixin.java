package me.phoenixra.visor.mixin.client.renderer;


import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.common.player.PoseElement;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.render.VRCameraType;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.player.pose.LocalPlayerPose;
import me.phoenixra.visor.modified.client.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRCameraEntityCache;
import me.phoenixra.visor.core.client.render.VRGameCamera;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.render.helpers.VREffectsHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;

import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.options.enums.MirrorMode;
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
import net.minecraft.world.level.ClipContext;
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
import java.util.Optional;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin
        implements ResourceManagerReloadListener, AutoCloseable, GameRendererModified {
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
    public boolean visor$onfire;
    @Unique
    public float visor$inBlock = 0.0F;

    @Unique
    public VRCameraEntityCache visor$cameraEntityCache = new VRCameraEntityCache();
    @Unique
    private boolean visor$cameraEntityCached;



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
        if (VRRenderState.isInMainMenu()) {

            GL11.glDisable(GL11.GL_STENCIL_TEST);

            PoseStack poseStack = new PoseStack();
            //render VR main menu
            ClientContext.decorationRenderer.render(
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
    private boolean visor$renderGui(boolean doRender) {
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
        if (VRRenderState.isInMainMenu()) {
            info.setReturnValue(Double.valueOf(this.minecraft.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true)
    public void visor$projection(double d, CallbackInfoReturnable<Matrix4f> info) {
        if (VisorState.getState().isNotActive()) {
            return;
        }
        PoseStack posestack = new PoseStack();
        visor$setupClipPlanes();
        ClientContext.renderer.updateProjection();

        VRCameraType cameraType = VRRenderState.getCameraType();
        if(cameraType == VRCameraType.EYE_LEFT){
            posestack.mulPoseMatrix(
                    ClientContext.renderer.getEyeProjection(EyeType.LEFT)
            );
            info.setReturnValue(
                    posestack.last().pose()
            );
            return;
        }
        if (cameraType == VRCameraType.EYE_RIGHT) {
            posestack.mulPoseMatrix(
                    ClientContext.renderer.getEyeProjection(EyeType.RIGHT)
            );
            info.setReturnValue(posestack.last().pose());
            return;
        }
        if (cameraType == VRCameraType.THIRD_PERSON) {
            if (VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY) {
                posestack.mulPoseMatrix(
                        new Matrix4f().setPerspective(
                                VRClientSettings.getMixedRealityFov() * 0.01745329238474369F,
                                VRClientSettings.getMixedRealityAspectRatio(), this.visor$nearClipPlane,
                                this.visor$farClipPlane
                        )
                );
            }else {
                posestack.mulPoseMatrix(
                        new Matrix4f().setPerspective(
                                VRClientSettings.getThirdPersonFov() * 0.01745329238474369F,
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
        if(VisorState.getState().isNotActive()) return;
        this.resetProjectionMatrix(
                this.getProjectionMatrix(
                        minecraft.options.fov().get()
                )
        );
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
    }


    @WrapMethod(method = "pick")
    private void visor$vrPick(float partialTick, Operation<Void> original) {
        if(VisorState.getState().isNotActive()){
            original.call(partialTick);
            return;
        }
        // don't update the hitresult when chat is open
        if (this.minecraft.screen != null && this.minecraft.hitResult != null) {
            return;
        }
        // skip when data not available yet
        else if (this.minecraft.getCameraEntity() == null)
        {
            // some mods don't like it when the hitresult is null, so set it to a miss
            if (this.minecraft.player != null) {
                this.minecraft.hitResult = BlockHitResult.miss(this.minecraft.player.position(),
                        this.minecraft.player.getDirection(), this.minecraft.player.blockPosition());
            } else {
                this.minecraft.hitResult = BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
            }
            return;
        }

        AABB originalBB = this.minecraft.getCameraEntity().getBoundingBox();
        // set the entity position and view to the controller
        this.visor$cacheCameraEntity(this.minecraft.getCameraEntity());
        this.visor$setupCameraEntity(
                ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
                        .getHand(ClientContext.localPlayer.getActiveHand())
        );
        // move the bounding box as well, this is used for entity hits
        this.minecraft.getCameraEntity().setBoundingBox(originalBB.move(
                this.minecraft.getCameraEntity().getX() - visor$cameraEntityCache.getX(),
                this.minecraft.getCameraEntity().getY() - visor$cameraEntityCache.getY(),
                this.minecraft.getCameraEntity().getZ() - visor$cameraEntityCache.getZ()));

        // call the vanilla method
        original.call(partialTick);

        // restore entity
        this.visor$restoreCameraEntity(this.minecraft.getCameraEntity());
        this.minecraft.getCameraEntity().setBoundingBox(originalBB);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel")
    public void visor$pickAndSetupCamera(GameRenderer g, float pPartialTicks) {
        if (VRRenderState.getPhase().isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }
        if (VRRenderState.getCameraType() == VRCameraType.worldUpdater()) {
            this.pick(pPartialTicks);

            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != HitResult.Type.MISS) {
                this.visor$crossVec = this.minecraft.hitResult.getLocation();
            }
        }

        this.visor$cacheCameraEntity(this.minecraft.getCameraEntity());
        this.visor$setupCameraEntityAsVRCamera();
        this.visor$setupOverlayStatus(pPartialTicks);
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
        if (VisorState.getState().isNotActive()) {
            return original;
        }
        LocalPlayerPose renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);

        HandType activeHand = ClientContext.localPlayer.getActiveHand();

        this.minecraft.hitResult = visor$pickBlock(
                renderPose.getHand(activeHand),
                this.minecraft.gameMode.getPickRange(),
                false
        );
        this.visor$crossVec = visor$aimedPointAtDistance(
                renderPose.getHand(activeHand),
                this.minecraft.gameMode.getPickRange()
        );

        return new Vec3((Vector3f) renderPose.getHand(activeHand).getPosition());
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 visor$pickDirection(Vec3 original) {
        if (VisorState.getState().isNotActive()) {
            return original;
        }
        HandType activeHand = ClientContext.localPlayer.getActiveHand();

        return new Vec3(
                (Vector3f) ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                        .getHand(activeHand).getDirection()
        );
    }



    /* ******************************* *\
      //--------DISABLE VANILLA STUFF--------\\
        \* ******************************* */
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean visor$noPostEffectOnThirdPerson(GameRenderer instance) {
        return this.effectActive && VRRenderState.getCameraType() != VRCameraType.THIRD_PERSON;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean visor$noPauseGameIfWindowNotFocused(Minecraft instance) {
        return VisorState.getState().isActive() || instance.isWindowActive();
    }


    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void visor$noFOVchangeInVR(CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            this.oldFov = this.fov = 1.0f;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void visor$noScreenshotInMenu(Path path, CallbackInfo ci) {
        if (VisorState.getState().isActive() && VRRenderState.isInMainMenu()) {
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
        return VRRenderState.getPhase().isVanilla() && renderHand;
    }

    @Inject(at = @At("TAIL"), method = "renderLevel")
    public void visor$disableStencil(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if(VRRenderState.getPhase().isNotVanilla()) {
            VREffectsHelper.disableStencilTest();
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
        VRCameraType currentCamera = VRRenderState.getCameraType();
        // need to do stuff twice, because redirects have no access to locals
        int i = 40 - this.itemActivationTicks;
        float g = ((float) i + partialTicks) / 40.0f;
        float h = g * g;
        float l = g * h;
        float m = 10.25f * l * h - 24.95f * h * h + 25.5f * l - 13.8f * h + 4.0f * g;
        float n = m * (float) Math.PI;
        float sinN = Mth.sin(n) * 0.5F;
        poseStack.translate(0, 0, sinN - 1.0);
        if (currentCamera == VRCameraType.THIRD_PERSON) {
            float fov;
            if(VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY){
                fov = VRClientSettings.getMixedRealityFov();
            }else{
                fov = VRClientSettings.getThirdPersonFov();
            }
            sinN *= (float) (fov / 70.0);
        }
        RenderPoseHelper.applyCameraPose(currentCamera, poseStack);
        poseStack.scale(sinN, sinN, sinN);
        poseStack.mulPose(Axis.YP.rotationDegrees(-ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getCameraPose(currentCamera).getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER).getCameraPose(currentCamera).getPitch()));
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
        if (VisorState.getState().isNotActive() || VRRenderState.getCameraType() == VRCameraType.worldUpdater()) {
            instance.pauseGame(bl);
        }
    }

    /**
     * Only process this when rendering vanilla
     * or VR camera that is a worldUpdater
     */
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long visor$useActiveTimeOncePerFrame() {
        if (VisorState.getState().isNotActive() || VRRenderState.getCameraType() == VRCameraType.worldUpdater()) {
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
    public void visor$setupCameraEntity(PoseElement poseElement) {
        if (this.visor$cameraEntityCached) {

            var position = poseElement.getPosition();
            LivingEntity cameraEntity = (LivingEntity) this.minecraft.getCameraEntity();
            cameraEntity.setPosRaw(position.x(), position.y(), position.z());
            cameraEntity.xOld = position.x();
            cameraEntity.yOld = position.y();
            cameraEntity.zOld = position.z();
            cameraEntity.xo = position.x();
            cameraEntity.yo = position.y();
            cameraEntity.zo = position.z();
            cameraEntity.setXRot(-poseElement.getPitch());
            cameraEntity.xRotO = cameraEntity.getXRot();
            cameraEntity.setYRot(poseElement.getYaw());
            cameraEntity.yHeadRot = cameraEntity.getYRot();
            cameraEntity.yHeadRotO = cameraEntity.getYRot();
            cameraEntity.eyeHeight = 0.0001F;
        }
    }

    @Override
    @Unique
    public void visor$cacheCameraEntity(Entity cameraEntity) {
        if (this.minecraft.getCameraEntity() != null) {
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
        if (cameraEntity != null) {
            visor$cameraEntityCache.apply(cameraEntity);
            this.visor$cameraEntityCached = false;
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
    public float visor$isInBlock() {
        return visor$inBlock;
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
    private void visor$setupOverlayStatus(float partialTicks) {
        //@TODO add post process for these effects
        this.visor$inBlock = 0.0F;

        this.visor$onfire = false;

        if(minecraft.player.isSpectator()
                || !minecraft.player.isAlive()
                || VRRenderState.isInMainMenu()){
            return;
        }
        var cameraPos = RenderPoseHelper.getCameraPosition(
                VRRenderState.getCameraType(),
                ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
        );
        Optional<VREffectsHelper.NearestOpaqueBlock> nearSolidBlock = RenderHelper
                .findNearestSolidBlock(
                        new Vec3((Vector3f) cameraPos),
                        this.visor$nearClipPlane
                );

        if(nearSolidBlock.isPresent()){
            var solid = nearSolidBlock.get();
            boolean renderOverlay = ModLoader.get()
                    .renderBlockOverlay(
                            this.minecraft.player,
                            new PoseStack(),
                            solid.state(),
                            solid.position()
                    );
            if(!renderOverlay){
                this.visor$inBlock = solid.distance();
            }else {
                this.visor$inBlock = 0f;
            }
        }else{
            this.visor$inBlock = 0f;
        }


        this.visor$onfire = VRRenderState.getCameraType() != VRCameraType.THIRD_PERSON
                && this.minecraft.player.isOnFire()
                && !ModLoader.get().renderFireOverlay(
                this.minecraft.player, new PoseStack()
        );
    }

    @Unique
    public Vec3 visor$aimedPointAtDistance(PoseElement poseElement,
                                           double distance) {
        var dir = poseElement.getDirection();
        return new Vec3(poseElement
                .getPosition().add(
                        dir.x() * (float) distance,
                        dir.y() * (float) distance,
                        dir.z() * (float) distance,
                        new Vector3f()
                )
        );
    }

    @Unique
    public HitResult visor$pickBlock(PoseElement poseElement,
                                     double blockReachDistance,
                                     boolean fluid
    ) {
        var position = poseElement.getPosition();
        Vec3 aimedPointAtDistance = visor$aimedPointAtDistance(poseElement, blockReachDistance);
        return MC.level.clip(
                new ClipContext(
                        new Vec3((Vector3f) position),
                        aimedPointAtDistance,
                        ClipContext.Block.OUTLINE,
                        fluid
                                ? ClipContext.Fluid.ANY
                                : ClipContext.Fluid.NONE,
                        MC.player
                )
        );
    }

}
