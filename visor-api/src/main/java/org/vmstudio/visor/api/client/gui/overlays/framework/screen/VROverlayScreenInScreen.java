package org.vmstudio.visor.api.client.gui.overlays.framework.screen;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;

import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The {@link VROverlayScreen} that renders other {@link Screen}
 */
@Getter
public abstract class VROverlayScreenInScreen<T extends Screen> extends VROverlayScreen {
    protected T screen;

    public VROverlayScreenInScreen(@NotNull VisorAddon owner,
                                   @NotNull String id,
                                   @Nullable T screen) {
        this(owner, id, ComponentPriority.NORMAL, 1.0f, screen);

    }

    public VROverlayScreenInScreen(@NotNull VisorAddon owner,
                                   @NotNull String id,
                                   @NotNull ComponentPriority priority,
                                   float overlayScale,
                                   @Nullable T screen) {
        super(owner, id, priority, overlayScale);
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
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        if (super.mouseClicked(mouseX, mouseY, buttonType)) {
            return true;
        }
        if(screen==null) return true;
        return screen.mouseClicked(mouseX, mouseY, buttonType);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        if (super.mouseReleased(mouseX, mouseY, buttonType)) {
            return true;
        }
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
