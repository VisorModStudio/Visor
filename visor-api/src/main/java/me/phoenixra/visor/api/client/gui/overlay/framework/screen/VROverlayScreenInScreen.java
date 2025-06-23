package me.phoenixra.visor.api.client.gui.overlay.framework.screen;

import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;

import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The overlay that draws mc screen.
 * It also handles mouse and keyboard actions via this screen
 */
@Getter
public abstract class VROverlayScreenInScreen<T extends Screen> extends VROverlayScreen {
    protected T screen;

    public VROverlayScreenInScreen(@NotNull VisorAddon owner,
                                   @NotNull String id,
                                   @Nullable T screen) {
        super(owner, id);
        this.screen = screen;

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
    public boolean mouseClicked(double x, double y, int buttonType) {
        if(screen==null) return true;
        return screen.mouseClicked(x, y, buttonType);
    }

    @Override
    public boolean mouseReleased(double x, double y, int buttonType) {
        if(screen==null) return true;
        return screen.mouseReleased(x, y, buttonType);
    }

    @Override
    public void mouseMoved(double d, double e) {
        if(screen==null) return;
        screen.mouseMoved(d, e);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY,
                                int button,
                                double dragX, double dragY
    ) {
        if(screen==null) return true;
        return screen.mouseDragged(mouseX, mouseY, button, dragX, dragY);
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
