package me.phoenixra.visor.api.client.gui.overlays.framework.template;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;


/**
 * Abstract class for {@link VROverlayScreen} templates,
 * that render specified {@link Screen}.
 */
public abstract class VROverlayTemplateScreenInScreen<T extends Screen> extends VROverlayTemplateScreen{

    @Getter
    protected T screen;

    public VROverlayTemplateScreenInScreen(@NotNull VisorAddon owner, @NotNull String id) {
        super(owner, id);
    }

    public VROverlayTemplateScreenInScreen(@NotNull VisorAddon owner, @NotNull String id, @NotNull ElementPriority priority, float overlayScale) {
        super(owner, id, priority, overlayScale);
    }



    @Override
    protected void init() {
        if(screen!=null){
            screen.init(
                    Minecraft.getInstance(),
                    width,
                    height
            );
        }
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

        if(screen!=null) {
            screen.renderWithTooltip(guiGraphics, mouseX, mouseY, partialTicks);
        }

    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        if(screen==null) return true;
        return screen.mouseClicked(mouseX, mouseY, buttonType);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        if(screen==null) return true;
        return screen.mouseReleased(mouseX, mouseY, buttonType);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(screen==null) return;
        screen.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY,
                                int buttonType,
                                double dragX, double dragY
    ) {
        if(screen==null) return true;
        return screen.mouseDragged(mouseX, mouseY, buttonType, dragX, dragY);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if(screen==null) return true;
        return screen.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int keyScan, int modifiers) {
        if(screen==null) return true;
        return screen.keyPressed(keyCode, keyScan, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int keyScan, int modifiers) {
        if(screen==null) return true;
        return screen.keyReleased(keyCode, keyScan, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if(screen==null) return true;
        return screen.charTyped(chr, modifiers);
    }

    @Override
    protected void onTick() {
        if(screen != null && isVisible()){
            screen.tick();
        }
    }


}
