package org.vmstudio.visor.core.client.render.player;

//? if >=1.21.4 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.core.client.player.VRClientPlayers;

public class VRPlayerRenderState extends PlayerRenderState {
    @Nullable
    public AbstractClientPlayer player;
    public float partialTick;

    public static void extract(PlayerRenderState state, AbstractClientPlayer player, float partialTick) {
        if (!(state instanceof VRPlayerRenderState vrState)) {
            return;
        }
        vrState.player = player;
        vrState.partialTick = partialTick;

        if (!swapsHands(player)) {
            return;
        }
        ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
        //? if >=1.21.5 {
        resolver.updateForLiving(state.rightHandItem, player.getItemHeldByArm(HumanoidArm.LEFT),
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, player);
        resolver.updateForLiving(state.leftHandItem, player.getItemHeldByArm(HumanoidArm.RIGHT),
                ItemDisplayContext.THIRD_PERSON_LEFT_HAND, player);
        //?} else {
        /*resolver.updateForLiving(state.rightHandItem, player.getItemHeldByArm(HumanoidArm.LEFT),
                ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, player);
        resolver.updateForLiving(state.leftHandItem, player.getItemHeldByArm(HumanoidArm.RIGHT),
                ItemDisplayContext.THIRD_PERSON_LEFT_HAND, true, player);
        *///?}
    }

    public static ItemStack heldItemForArm(AbstractClientPlayer player, HumanoidArm arm) {
        return player.getItemHeldByArm(swapsHands(player) ? arm.getOpposite() : arm);
    }

    @Nullable
    public static AbstractClientPlayer playerOf(EntityRenderState state) {
        if (state instanceof VRPlayerRenderState vrState) {
            return vrState.player;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (state instanceof PlayerRenderState playerState && level != null
                && level.getEntity(playerState.id) instanceof AbstractClientPlayer player) {
            return player;
        }
        return null;
    }

    private static boolean swapsHands(AbstractClientPlayer player) {
        VRClientPlayer vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        HumanoidArm mainArm = vrPlayer != null && vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        return mainArm != player.getMainArm();
    }
}
//?} elif >=1.21.2 {
/*import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.core.client.player.VRClientPlayers;

public class VRPlayerRenderState extends PlayerRenderState {
    @Nullable
    public AbstractClientPlayer player;
    public float partialTick;

    public static void extract(PlayerRenderState state, AbstractClientPlayer player, float partialTick) {
        if (!(state instanceof VRPlayerRenderState vrState)) {
            return;
        }
        vrState.player = player;
        vrState.partialTick = partialTick;

        // hand items follow the VR handedness, ItemInHandLayer placed them that way before 1.21.2
        var vrPlayer = VRClientPlayers.getPlayer(player.getUUID());
        HumanoidArm mainArm = vrPlayer != null && vrPlayer.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        if (mainArm != player.getMainArm()) {
            ItemStack rightItem = state.rightHandItem;
            BakedModel rightModel = state.rightHandItemModel;
            state.rightHandItem = state.leftHandItem;
            state.rightHandItemModel = state.leftHandItemModel;
            state.leftHandItem = rightItem;
            state.leftHandItemModel = rightModel;
        }
    }

    @Nullable
    public static AbstractClientPlayer playerOf(EntityRenderState state) {
        if (state instanceof VRPlayerRenderState vrState) {
            return vrState.player;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (state instanceof PlayerRenderState playerState && level != null
                && level.getEntity(playerState.id) instanceof AbstractClientPlayer player) {
            return player;
        }
        return null;
    }
}
*///?}
