package me.phoenixra.visor.core.mixin.client.render;


import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRCameraEntityCache;
import me.phoenixra.visor.core.client.render.VRGameCamera;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.render.helpers.VREffectsHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;

import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
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



    @Shadow public abstract void render(float f, long l, boolean bl);

    @Shadow
    public abstract void renderItemActivationAnimation(int i, int j, float par1);


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



    /* ******************* *\
  //--------RENDERING--------\\
    \* ******************* */

    /**
     * Cancels GUI rendering for VRWorld stage and render VR main menu room
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", ordinal = 6), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", cancellable = true)
    public void visor$onRenderGUI(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {

        if (VRRenderState.getCurrentPhase().isNotVRWorld()) {
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
        if (VRRenderState.getCurrentPhase().isVanilla()) {
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
        if (VRRenderState.getCurrentPhase().isVRWorld()) {
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

        VRDisplay display = VRRenderState.getCurrentVRDisplay();
        if(display == VRDisplay.EYE_LEFT){
            posestack.mulPoseMatrix(
                    ClientContext.renderer.getEyeProjection(EyeType.LEFT)
            );
            info.setReturnValue(
                    posestack.last().pose()
            );
            return;
        }
        if (display == VRDisplay.EYE_RIGHT) {
            posestack.mulPoseMatrix(
                    ClientContext.renderer.getEyeProjection(EyeType.RIGHT)
            );
            info.setReturnValue(posestack.last().pose());
            return;
        }
        if (display == VRDisplay.THIRD_PERSON) {
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


    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void visor$pickAndSetupCamera(GameRenderer g, float pPartialTicks) {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }
        if (VRRenderState.getCurrentVRDisplay() == VRDisplay.worldUpdater()) {
            this.pick(pPartialTicks);

            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != HitResult.Type.MISS) {
                this.visor$crossVec = this.minecraft.hitResult.getLocation();
            }
        }

        this.visor$cacheCameraEntity((LivingEntity) this.minecraft.getCameraEntity());
        this.visor$setupCameraEntity();
        this.visor$setupOverlayStatus(pPartialTicks);
    }

    @Inject(at = @At(value = "TAIL"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void visor$restoreCamera(float f, long j, PoseStack p, CallbackInfo i) {
        if(VRRenderState.getCurrentPhase().isNotVanilla()) {
            this.visor$restoreCameraEntity(
                    (LivingEntity) this.minecraft.getCameraEntity()
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
        PoseDataImpl renderPose = ClientContext.player
                .getPose(PoseDataType.RENDER);

        ControllerHand activeHand = ClientContext.player.getActiveHand();

        this.minecraft.hitResult = visor$pickBlock(
                renderPose.getController(activeHand),
                this.minecraft.gameMode.getPickRange(),
                false
        );
        this.visor$crossVec = visor$aimedPointAtDistance(
                renderPose.getController(activeHand),
                this.minecraft.gameMode.getPickRange()
        );

        return new Vec3((Vector3f) renderPose.getController(activeHand).getPosition());
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 visor$pickDirection(Vec3 original) {
        if (VisorState.getState().isNotActive()) {
            return original;
        }
        ControllerHand activeHand = ClientContext.player.getActiveHand();

        return new Vec3(
                (Vector3f) ClientContext.player.getPose(PoseDataType.RENDER)
                        .getController(activeHand).getDirection()
        );
    }



    /* ******************************* *\
      //--------DISABLE VANILLA STAFF--------\\
        \* ******************************* */
    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean visor$noPostEffectOnThirdPerson(GameRenderer instance) {
        return this.effectActive && VRRenderState.getCurrentVRDisplay() != VRDisplay.THIRD_PERSON;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean visor$noPauseGameIfWindowNotFocused(Minecraft instance) {
        return VisorState.getState().isActive() || instance.isWindowActive();
    }


    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void visor$noFOVchangeInVR(CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isNotVanilla()) {
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
        if(VRRenderState.getCurrentPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void visor$noBobView(PoseStack matrixStack,
                                float f,
                                CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void visor$noConfusionOverlayInGUI(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
        if (VRRenderState.getCurrentPhase().isVRGui()) {
            ci.cancel();
        }
    }



    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean visor$noVanillaHands(GameRenderer instance) {
        return VRRenderState.getCurrentPhase().isVanilla() && renderHand;
    }

    @Inject(at = @At("TAIL"), method = "renderLevel")
    public void visor$disableStencil(float f, long l, PoseStack poseStack, CallbackInfo ci) {
        if(VRRenderState.getCurrentPhase().isNotVanilla()) {
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
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            poseStack.scale(x, y, z);
            return;
        }
        VRDisplay currentDisplay = VRRenderState.getCurrentVRDisplay();
        // need to do stuff twice, because redirects have no access to locals
        int i = 40 - this.itemActivationTicks;
        float g = ((float) i + partialTicks) / 40.0f;
        float h = g * g;
        float l = g * h;
        float m = 10.25f * l * h - 24.95f * h * h + 25.5f * l - 13.8f * h + 4.0f * g;
        float n = m * (float) Math.PI;
        float sinN = Mth.sin(n) * 0.5F;
        poseStack.translate(0, 0, sinN - 1.0);
        if (currentDisplay == VRDisplay.THIRD_PERSON) {
            float fov;
            if(VRClientSettings.getMirrorMode() == MirrorMode.MIXED_REALITY){
                fov = VRClientSettings.getMixedRealityFov();
            }else{
                fov = VRClientSettings.getThirdPersonFov();
            }
            sinN *= (float) (fov / 70.0);
        }
        RenderPoseHelper.applyDisplayPose(currentDisplay, poseStack);
        poseStack.scale(sinN, sinN, sinN);
        poseStack.mulPose(Axis.YP.rotationDegrees(-ClientContext.player.getPose(PoseDataType.RENDER).getElementForDisplay(currentDisplay).getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-ClientContext.player.getPose(PoseDataType.RENDER).getElementForDisplay(currentDisplay).getPitch()));
    }
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void visor$noItemActivationAnimInGUI(GameRenderer instance, int i, int j, float f) {
        if(VRRenderState.getCurrentPhase().isVanilla()) {
            renderItemActivationAnimation(i, j, f);
        }
    }
    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void visor$noItemTranslate(PoseStack poseStack, float x, float y, float z) {
        if(VRRenderState.getCurrentPhase().isVanilla()) {
            poseStack.translate(x, y, z);
        }
    }
    //--

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void visor$pauseOnlyOnTickDisplay(Minecraft instance, boolean bl) {
        if (VisorState.getState().isNotActive() || VRRenderState.getCurrentVRDisplay() == VRDisplay.worldUpdater()) {
            instance.pauseGame(bl);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long visor$useActiveTimeOnlyOnTickDisplay() {
        if (VisorState.getState().isNotActive() || VRRenderState.getCurrentVRDisplay() == VRDisplay.worldUpdater()) {
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
    public void visor$setupCameraEntity() {
        if (this.visor$cameraEntityCached) {
            PoseElement eye = ClientContext.player
                    .getPose(PoseDataType.RENDER)
                    .getElementForDisplay(VRRenderState.getCurrentVRDisplay());
            var eyePos = eye.getPosition();
            LivingEntity cameraEntity = (LivingEntity) this.minecraft.getCameraEntity();
            cameraEntity.setPosRaw(eyePos.x(), eyePos.y(), eyePos.z());
            cameraEntity.xOld = eyePos.x();
            cameraEntity.yOld = eyePos.y();
            cameraEntity.zOld = eyePos.z();
            cameraEntity.xo = eyePos.x();
            cameraEntity.yo = eyePos.y();
            cameraEntity.zo = eyePos.z();
            cameraEntity.setXRot(-eye.getPitch());
            cameraEntity.xRotO = cameraEntity.getXRot();
            cameraEntity.setYRot(eye.getYaw());
            cameraEntity.yHeadRot = cameraEntity.getYRot();
            cameraEntity.yHeadRotO = cameraEntity.getYRot();
            cameraEntity.eyeHeight = 0.0001F;
        }
    }

    @Override
    @Unique
    public void visor$cacheCameraEntity(LivingEntity cameraEntity) {
        if (this.minecraft.getCameraEntity() != null) {
            if (!this.visor$cameraEntityCached) {
                visor$cameraEntityCache = new VRCameraEntityCache(
                        cameraEntity.getX(), cameraEntity.getY(),
                        cameraEntity.getZ(),

                        cameraEntity.xOld, cameraEntity.yOld,
                        cameraEntity.zOld,

                        cameraEntity.xo, cameraEntity.yo,
                        cameraEntity.zo,

                        cameraEntity.yHeadRot, cameraEntity.getXRot(),

                        cameraEntity.yHeadRotO, cameraEntity.xRotO,

                        cameraEntity.getEyeHeight()
                );
                this.visor$cameraEntityCached = true;
            }
        }
    }

    @Override
    @Unique
    public void visor$restoreCameraEntity(LivingEntity cameraEntity) {
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
                VRRenderState.getCurrentVRDisplay(),
                ClientContext.player.getPose(PoseDataType.RENDER)
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


        this.visor$onfire = VRRenderState.getCurrentVRDisplay() != VRDisplay.THIRD_PERSON
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
