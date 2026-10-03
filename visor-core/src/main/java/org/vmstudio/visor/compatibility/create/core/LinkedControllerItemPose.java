package org.vmstudio.visor.compatibility.create.core;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;

public class LinkedControllerItemPose extends VRHandItemPose {
    public static final String ID = "create_linked_controller";

    public LinkedControllerItemPose(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void applyPose(@NotNull PoseStack stack,
                          @NotNull AbstractClientPlayer player,
                          @NotNull HandType hand,
                          @NotNull ItemStack item,
                          float equipProgress,
                          float partialTicks) {
        VRClientPlayer vrPlayer = VisorAPI.client().getVRPlayer(player.getUUID());
        if (vrPlayer == null) return;

        int handDir = hand == HandType.MAIN ? 1 : -1;

        float scale = 1.0f;
        float translateX = handDir * -0.1325f;
        float translateY = -0.2f;
        float translateZ = 0.1f;

        float yaw = -30f;
        float pitch = 0f;
        float roll = 0f;

        Quaternionf rotation = new Quaternionf();
        rotation.mul(Axis.ZP.rotationDegrees(roll));
        rotation.mul(Axis.YP.rotationDegrees(pitch));
        rotation.mul(Axis.XP.rotationDegrees(yaw));

        stack.translate(translateX, translateY, translateZ);
        McRenderUtils.rotate(stack, rotation);
        stack.scale(scale, scale, scale);
    }

    @Override
    public boolean canApplyPose(@NotNull AbstractClientPlayer player,
                                @NotNull HandType hand,
                                @NotNull ItemStack itemStack) {
        return itemStack.getItem().toString().equals("create:linked_controller");
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.NORMAL;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
