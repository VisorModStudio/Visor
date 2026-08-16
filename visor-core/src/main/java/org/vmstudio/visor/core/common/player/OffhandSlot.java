package org.vmstudio.visor.core.common.player;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;

public class OffhandSlot extends Slot {
    private Player owner;
    public OffhandSlot(Player owner, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.owner = owner;
    }

    //-----COPY FROM VANILLA
    // 1.21.2 made setByPlayer(new, old) the override point; the single-arg form now
    // delegates to it, and vanilla calls the two-arg form directly in places the
    // single-arg override never saw (InventoryMenu#quickMoveStack).
    @Override
    public void setByPlayer(ItemStack newStack, ItemStack oldStack) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            // 1.21.2 turned Equipable into the EQUIPPABLE data component.
            // Vanilla's own offhand slot dropped this check and always notifies;
            // kept here to preserve Visor's behaviour.
            Equippable equippable = newStack.get(DataComponents.EQUIPPABLE);
            if (equippable != null) {
                owner.onEquipItem(EquipmentSlot.OFFHAND, oldStack, newStack);
            }
            super.setByPlayer(newStack, oldStack);
        }

    }

    // 1.21.2: no longer a Pair - the atlas is implied
    @Override
    public ResourceLocation getNoItemIcon() {
        return InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
    }


    //-----DISABLE SLOT
    @Override
    public boolean isActive() {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            return super.isActive();
        }
        return false;
    }

    //-----EXTRA CHECKS FOR SAFETY
    @Override
    public void set(@NotNull ItemStack stack) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            super.set(stack);
        }
    }
    @Override
    public @NotNull ItemStack remove(int amount) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            return super.remove(amount);
        }
        return ItemStack.EMPTY;
    }
    @Override
    public @NotNull ItemStack getItem() {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            return super.getItem();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            return super.mayPlace(stack);
        }
        return false;
    }

    @Override
    public boolean mayPickup(@NotNull Player player) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(owner);
        if (vrPlayer == null || !VRServerSettings.isTwoHandedVR()){
            return super.mayPickup(player);
        }
        return false;
    }
}
