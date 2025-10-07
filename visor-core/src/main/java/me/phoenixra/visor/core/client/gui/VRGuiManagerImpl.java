package me.phoenixra.visor.core.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.visor.api.client.gui.VRGuiManager;
import me.phoenixra.visor.api.common.addon.element.VisorRegistry;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@Getter
public class VRGuiManagerImpl implements VRGuiManager {
    private final int guiWidth = 1920;
    private final int guiHeight = 1080;



    //TODO Rework scaling.  make it changeable on each overlay maybe?
    private int scaleFactor;
    private int guiScaledWidth;
    private int guiScaledHeight;

    public VRGuiManagerImpl(){
        scaleFactor = calculateScale(
                0,
                guiWidth, guiHeight
        );
        guiScaledWidth = Mth.ceil(guiWidth / (float) scaleFactor);
        guiScaledHeight = Mth.ceil(guiHeight / (float) scaleFactor);

        ClientContext.overlayManager = new VROverlayManagerImpl();
        ClientContext.cursorHandler = new VRCursorHandlerImpl();

    }



    public void renderGUI(PoseStack poseStack,
                          float partialTicks) {
        ClientContext.overlayManager.renderOverlays(
                partialTicks,
                poseStack
        );
    }
    public int calculateScale(int scaleIn,
                              int guiWidth,
                              int guiHeight) {
        int scale = 1;
        for (int i = 1;
             i < guiWidth
                     && i < guiHeight
                     && guiWidth / (i + 1) >= 320
                     && guiHeight / (i + 1) >= 240;
             i++) {

            if (scale < scaleIn || scaleIn == 0) {
                scale++;
            }
        }

        //TODO needed?
        /*if (forceUnicode) {
            if (scale % 2 != 0) {
                scale++;
            }
        }*/

        return scale;
    }

    public boolean updateResolution() {
        int oldWidth = guiWidth;
        int oldGuiScale = scaleFactor;
        scaleFactor = calculateScale(
                (int) Math.ceil(((int) VRClientSettings.getGuiScale()) * 0.5f),
                guiWidth,
                guiHeight
        );
        guiScaledWidth = Mth.ceil(guiWidth / (float) scaleFactor);
        guiScaledHeight = Mth.ceil(guiHeight / (float) scaleFactor);
        if (oldWidth != guiWidth) {
            return true;
        } else {
            return oldGuiScale != scaleFactor;
        }
    }

    public List<VisorRegistry<?>> getElementRegistries(){
        return List.of(
                ClientContext.overlayManager.getOverlaysRegistry(),
                ClientContext.overlayManager.getOverlayTemplatesRegistry()
        );
    }

    @Override
    public @NotNull VROverlayManagerImpl getOverlayManager() {
        return ClientContext.overlayManager;
    }

    @Override
    public @NotNull VRCursorHandlerImpl getCursorHandler() {
        return ClientContext.cursorHandler;
    }
}
