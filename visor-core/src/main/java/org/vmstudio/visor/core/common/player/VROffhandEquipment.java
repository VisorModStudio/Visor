package org.vmstudio.visor.core.common.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
//? if >=1.21.5 {
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerEquipment;
//?} else {
/*import lombok.Setter;
import net.minecraft.core.NonNullList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
*///?}

/**
 * Two-handed VR: the offhand is the hotbar slot held in the other hand.
 * The player's equipment since 1.21.5, the inventory's offhand list before.
 */
//? if >=1.21.5 {
public class VROffhandEquipment extends PlayerEquipment {
    private final Player player;

    public VROffhandEquipment(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public ItemStack get(EquipmentSlot slot) {
        VRPlayer vrPlayer = redirecting(slot);
        if (vrPlayer == null) {
            return super.get(slot);
        }
        int hotbarSlot = vrPlayer.getOffhandSlot();
        return hotbarSlot < 0 ? ItemStack.EMPTY : player.getInventory().getItem(hotbarSlot);
    }

    @Override
    public ItemStack set(EquipmentSlot slot, ItemStack stack) {
        VRPlayer vrPlayer = redirecting(slot);
        if (vrPlayer == null) {
            return super.set(slot, stack);
        }
        int hotbarSlot = vrPlayer.getOffhandSlot();
        return hotbarSlot < 0 ? ItemStack.EMPTY : player.getInventory().getNonEquipmentItems().set(hotbarSlot, stack);
    }

    private VRPlayer redirecting(EquipmentSlot slot) {
        if (slot != EquipmentSlot.OFFHAND || !VRServerSettings.isTwoHandedVR()) {
            return null;
        }
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        return vrPlayer == null || vrPlayer.isRemote() ? null : vrPlayer;
    }
}
//?} else {
/*public class VROffhandEquipment extends NonNullList<ItemStack> {
    public Player player;

    @Setter
    private boolean useVanilla;

    public VROffhandEquipment(Player player,
                              List<ItemStack> list,
                              @Nullable ItemStack object
    ) {
        super(list, object);
        this.player = player;
    }

    @NotNull
    @Override
    public ItemStack get(int i) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if(vrPlayer == null || useVanilla
                || !VRServerSettings.isTwoHandedVR()
                || vrPlayer.isRemote()){
            return super.get(i);
        }
        if (vrPlayer.getOffhandSlot() < 0) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().getItem(vrPlayer.getOffhandSlot());

    }


    @Override
    public @NotNull ItemStack set(int i, @NotNull ItemStack itemStack) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if (vrPlayer == null || useVanilla || !VRServerSettings.isTwoHandedVR() || vrPlayer.isRemote()){
            return super.set(i, itemStack);
        }

        int slot = vrPlayer.getOffhandSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().items.set(slot, itemStack);
    }

    @Override
    public void add(int i, ItemStack object) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if(vrPlayer == null || useVanilla
                || !VRServerSettings.isTwoHandedVR()
                || vrPlayer.isRemote()){
            super.add(i, object);
        }
    }

    @Override
    public ItemStack remove(int i) {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if(vrPlayer == null || useVanilla
                || !VRServerSettings.isTwoHandedVR()
                || vrPlayer.isRemote()){
            return super.remove(i);
        }

        int slot = vrPlayer.getOffhandSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        }
        return player.getInventory().items.set(slot, ItemStack.EMPTY);
    }

    @Override
    public void clear() {
        VRPlayer vrPlayer = VisorAPI.getVRPlayer(player);
        if(vrPlayer == null || useVanilla
                || !VRServerSettings.isTwoHandedVR()
                || vrPlayer.isRemote()){
            super.clear();
        }
    }
}
*///?}
