package me.phoenixra.visor.core.mixin.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {
    @Redirect(
            method = "renderTooltipInternal",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"
            )
    )
    private void visor$redirectPoseTranslate(PoseStack pose, float x, float y, float z) {
        //fixes issue with no tooltip text in overlays
        if(VisorState.getState().isNotActive()){
            pose.translate(x, y, z);
            return;
        }
        if (ClientContext.cursorHandler.getFocusedOverlayAsScreen() != null
                && x == 0.0F && y == 0.0F && z == 400.0F) {
            pose.translate(0.0F, 0.0F, -2000.0F);
        } else {
            pose.translate(x, y, z);
        }
    }
}
