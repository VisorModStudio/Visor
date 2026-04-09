package org.vmstudio.visor.mixin.common.player;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.core.common.player.OffhandSlot;

@Mixin(InventoryMenu.class)
public class InventoryMenuMixin {

    @ModifyArg(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/inventory/InventoryMenu;addSlot(Lnet/minecraft/world/inventory/Slot;)Lnet/minecraft/world/inventory/Slot;",
                    ordinal = 5
            )
    )
    private Slot visor$replaceOffhandSlot(Slot original) {
        return new OffhandSlot(
                ((Inventory) original.container).player,
                original.container,
                original.getContainerSlot(),
                original.x,
                original.y
        );
    }
}
