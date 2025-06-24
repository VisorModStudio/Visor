package me.phoenixra.visor.core.client.gui.overlays.builtin;


import me.phoenixra.visor.api.client.gui.overlay.ModelViewAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


public class VROverlayDraggedItem extends VROverlayScreen {
    public static final String ID = "dragged_item";

    private Vector3f orientPosOffset = new Vector3f(0,0,-0.6f);
    private Vector3f orientRotationOffset = new Vector3f(0,0,0);

    public VROverlayDraggedItem(@NotNull VisorAddon owner,
                                @NotNull String id
    ) {
        super(owner, id);
        overlayScale = 1f;
        setPriority(ElementPriority.HIGHER);
        setEnabled(true);
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

        ClientContext.overlayManager.showKeyboard(false);

        ClientContext.cursorHandler.setDraggingItem(true);
        renderFloatingItem(
                guiGraphics,
                minecraft.player.containerMenu.getCarried(),
                width/2 - 8,height/2 - 8,
                null
        );
    }

    private void renderFloatingItem(GuiGraphics guiGraphics,
                                    ItemStack itemStack,
                                    int posX, int posY,
                                    String string) {
        guiGraphics.pose().pushPose();
        guiGraphics.renderItem(itemStack, posX, posY);
        guiGraphics.renderItemDecorations(
                this.font,
                itemStack,
                posX, posY, string
        );
        guiGraphics.pose().popPose();
    }

    @Override
    protected void onTick() {

    }

    @Override
    public boolean updateVisibility() {
        if(minecraft.level==null ||
                (ClientContext.cursorHandler.isCursorHandFocused()
                        && ClientContext.cursorHandler.getFocusedOverlayScreen() != this)
                || minecraft.player.containerMenu == null
                || minecraft.player.containerMenu.getCarried() == null
                || minecraft.player.containerMenu.getCarried().isEmpty()) {

            ClientContext.cursorHandler.setDraggingItem(false);
            return false;
        }
        return true;
    }


    @Override
    public void applyModelView(float partialTick) {
        VROverlayHelper.applyModelView(
                this,
                ModelViewAnchor.MAIN_HAND,
                ModelViewAnchor.MAIN_HAND,
                true,
                orientPosOffset,
                orientRotationOffset

        );
    }


    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        this.minecraft.gameMode.handleInventoryMouseClick(
                minecraft.player.containerMenu.containerId,
                -999, buttonType, ClickType.PICKUP, this.minecraft.player
        );
        return true;
    }
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    @Override
    public boolean isCursorSupported() {
        return false;
    }


    public static boolean isDraggingItem(){
        return MC.level!=null
                && MC.player.containerMenu != null
                && MC.player.containerMenu.getCarried() != null
                && !MC.player.containerMenu.getCarried().isEmpty();
    }


    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(
        );
    }
}
