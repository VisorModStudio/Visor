package org.vmstudio.visor.compatibility.iris.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.VRRenderState;

@Pseudo
@Mixin(targets = {
        "net.irisshaders.iris.shadows.ShadowMatrices",
        "net.coderbot.iris.shadows.ShadowMatrices",
        "net.coderbot.iris.shadow.ShadowMatrices"
}, remap = false)
public class IrisShadowMatricesMixin {
    @Unique
    private static long visor$anchorFrame = Long.MIN_VALUE;
    @Unique
    private static double visor$anchorX;
    @Unique
    private static double visor$anchorY;
    @Unique
    private static double visor$anchorZ;

    @Inject(method = "snapModelViewToGrid", at = @At("HEAD"), cancellable = true)
    private static void visor$consistentEyeSnap(PoseStack target, float shadowIntervalSize,
                                                double cameraX, double cameraY, double cameraZ,
                                                CallbackInfo ci) {
        if (!IrisCompatHelper.syncShadowGrid() || !VisorState.get().isActive()
                || VRRenderState.getPhase().isNotVRWorld()) {
            return;
        }
        VRRenderPass pass = VRRenderState.getRenderPass();
        if (pass == null || !pass.isWorld()) {
            return;
        }
        if (Math.abs(shadowIntervalSize) == 0.0F) {
            return;
        }

        if (visor$anchorFrame != VisorState.FRAME_COUNT) {
            visor$anchorFrame = VisorState.FRAME_COUNT;
            visor$anchorX = cameraX;
            visor$anchorY = cameraY;
            visor$anchorZ = cameraZ;
            return;
        }

        float half = shadowIntervalSize / 2.0F;
        float offsetX = (float) visor$anchorX % shadowIntervalSize - half + (float) (cameraX - visor$anchorX);
        float offsetY = (float) visor$anchorY % shadowIntervalSize - half + (float) (cameraY - visor$anchorY);
        float offsetZ = (float) visor$anchorZ % shadowIntervalSize - half + (float) (cameraZ - visor$anchorZ);

        target.last().pose().translate(offsetX, offsetY, offsetZ);
        ci.cancel();
    }
}
