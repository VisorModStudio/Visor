package org.vmstudio.visor.api.client.gui.overlays.framework.template;

import lombok.Getter;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    public VROverlayTemplateScreenInScreen(@NotNull VisorAddon owner, @NotNull String id, @NotNull ComponentPriority priority, float overlayScale) {
        super(owner, id, priority, overlayScale);
    }



    @Override
    protected void init() {
        if(screen!=null){
            McGuiUtils.initScreen(screen, width, height);
        }
    }

    @Override
    protected void onRender(GuiGraphicsExtractor guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

        if(screen!=null) {
            McGuiUtils.renderWithTooltip(screen, guiGraphics, mouseX, mouseY, partialTicks);
        }

    }


    @Override
    protected boolean onMouseClicked(double mouseX, double mouseY, int buttonType) {
        if(screen==null) return true;
        return McGuiUtils.mouseClicked(screen, mouseX, mouseY, buttonType);
    }

    @Override
    protected boolean onMouseReleased(double mouseX, double mouseY, int buttonType) {
        if(screen==null) return true;
        return McGuiUtils.mouseReleased(screen, mouseX, mouseY, buttonType);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if(screen==null) return;
        screen.mouseMoved(mouseX, mouseY);
    }

    @Override
    protected boolean onMouseDragged(double mouseX, double mouseY,
                                     int buttonType,
                                     double dragX, double dragY
    ) {
        if(screen==null) return true;
        return McGuiUtils.mouseDragged(screen, mouseX, mouseY, buttonType, dragX, dragY);
    }


    @Override
    protected boolean onMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        if(screen==null) return true;
        return McGuiUtils.mouseScrolled(screen, mouseX, mouseY, scrollDelta);
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int keyScan, int modifiers) {
        if(screen==null) return true;
        return McGuiUtils.keyPressed(screen, keyCode, keyScan, modifiers);
    }

    @Override
    protected boolean onKeyReleased(int keyCode, int keyScan, int modifiers) {
        if(screen==null) return true;
        return McGuiUtils.keyReleased(screen, keyCode, keyScan, modifiers);
    }

    @Override
    protected boolean onCharTyped(char chr, int modifiers) {
        if(screen==null) return true;
        return McGuiUtils.charTyped(screen, chr, modifiers);
    }

    @Override
    protected void onTick() {
        if(screen != null && isVisible()){
            screen.tick();
        }
    }


}
