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
        return inventory.selected;
    }

    public static void setSelectedSlot(Inventory inventory, int slot) {
        inventory.selected = slot;
    }
}
