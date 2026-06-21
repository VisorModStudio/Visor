package org.vmstudio.visor.core.common.player;

import lombok.Setter;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;

import java.util.List;

public class OffhandNonNullList extends NonNullList<ItemStack> {
    public Player player;

    @Setter
    private boolean useVanilla;

    public OffhandNonNullList(Player player,
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