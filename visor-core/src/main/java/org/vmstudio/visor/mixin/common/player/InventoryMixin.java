// #!MC-VERSION:: 1.21.5+
package org.vmstudio.visor.mixin.common.player;

import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable {
    // since 1.21.5 the offhand redirect lives in VROffhandEquipment, installed by Common_PlayerMixin
}
