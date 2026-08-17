package org.vmstudio.visor.mixin.common.player;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vmstudio.visor.core.common.player.OffhandSlot;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;"
            ),
            require = 1
    )
    private Slot visor$replaceOffhandSlot(Slot original) {
        if (!(original.container instanceof Inventory inventory)
                || original.getContainerSlot() != Inventory.SLOT_OFFHAND) {
            return original;
        }
        return new OffhandSlot(
                inventory.player,
                original.container,
                original.getContainerSlot(),
                original.x,
                original.y
        );
    }
}
