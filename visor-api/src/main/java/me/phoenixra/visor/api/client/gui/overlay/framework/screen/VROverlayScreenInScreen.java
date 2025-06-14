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
    public boolean mouseClicked(double d, double e, int i) {
        if(screen==null) return true;
        return screen.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if(screen==null) return true;
        return screen.mouseReleased(d, e, i);
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
    public boolean mouseScrolled(double d, double e, double f) {
        if(screen==null) return true;
        return screen.mouseScrolled(d, e, f);
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
    public boolean charTyped(char c, int i) {
        if(screen==null) return true;
        return screen.charTyped(c, i);
    }

    @Override
    protected void onTick() {
        if(screen != null && isVisible()){
            screen.tick();
        }
    }
}
