package org.vmstudio.visor.compatibility.create.addons.aeronautics;

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

public class CreativeStaffItemPose extends VRHandItemPose {
    private static final String ID = "creative_staff_pose";

    public CreativeStaffItemPose(@NotNull VisorAddon owner) { super(owner); }

    @Override
    public void applyPose(@NotNull PoseStack stack,
                          @NotNull AbstractClientPlayer player,
                          @NotNull HandType hand,
                          @NotNull ItemStack item,
                          float equipProgress,
                          float partialTicks) {
        VRClientPlayer vrPlayer = VisorAPI.client().getVRPlayer(player.getUUID());
        if (vrPlayer == null) return;

        float scale = 1f;
        float translateX = 0.0f;
        float translateY = 0.4f;
        float translateZ = -0.3f;
        float yaw = -30f;
        float pitch = 0f;
        float roll =  0f;

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
        return itemStack.getItem().toString().equals("simulated:creative_physics_staff");
    }

    @Override
    public @NotNull ComponentPriority getPriority() { return ComponentPriority.NORMAL; }

    @Override
    public @NotNull String getId() { return ID; }
}
