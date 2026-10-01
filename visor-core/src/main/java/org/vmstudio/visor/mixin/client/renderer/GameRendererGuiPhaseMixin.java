package org.vmstudio.visor.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
//? if >=26.1 {
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
//?}
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.MinecraftExtension;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
//? if <1.21.2 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

// GameRenderer.render() GUI pass: cancelled in the VR world phase, VR main menu scene drawn instead
@Mixin(GameRenderer.class)
public abstract class GameRendererGuiPhaseMixin implements GameRendererExtension {

    // ---- Unique fields ----
    @Unique
    private boolean visor$isVRGuiVisible;


    /* ***************** *\
  //--------MIXINS--------\\
    \* ***************** */

    /**
     * Cancels GUI rendering for VRWorld stage and render VR main menu room
     */
    //? if >=1.21.5 {
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V", remap = false, ordinal = 0), method = "render", cancellable = true, require = 1)
    //?} else {
    /*@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", ordinal = 6), method = "render", cancellable = true, require = 1)
    *///?}
    public void visor$onRenderGUI(CallbackInfo info) {

        if (VRRenderState.getPhase().isNotVRWorld()) {
            // Proceed rendering GUI for Vanilla and VRGui stage
            return;
        }

        info.cancel();
        //? if >=26.1 {
        McVersionClientUtils.profiler().pop();
        //?}

        //? if >=1.21.9 {
        // the cancel lands before the tail of render(), where 1.21.9 ends the submit and feature frames
        GameRenderer self = (GameRenderer) (Object) this;
        self.getSubmitNodeStorage().endFrame();
        self.getFeatureRenderDispatcher().endFrame();
        //?}

        // Render Main Menu View
        if (VRRenderState.getSceneType().isMainMenu()) {

            GL11.glDisable(GL11.GL_STENCIL_TEST);

            float partialTicks = ((MinecraftExtension) MC).visor$getPartialTicks();
            // 1.21.6 renderLevel leaves vanilla's hud3d projection behind
            visor$resetProjectionMatrix(partialTicks);

            PoseStack poseStack = new PoseStack();
            //render VR main menu
            ClientContext.decorationRenderer.renderMainMenu(
                    poseStack,
                    partialTicks
            );
        }
    }

    /**
     * Draw GUI only after first level render
     */
    //? if >=26.1 {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V"), method = "extract", index = 1, require = 1)
    //?} elif >=1.21.5 {
    /*@ModifyVariable(at = @At(value = "NEW", target = "net/minecraft/client/gui/GuiGraphicsExtractor", shift = Shift.AFTER), method = "render", ordinal = 0, argsOnly = true, require = 1)
    *///?} else {
    /*@ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.AFTER, ordinal = 6), method = "render", ordinal = 0, argsOnly = true, require = 1)
    *///?}
    private boolean visor$vrGuiVisibility(boolean doRender) {
        if (VRRenderState.getPhase().isVanilla()) {
            return doRender;
        }
        //? if >=26.1 {
        return visor$isVRGuiVisible() && MC.isGameLoadFinished() && MC.level != null;
        //?} else {
        /*return visor$isVRGuiVisible();
        *///?}
    }

    //? if >=26.1 {
    @Inject(at = @At("HEAD"), method = "extractGui", cancellable = true)
    private void visor$noGuiExtractionInWorldPass(CallbackInfo ci) {
        if (VRRenderState.getPhase().isVRWorld()) {
            ci.cancel();
        }
    }
    //?}

    // 1.21.2 moved it into Gui, see GuiMixin
    //? if <1.21.2 {
    /*@Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void visor$noConfusionOverlayInGUI(GuiGraphicsExtractor guiGraphics, float f, CallbackInfo ci) {
        if (VRRenderState.getPhase().isVRGui()) {
            ci.cancel();
        }
    }
    *///?}


     /* ************************ *\
   //--------PUBLIC METHODS--------\\
     \* ************************ */

    @Override
    public boolean visor$isVRGuiVisible(){
        return visor$isVRGuiVisible;
    }

    @Override
    public void visor$setVRGuiVisible(boolean flag){
        visor$isVRGuiVisible = flag;
    }
}
