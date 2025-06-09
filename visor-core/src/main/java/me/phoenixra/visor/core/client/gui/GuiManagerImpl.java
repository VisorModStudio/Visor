package me.phoenixra.visor.core.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.visor.api.client.gui.VRCursorHandler;
import me.phoenixra.visor.api.client.gui.GuiManager;
import me.phoenixra.visor.api.client.gui.OverlayManager;
import me.phoenixra.visor.api.common.addon.VisorElementRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class GuiManagerImpl implements GuiManager {
    @Getter
    private final int guiWidth = 1280;
    @Getter
    private final int guiHeight = 720;



    @Getter
    private int scaleFactor = calculateScale(
            0, false,
            guiWidth, guiHeight
    );
    @Getter
    private int scaledGuiWidth;
    @Getter
    private int scaledGuiHeight;
    public GuiManagerImpl(){
        ClientContext.overlayManager = new OverlayManagerImpl();
        ClientContext.cursorHandler = new VRCursorHandlerImpl();
    }



    public void renderGUI(PoseStack poseStack,
                          float partialTicks,
                          boolean depthAlways) {

        ClientContext.cursorHandler.process();

        ClientContext.overlayManager.renderOverlays(
                partialTicks,
                depthAlways,
                poseStack
        );
    }
    public int calculateScale(int scaleIn,
                              boolean forceUnicode,
                              int frameBufferWidth,
                              int frameBufferHeight) {
        int scale = 1;
        for (int i = 1;
             i < frameBufferWidth
                     && i < frameBufferHeight
                     && frameBufferWidth / (i + 1) >= 320
                     && frameBufferHeight / (i + 1) >= 240;
             i++) {

            if (scale < scaleIn || scaleIn == 0) {
                scale++;
            }
        }

        if (forceUnicode) {
            if (scale % 2 != 0) {
                scale++;
            }
        }

        scaledGuiWidth = Mth.ceil(frameBufferWidth / (float) scale);
        scaledGuiHeight = Mth.ceil(frameBufferHeight / (float) scale);

        return scale;
    }

    public boolean updateResolution() {
        int oldWidth = guiWidth;
        int oldGuiScale = scaleFactor;
        scaleFactor = calculateScale(
                (int) Math.ceil(((int) VRClientSettings.getGuiScale()) * 0.5f),
            false,
                guiWidth,
                guiHeight
        );
        if (oldWidth != guiWidth) {
            return true;
        } else {
            return oldGuiScale != scaleFactor;
        }
    }

    public List<VisorElementRegistry<?>> getElementRegistries(){
        return List.of(
                ClientContext.overlayManager.getOverlaysRegistry(),
                ClientContext.overlayManager.getOverlayTypesRegistry()
        );
    }

    @Override
    public @NotNull OverlayManager getOverlayManager() {
        return ClientContext.overlayManager;
    }

    @Override
    public @NotNull VRCursorHandler getCursorHandler() {
        return ClientContext.cursorHandler;
    }
}
