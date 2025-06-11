package me.phoenixra.visor.core.client.tasks.types;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar.HotBarSlice;
import me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar.VROverlayHotBar;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


//@TODO why this way it is updated and not directly from overlay?
@RegisterVisorTask
public class TaskHotBar extends VisorTask {

    private static final String ID = "hotbar";
    @Getter
    private static TaskHotBar instance;

    @Setter
    private boolean inputPressedMain;
    @Setter
    private boolean inputPressedOffhand;

    private boolean pressedMain;
    private boolean pressedOffhand;
    @Getter
    private static HotBarSlice currentStateMain = HotBarSlice.CENTER;
    @Getter @Setter
    private static HotBarSlice currentStateOffhand = HotBarSlice.NOT_SELECTED;


    private static HotBarSlice previousStateMain = null;
    private static HotBarSlice previousStateOffhand = null;

    @Setter
    private static boolean resetData = true;

    public TaskHotBar(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    public void onRun(@Nullable LocalPlayer player) {

        if(resetData
                || player.getInventory().selected
                != currentStateMain.getSlot()){
            currentStateMain = HotBarSlice.fromSlot(
                    player.getInventory().selected
            );
            resetData = false;
        }

        VROverlayHotBar hotBarOffhand = (VROverlayHotBar)
                ClientContext.overlayManager
                .getOverlay(VROverlayHotBar.ID_OFFHAND);
        VROverlayHotBar hotBarMainHand = (VROverlayHotBar)
                ClientContext.overlayManager
                .getOverlay(VROverlayHotBar.ID_MAIN);
        //left
        if (inputPressedOffhand && !pressedOffhand) {
            ClientContext.inputManager
                    .triggerHapticPulse(
                            ControllerHand.OFFHAND, 0.002f
                    );
            hotBarOffhand.setEnabled(
                    true
            );
            pressedOffhand = true;

        }

        if (pressedOffhand) {
            currentStateOffhand = HotBarSlice.fromSlot(
                    hotBarOffhand.getSelectedSlice()
            );
            int slot = currentStateOffhand.getSlot();
            if(slot != -1) {

                //if selected item in main hand
                if(previousStateMain != null
                        && slot != previousStateMain.getSlot()){
                    currentStateMain = previousStateMain;
                    player.getInventory().selected = currentStateMain.getSlot();
                    previousStateMain = null;
                } else if(slot == currentStateMain.getSlot()){
                    previousStateMain = currentStateMain;
                    player.getInventory().selected = slot == 8 ? 0 : slot+1;
                    currentStateMain = HotBarSlice.fromSlot(
                            player.getInventory().selected
                    );
                }

            }
            if(!inputPressedOffhand) {
                ClientContext.inputManager
                        .triggerHapticPulse(
                                ControllerHand.OFFHAND, 0.003f
                        );
                hotBarOffhand.setEnabled(
                        false
                );
                pressedOffhand = false;
                previousStateOffhand = null;
                previousStateMain = null;

                if(previousStateOffhand != currentStateOffhand){


                }
            }
        }

        //right

        if (inputPressedMain && !pressedMain) {
            ClientContext.inputManager
                    .triggerHapticPulse(
                            ControllerHand.MAIN, 0.002f
                    );
            hotBarMainHand.setEnabled(
                    true
            );
            pressedMain = true;
        }

        if (pressedMain) {

            currentStateMain = HotBarSlice.fromSlot(
                    hotBarMainHand.getSelectedSlice()
            );

            int slot = currentStateMain.getSlot();
            if(slot != -1) {
                player.getInventory().selected = slot;

                //if selected item in offhand
                if(previousStateOffhand != null
                        && slot != previousStateOffhand.getSlot()){
                    currentStateOffhand = previousStateOffhand;
                    previousStateOffhand = null;
                }else if(slot == currentStateOffhand.getSlot()){
                    previousStateOffhand = currentStateOffhand;
                    currentStateOffhand = HotBarSlice.NOT_SELECTED;
                }
            }
            if(!inputPressedMain) { //isNotDown
                ClientContext.inputManager
                        .triggerHapticPulse(
                                ControllerHand.MAIN, 0.003f
                        );
                hotBarMainHand.setEnabled(
                        false
                );
                pressedMain = false;
                previousStateOffhand = null;
                previousStateMain = null;

                if(previousStateOffhand != currentStateOffhand){

                }
            }
        }
        if(currentStateOffhand.getSlot() == currentStateMain.getSlot()){
            currentStateOffhand = HotBarSlice.NOT_SELECTED;
        }



    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        if(player == null) return false;
        if(MC.screen != null) return false;
        return true;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }


    public static ItemStack getOffhandItem(){
        if(currentStateOffhand == HotBarSlice.NOT_SELECTED
                || MC.player == null){
            return ItemStack.EMPTY;
        }
        return MC.player.getInventory()
                .getItem(currentStateOffhand.getSlot());
    }

    public static ItemStack getHandItem(ControllerHand hand){
        if(hand == ControllerHand.OFFHAND){
            return getOffhandItem();
        }
        //main
        if(currentStateMain == HotBarSlice.NOT_SELECTED
                || MC.player == null){
            return ItemStack.EMPTY;
        }
        return MC.player.getInventory()
                .getItem(currentStateMain.getSlot());
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
