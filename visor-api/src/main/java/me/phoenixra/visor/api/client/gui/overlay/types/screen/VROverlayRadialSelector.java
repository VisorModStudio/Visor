package me.phoenixra.visor.api.client.gui.overlay.types.screen;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.GuiManager;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The overlay that has selection boxes.
 */
public abstract class VROverlayRadialSelector extends VROverlayScreen {
    protected final int radialMenuSize;

    protected HashMap<Integer, SelectionBox> selectionBoxes;
    @Getter
    protected List<Integer> disabledBoxes;


    @Getter
    @Setter
    private int selectedSlice = -1;

    protected VROverlayRadialSelector(@NotNull VisorAddon owner,
                                      @NotNull ControllerHand controller,
                                      @NotNull String id,
                                      int radialMenuSize,
                                      SelectionBox... selectionBoxes
    ) {
        super(owner, id);
        this.setCursorHand(controller);

        this.radialMenuSize = radialMenuSize;

        this.selectionBoxes = new HashMap<>();
        for (SelectionBox box : selectionBoxes) {
            this.selectionBoxes.put(box.id, box);
        }
        this.disabledBoxes = new ArrayList<>();
    }

    protected abstract void renderRadialImage(GuiGraphics guiGraphics,
                                              float pPartialTicks,
                                              int selectedSlice,
                                              int x,
                                              int y,
                                              int size
    );

    @Override
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        Vec2 cursor = VisorAPI.client().getGuiManager()
                .getCursorHandler()
                .getCursorCoordsInGui(
                        VisorAPI.client().getPlayer()
                                .getPose(PoseType.RENDER)
                                .getController(
                                        getCursorHand()
                                ),
                        getPosition(),
                        getRotation(),
                        overlayScale
                );
        boolean cursorValid;
        if (cursor.x == -1 && cursor.y == -1) {
            cursorValid = false;
        } else {
            cursorValid = true;
            updateMousePosition(true, cursor.x, cursor.y);
        }


        //update selected box if cursor is valid
        if (cursorValid) {
            GuiManager guiManager = VisorAPI.client().getGuiManager();
            //used to find selected slice are based
            // on origin in the center of the menu
            //and without coords bounds
            int specialMouseX = (int) (
                    ((getRawCursorX() - 0.5) * guiManager.getGuiWidth())
                            * (double) this.width / (double) guiManager.getGuiWidth()
            );
            int specialMouseY = (int) (
                    ((getRawCursorY() - 0.5) * guiManager.getGuiHeight())
                            * (double) this.height / (double) guiManager.getGuiHeight()
            );
            int selectedSliceNew = getSliceFromPos(
                    new Vector2f(
                            specialMouseX,
                            specialMouseY
                    )
            );

            if (selectedSliceNew != -1
                    && selectedSliceNew != selectedSlice) {
                VisorAPI.client().getInputManager()
                        .triggerHapticPulse(getCursorHand(),
                                0.001f
                        );
                selectedSlice = selectedSliceNew;
            }
        }
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    public void onRender(GuiGraphics guiGraphics,
                         int mouseX, int mouseY,
                         float pPartialTicks
    ) {


        int startX = guiGraphics.guiWidth() / 2 - radialMenuSize / 2;
        int startY = guiGraphics.guiHeight() / 2 - radialMenuSize / 2;
        renderRadialImage(guiGraphics, pPartialTicks, selectedSlice,
                startX, startY, radialMenuSize
        );


    }


    private int getSliceFromPos(Vector2f mousePosition) {
        for (SelectionBox selectionBox : selectionBoxes.values()) {
            if (selectionBox.isInBox((int) mousePosition.x,
                    (int) mousePosition.y)) {
                if (disabledBoxes.contains(selectionBox.id)) return -1;
                return selectionBox.getId();
            }
        }
        return -1;
    }


    @Override
    public boolean isCursorSupported() {
        return false;
    }

    @Getter
    public abstract static class SelectionBox {

        private final int id;

        public SelectionBox(int id
        ) {
            this.id = id;
        }


        public abstract boolean isInBox(int x, int y);

    }

}
