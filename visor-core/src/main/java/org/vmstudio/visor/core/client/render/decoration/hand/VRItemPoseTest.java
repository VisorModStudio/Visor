package org.vmstudio.visor.core.client.render.decoration.hand;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.PropertyBool;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.PropertyFloat;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayItemPoseTest;
import org.vmstudio.visor.core.client.player.VRClientPlayers;

@RegisterVRItemPose
public class VRItemPoseTest extends VRHandItemPose {
    private static final String ID = "test";

    public VRItemPoseTest(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void applyPose(@NotNull PoseStack stack,
                          @NotNull AbstractClientPlayer player,
                          @NotNull HandType hand,
                          @NotNull ItemStack item,
                          float equipProgress,
                          float partialTicks) {

        var vrPlayer = VRClientPlayers.getPlayer(player);
        if(vrPlayer == null) return;

        PoseParams params = computeParams();

        stack.mulPose(params.preRotation);
        stack.translate(params.offsetX, params.offsetY, params.offsetZ);
        stack.mulPose(params.rotation);
        stack.scale(params.scale, params.scale, params.scale);
    }


    private PoseParams computeParams() {

        Quaternionf preRotation = new Quaternionf();

        Quaternionf rotation = new Quaternionf();

        float scale = 0.8f;

        float preYaw = 0;
        float prePitch = 0;
        float preRoll = 0;

        float translateX = 0;
        float translateY = 0;
        float translateZ = 0;

        float yaw = 0;
        float pitch = 0;
        float roll = 0;


        var options = ClientContext.overlayManager.getOverlay(
                VROverlayItemPoseTest.ID,
                VROverlayItemPoseTest.class
        );
        var properties = options.getProperties();

        scale = properties.getProperty(
                "scale",
                PropertyFloat.class
        ).getValue();

        translateX = properties.getProperty(
                "translate_x",
                PropertyFloat.class
        ).getValue();
        translateY = properties.getProperty(
                "translate_y",
                PropertyFloat.class
        ).getValue();
        translateZ = properties.getProperty(
                "translate_z",
                PropertyFloat.class
        ).getValue();


        preYaw = properties.getProperty("pre_yaw", PropertyFloat.class).getValue();
        prePitch = properties.getProperty("pre_pitch", PropertyFloat.class).getValue();
        preRoll = properties.getProperty("pre_roll", PropertyFloat.class).getValue();

        yaw = properties.getProperty("yaw", PropertyFloat.class).getValue();
        pitch = properties.getProperty("pitch", PropertyFloat.class).getValue();
        roll = properties.getProperty("roll", PropertyFloat.class).getValue();

        preRotation.mul(Axis.ZP.rotationDegrees(preRoll));
        preRotation.mul(Axis.YP.rotationDegrees(prePitch));
        preRotation.mul(Axis.XP.rotationDegrees(preYaw));
        rotation.mul(Axis.ZP.rotationDegrees(roll));
        rotation.mul(Axis.YP.rotationDegrees(pitch));
        rotation.mul(Axis.XP.rotationDegrees(yaw));
        return new PoseParams(preRotation, rotation, translateX, translateY, translateZ, scale);

    }


    @Override
    public boolean canApplyPose(@NotNull AbstractClientPlayer player,
                                @NotNull HandType hand,
                                @NotNull ItemStack itemStack) {
        try {
            var options = ClientContext.overlayManager.getOverlay(
                    VROverlayItemPoseTest.ID,
                    VROverlayItemPoseTest.class
            );
            if(options == null){
                return false;
            }
            var properties = options.getProperties();
            return properties.getProperty(
                    "active",
                    PropertyBool.class
            ).getValue();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.HIGHEST;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    private record PoseParams(Quaternionf preRotation,
                              Quaternionf rotation,
                              float offsetX,
                              float offsetY,
                              float offsetZ,
                              float scale) {}

}
