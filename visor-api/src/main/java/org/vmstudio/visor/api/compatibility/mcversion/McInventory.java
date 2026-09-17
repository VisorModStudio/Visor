package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.world.entity.player.Inventory;

/**
 * Cross-mc-version facade over the player inventory selection
 */
public class McInventory {
    private McInventory() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static int selectedSlot(Inventory inventory) {
        //? if >=1.21.5 {
        return inventory.getSelectedSlot();
        //?} else {
        /*return inventory.selected;
        *///?}
    }

    public static void setSelectedSlot(Inventory inventory, int slot) {
        //? if >=1.21.5 {
        inventory.setSelectedSlot(slot);
        //?} else {
        /*inventory.selected = slot;
        *///?}
    }
}
