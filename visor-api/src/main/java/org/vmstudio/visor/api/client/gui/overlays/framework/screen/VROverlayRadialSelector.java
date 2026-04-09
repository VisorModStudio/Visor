package org.vmstudio.visor.api.client.gui.overlays.framework.screen;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The {@link VROverlayScreen}, that has selection boxes
 */
public abstract class VROverlayRadialSelector extends VROverlayScreen {
    protected final int radialMenuSize;

    @Getter
    protected HandType usedHand;

    protected HashMap<Integer, SelectionBox> selectionBoxes;
    @Getter
    protected List<Integer> disabledBoxes;


    @Getter
    @Setter
    private int selectedSlice = -1;

    protected VROverlayRadialSelector(@NotNull VisorAddon owner,
                                      @NotNull HandType hand,
                                      @NotNull String id,
                                      @NotNull ComponentPriority priority,
                                      int radialMenuSize,
                                      SelectionBox... selectionBoxes
    ) {
        super(owner, id, priority, 1.0f);
        this.usedHand = hand;

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
    public void onPreRender(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {
        Vector2f cursor = VisorAPI.client().getGuiManager()
                .getCursorHandler()
                .findCursorPosition2D(
                        VisorAPI.client().getVRLocalPlayer()
                                .getPoseData(PlayerPoseType.RENDER)
                                .getHand(
                                        usedHand
                                ),
                        getPose().getPosition(),
                        getPose().getRotation(),
                        getPose().getScale(),
                        getAspectRatio()
                );
        boolean cursorValid;
        if (cursor.x == -1 && cursor.y == -1) {
            cursorValid = false;
        } else {
            cursorValid = true;
            updateCursorData(true, cursor.x, cursor.y);
        }


        //update selected box if cursor is valid
        if (cursorValid) {
            //used to find selected slice are based
            // on origin in the center of the menu
            //and without coords bounds
            int specialMouseX = (int) (
                    ((getRawMouseX() - 0.5) * width)
            );
            int specialMouseY = (int) (
                    ((getRawMouseY() - 0.5) * height)
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
                        .triggerHapticPulse(usedHand,
                                0.0005f
                        );
                selectedSlice = selectedSliceNew;
            }
        }
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
    public boolean supportsCursor() {
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
