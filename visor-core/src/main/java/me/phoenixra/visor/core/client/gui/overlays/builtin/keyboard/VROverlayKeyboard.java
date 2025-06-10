package me.phoenixra.visor.core.client.gui.overlays.builtin.keyboard;


import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.ModelViewAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.types.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.screens.VRKeyboardScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;


public class VROverlayKeyboard extends VROverlayScreenInScreen<VRKeyboardScreen> {
    public static final String ID = "keyboard";

    private final Vector3f posOffset = new Vector3f(0,-0.5f,-0.6f);
    private final Vector3f rotationOffset = new Vector3f(0,0,0);

    @Getter
    private boolean shiftPressed = false;

    @Getter
    @Nullable
    private Screen attachedTo;

    @Getter
    private boolean shown;
    public VROverlayKeyboard(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id,
                new VRKeyboardScreen(Component.literal(""))
        );
        getScreen().setOverlayKeyboard(this);
        overlayScale = 0.5f;
        setEnabled(true);
    }


    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        if(ClientContext.cursorHandler.getFocusedOverlayAsScreen() != this){
            getScreen().clearPress();
        }
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }


    @Override
    public boolean updateVisibility() {
        return shown;
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }


    public void setShiftPressed(boolean shift) {
        if (shift != this.shiftPressed) {
            this.shiftPressed = shift;
            this.initAgain = true;
        }
    }


    public boolean showKeyboard(boolean flag){
        return showKeyboard(flag, null);
    }

    public boolean showKeyboard(boolean flag,
                                @Nullable Screen attachedTo) {
        shown = flag;

        if (shown) {
            orient(attachedTo);
            shiftPressed = false;
            initAgain = true;
        } else {
            getScreen().clearPress();
            this.attachedTo = null;
        }

        return shown;

    }


    public void updateOrient(){
        VROverlayHelper.applyModelView(
                this,
                ModelViewAnchor.HMD,
                ModelViewAnchor.HMD,
                true,
                posOffset,
                rotationOffset
        );

    }
    private void orient(@Nullable Screen attachedTo) {
        if (!shown) {
            this.attachedTo = null;
            return;
        }

        this.attachedTo = attachedTo;

        VROverlayHelper.applyModelView(
                this,
                ModelViewAnchor.HMD,
                ModelViewAnchor.HMD,
                true,
                posOffset,
                rotationOffset
        );

    }

    @Override
    public void applyModelView(float partialTick) {

    }



    @Override
    public boolean supportsTwoHandedCursor() {
        return true;
    }

    @Override
    public boolean isCursorSupported() {
        return shown;
    }

    @Override
    public boolean ignoreFacingGui() {
        return true;
    }

    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(
        );
    }

}
