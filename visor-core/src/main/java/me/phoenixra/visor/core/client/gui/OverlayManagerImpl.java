package me.phoenixra.visor.core.client.gui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import me.phoenixra.visor.api.client.gui.OverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.OverlayCatalog;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard.VROverlayKeyboard;
import me.phoenixra.visor.core.client.gui.overlays.types.VROverlayHUD;
import me.phoenixra.visor.core.client.gui.registry.VROverlayRegistry;
import me.phoenixra.visor.core.client.gui.registry.VROverlayTypeRegistry;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.OptionsScreenGlobal;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.OptionsScreenModelView;
import me.phoenixra.visor.core.client.render.helpers.VRScreenHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class OverlayManagerImpl implements OverlayManager {

    @Getter
    private final VROverlayRegistry overlaysRegistry = new VROverlayRegistry();
    @Getter
    private final VROverlayTypeRegistry overlayTypesRegistry = new VROverlayTypeRegistry();


    private VROverlayKeyboard keyboard;

    @Getter
    private boolean hudDisplayed;


    public void tick(){
        for(VROverlay overlay : overlaysRegistry.getSortedElements()){
            if(!overlay.isEnabled()) continue;
            overlay.tick();
        }

        hudDisplayed = overlaysRegistry.getElementsByType(
                VROverlayHUD.ID_TYPE
        ).stream().anyMatch(VROverlay::isVisible);

    }

    public void renderOverlayTextures(ProfilerFiller profiler,
                                      GuiGraphics guiGraphics,
                                      float actualPartialTicks) {
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if(!overlay.isVisible()) continue;
            profiler.push("Overlay Render " + overlay.getId());
            if(overlay instanceof VROverlayScreen overlayScreen) {
                RenderTarget target = overlay.getRenderTarget();
                if(target == null){
                    throw new RuntimeException("Tried to render overlay with");
                }
                MC.mainRenderTarget = target;
                target.clear(Minecraft.ON_OSX);
                target.bindWrite(true);
                VRScreenHelper.drawScreen(
                        actualPartialTicks,
                        overlayScreen,
                        guiGraphics,
                        overlayScreen.getMouseX(),
                        overlayScreen.getMouseY()
                );
                guiGraphics.flush();
            }else if(overlay instanceof VROverlayFrameBuffer overlayFrameBuffer){
                overlayFrameBuffer.render(actualPartialTicks);
            }
            profiler.pop();
        }
    }

    public void renderOverlays(float partialTicks,
                               boolean depthAlways,
                               PoseStack poseStack) {
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if(!overlay.isVisible()) continue;
            overlay.applyModelView(partialTicks);
            VRScreenHelper.renderOverlay2D(
                    partialTicks,
                    overlay.getRenderTarget(),
                    overlay.getPosition(),
                    overlay.getRotation(),
                    depthAlways, poseStack,
                    overlay.getOverlayScale()
            );
        }
    }

    @Override
    public boolean isEnabled(@NotNull String id) {
        VROverlay overlay = getOverlay(id);
        if (overlay == null) return false;
        return overlay.isEnabled();
    }

    @Override
    public boolean isEnabledAtLeastOne() {
        for (VROverlay overlay : overlaysRegistry.getSortedElements()) {
            if (overlay.isEnabled()) return true;
        }
        return false;
    }


    @Override
    public VROverlay getOverlay(@NotNull String id) {
        return overlaysRegistry.getElement(id);
    }

    @Override
    public boolean isShowingKeyboard() {
        return getKeyboardOverlay().isShown();
    }

    @Override
    public void showKeyboard(boolean flag) {
        getKeyboardOverlay().showKeyboard(flag);
    }
    @Override
    public boolean showKeyboard(boolean flag,
                                @Nullable Screen attachedTo){
        return getKeyboardOverlay().showKeyboard(flag,attachedTo);
    }

    @Override
    public Screen getKeyboardAttachedTo() {
        return getKeyboardOverlay().getAttachedTo();
    }


    public VROverlayKeyboard getKeyboardOverlay() {
        if(keyboard == null) {
            keyboard = (VROverlayKeyboard) getOverlay("keyboard");
        }
        return keyboard;
    }

    @Override
    public @NotNull OverlayCatalog getOverlayCatalog() {
        return ClientContext.settingsHandler.getOverlayCatalog();
    }

    @Override
    public @NotNull OverlayOptionsScreen<?> getOptionsScreenFor(@NotNull OverlayOptionCategory category, float mainMenuWidth, float mainMenuHeight) {
        if(category instanceof OverlayOptionsGlobal category1){
            return new OptionsScreenGlobal(category1,mainMenuWidth,mainMenuHeight);
        }else if(category instanceof OverlayOptionsModelView category1){
            return new OptionsScreenModelView(category1, mainMenuWidth, mainMenuHeight);
        }
        return null; // tsss, secret
    }
}
