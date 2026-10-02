// #!MC-VERSION:: 26.2+
package org.vmstudio.visor.mixin.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.MinecraftExtension;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
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
    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;clearDepthTexture(Lcom/mojang/blaze3d/textures/GpuTexture;D)V", remap = false, ordinal = 0), method = "render", cancellable = true, require = 1)
    public void visor$onRenderGUI(CallbackInfo info) {

        if (VRRenderState.getPhase().isNotVRWorld()) {
            // Proceed rendering GUI for Vanilla and VRGui stage
            return;
        }

        info.cancel();
        McVersionClientUtils.profiler().pop();

        // the cancel lands before the tail of render(), where 1.21.9 ends the submit and feature frames
        ((GameRenderer) (Object) this).renderBuffers().endFrame();

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
    @Unique
    private boolean visor$vrGuiVisibility(boolean doRender) {
        if (VRRenderState.getPhase().isVanilla()) {
            return doRender;
        }
        return visor$isVRGuiVisible() && MC.isGameLoadFinished() && MC.level != null;
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractRenderState(Lnet/minecraft/client/DeltaTracker;ZZ)V"), method = "extract", require = 1)
    private void visor$extractVRGui(Gui gui, DeltaTracker deltaTracker, boolean renderLevel, boolean resourcesLoaded,
                                    Operation<Void> original) {
        if (VRRenderState.getPhase().isVRWorld()) {
            return;
        }
        original.call(gui, deltaTracker, visor$vrGuiVisibility(renderLevel), resourcesLoaded);
    }



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
