package org.vmstudio.visor.core.client.tasks.types;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.hotbar.VROverlayHotBar;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


//@TODO why this way it is updated and not directly from overlay?
@RegisterVisorTask
public class TaskHotBar extends VisorTask {

    private static final String ID = "hotbar";
    public static final int NOT_SELECTED = -1;
    public static final int NULL = -2;
    @Getter
    private static TaskHotBar instance;

    @Getter
    private static int slotMain = 0;
    @Getter @Setter
    private static int slotOffhand = NOT_SELECTED;


    private static int slotMainBack = NULL;
    private static int slotOffhandBack = NULL;

    @Setter
    private static boolean resetData = true;


    @Setter
    private boolean inputPressedMain;
    @Setter
    private boolean inputPressedOffhand;

    private boolean pressedMain;
    private boolean pressedOffhand;


    public TaskHotBar(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    public void setOffhandSlot(int slot){
        if(slot == slotOffhand){
            return;
        }
        slotOffhand = slot;
        slotMainBack = NULL;
        slotOffhandBack = NULL;
        if(slotMain == slotOffhand){
            handleSlotCollision(HandType.MAIN, false);
        }
    }

    @Override
    public void onRun(@Nullable LocalPlayer player) {
        var inventory = player.getInventory();
        int slotMainNew = inventory.selected;
        if (resetData || slotMainNew != slotMain) {
            slotMain = slotMainNew;
            resetData = false;
        }

        VROverlayHotBar hotBarOffhand = (VROverlayHotBar)
                ClientContext.overlayManager
                        .getOverlay(VROverlayHotBar.ID_OFFHAND);
        VROverlayHotBar hotBarMainHand = (VROverlayHotBar)
                ClientContext.overlayManager
                        .getOverlay(VROverlayHotBar.ID_MAIN);
        if (hotBarOffhand == null || hotBarMainHand == null) {
            throw new RuntimeException("hotbar overlay offhand or main hand not found");
        }

        //OFFHAND
        if (!ClientContext.decorationRenderer.getHandState(HandType.OFFHAND).isWorldHand()) {
            hotBarOffhand.setEnabled(
                    false
            );

        } else {
            if (inputPressedOffhand && !pressedOffhand) {
                ClientContext.inputManager
                        .triggerHapticPulse(
                                HandType.OFFHAND, 0.002f
                        );
                hotBarOffhand.setEnabled(
                        true
                );
                pressedOffhand = true;

            }

            if (pressedOffhand) {
                slotOffhand = hotBarOffhand.getSelectedSlice();
                if (slotOffhand != NOT_SELECTED) {

                    if (slotMainBack != NULL
                            && slotOffhand != slotMainBack) {
                        //switching back
                        slotMain = slotMainBack;
                        inventory.selected = slotMain;
                        slotMainBack = NULL;
                    } else if (slotOffhand == slotMain) {
                        //switching if collide
                        handleSlotCollision(HandType.MAIN, true);
                    }

                }
                if (!inputPressedOffhand) {
                    ClientContext.inputManager
                            .triggerHapticPulse(
                                    HandType.OFFHAND, 0.003f
                            );
                    hotBarOffhand.setEnabled(
                            false
                    );
                    pressedOffhand = false;
                    slotOffhandBack = NULL;
                    slotMainBack = NULL;
                }
            }
        }
        //MAIN HAND
        if (!ClientContext.decorationRenderer.getHandState(HandType.MAIN).isWorldHand()) {
            hotBarMainHand.setEnabled(
                    false
            );

        } else {
            if (inputPressedMain && !pressedMain) {
                ClientContext.inputManager
                        .triggerHapticPulse(
                                HandType.MAIN, 0.002f
                        );
                hotBarMainHand.setEnabled(
                        true
                );
                pressedMain = true;
            }

            if (pressedMain) {

                slotMain = hotBarMainHand.getSelectedSlice();

                if (slotMain != NOT_SELECTED) {
                    inventory.selected = slotMain;

                    //if selected item in offhand
                    if (slotOffhandBack != NULL
                            && slotMain != slotOffhandBack) {
                        //switching back
                        slotOffhand = slotOffhandBack;
                        slotOffhandBack = NULL;
                    } else if (slotMain == slotOffhand) {
                        //switching if collide
                        handleSlotCollision(HandType.OFFHAND, true);
                    }
                }
                if (!inputPressedMain) {
                    ClientContext.inputManager
                            .triggerHapticPulse(
                                    HandType.MAIN, 0.003f
                            );
                    hotBarMainHand.setEnabled(
                            false
                    );
                    pressedMain = false;
                    slotOffhandBack = NULL;
                    slotMainBack = NULL;

                }
            }
            if (slotOffhand == slotMain) {
                slotOffhand = NOT_SELECTED;
            }
        }
    }
    private void handleSlotCollision(HandType handType, boolean switchableBack){
        var inventory = MC.player.getInventory();
        if(handType == HandType.MAIN) {
            if(switchableBack){
                slotMainBack = slotMain;
            }
            inventory.selected = slotOffhand == 8
                    ? 0
                    : slotOffhand + 1;
            slotMain = inventory.selected;
        }else{
            if(switchableBack) {
                slotOffhandBack = slotOffhand;
            }
            slotOffhand = NOT_SELECTED;
        }
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {
        VROverlayHotBar hotBarOffhand = (VROverlayHotBar)
            ClientContext.overlayManager
                .getOverlay(VROverlayHotBar.ID_OFFHAND);
        VROverlayHotBar hotBarMainHand = (VROverlayHotBar)
            ClientContext.overlayManager
                .getOverlay(VROverlayHotBar.ID_MAIN);

        hotBarMainHand.setEnabled(false);
        hotBarOffhand.setEnabled(false);

        inputPressedMain = false;
        inputPressedOffhand = false;
        pressedMain = false;
        pressedOffhand = false;
        slotMainBack = NULL;
        slotOffhandBack = NULL;
        resetData = true;
    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        if(player == null) return false;
        if(MC.screen != null) return false;
        if(player.isSpectator()) return false;
        return true;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
