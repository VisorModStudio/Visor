package org.vmstudio.visor.core.common.player;

//? if >=1.21.5 {
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerEquipment;
import net.minecraft.world.item.ItemStack;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;

/**
 * Two-handed VR on 1.21.5
 */
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
//?}
