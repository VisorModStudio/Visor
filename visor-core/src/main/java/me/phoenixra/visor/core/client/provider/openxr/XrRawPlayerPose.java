package me.phoenixra.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.input.device.VRDeviceHMD;
import me.phoenixra.atumvr.core.input.device.OpenXRDeviceHMD;
import me.phoenixra.visor.core.client.data.raw.RawPlayerPose;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import me.phoenixra.visor.core.client.ClientContext;

public class XrRawPlayerPose extends RawPlayerPose {
    private final XrVRProvider provider;
    public XrRawPlayerPose(XrVRProvider provider){
        this.provider = provider;
    }

    @Override
    public void updatePose() {
        var hmdDevice = provider.getInputHandler().getDevice(
                VRDeviceHMD.ID, OpenXRDeviceHMD.class
        );
        hmdData.getRotationMutable().set(hmdDevice.getPose().matrix());
        hmdData.getLeftEyePoseMutable()
                .set(hmdDevice.getEyePose(EyeType.LEFT).matrix());
        hmdData.getRightEyePoseMutable()
                .set(hmdDevice.getEyePose(EyeType.RIGHT).matrix());


        Matrix4f hmdRotation = hmdData.getRotationMutable();
        Matrix4f hmdPose = hmdData.getDevicePoseMutable();
        hmdRotation.set3x3(hmdPose);

        Vec3 centerEyePos = hmdData.getCenterEyePosition();
        hmdData.getPositionHistory().add(centerEyePos);
        Vector3f vector3 = hmdData.getRotation()
                .transformDirection(new Vector3f(0.0F, -0.1F, 0.1F));
        hmdData.getPivotHistory()
                .add(new Vec3(
                                (double) vector3.x() + centerEyePos.x,
                                (double) vector3.y() + centerEyePos.y,
                                (double) vector3.z() + centerEyePos.z
                        )
                );
        hmdData.getRotationHistory()
                .add(new Quaternionf().setFromNormalized(hmdRotation)
                        .rotateY(ClientContext.player.getRotationYaw()));

    }
}
