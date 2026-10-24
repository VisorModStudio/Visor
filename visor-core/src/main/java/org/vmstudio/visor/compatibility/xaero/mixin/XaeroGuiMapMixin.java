package org.vmstudio.visor.compatibility.xaero.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.core.client.VisorState;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@Pseudo
@MixinGate(classes = "xaero.map.gui.GuiMap")
@Mixin(targets = "xaero.map.gui.GuiMap", remap = false)
public class XaeroGuiMapMixin {

    //1.21.6+
    @Inject(method = {"extractRenderState", "render", "method_25394"},
            at = @At(value = "FIELD", target = "Lxaero/lib/client/graphics/XaeroRenderType;RP_MAP_FRAME", opcode = Opcodes.GETSTATIC),
            require = 0, remap = false)
    private void visor$opaqueMapBacking(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (VisorState.get().isNotActive()) return;
        Window window = MC.getWindow();
        graphics.fill(0, 0, window.getWidth(), window.getHeight(), 0xFF000000);
    }

    //1.21.5-
    @ModifyExpressionValue(method = {"render", "method_25394"},
            at = @At(value = "FIELD", target = "Lxaero/map/mods/SupportMods;vivecraft:Z", opcode = Opcodes.GETSTATIC),
            require = 0, remap = false)
    private boolean visor$vivecraftRenderPath(boolean original) {
        return original || VisorState.get().isActive();
    }
}
