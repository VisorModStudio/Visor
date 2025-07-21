package me.phoenixra.visor.core.mixin.client.gui.screen;

import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.framework.VROverlayTemplateScreenInScreen;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.VROverlayGameScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 1)
    )
    private boolean visorEssentials$noDraggingItem(ItemStack instance) {
        var focused = ClientContext.cursorHandler.getFocusedOverlay();
        if(focused instanceof VROverlayGameScreen){
            if(MC.screen == this){
                return instance.isEmpty();
            }
        }
        if(focused instanceof VROverlayScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() == this){
                return instance.isEmpty();
            }
        }
        if(focused instanceof VROverlayTemplateScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() == this){
                return instance.isEmpty();
            }
        }
        return true;
    }
}
