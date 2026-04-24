package org.vmstudio.visor.core.server.player;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.server.player.PlayerPoseServer;
import org.vmstudio.visor.core.common.player.VRPoseImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.*;

import java.lang.Math;
import java.util.List;

@Getter
public class PlayerPoseServerImpl implements PlayerPoseServer {
    private VRServerPlayerImpl player;

    protected final VRPoseImpl hmd;
    protected final VRPoseImpl mainHand;
    protected final VRPoseImpl offhand;

    private final List<VRPoseImpl> elements;


    private Vector3fc origin;


    private float bodyYaw;
    private Vector3fc headPivot;

    public PlayerPoseServerImpl(@NotNull VRServerPlayerImpl player) {
        this.player = player;

        this.hmd = new VRPoseImpl();

        this.mainHand = new VRPoseImpl();
        this.offhand = new VRPoseImpl();

        origin = VRMathUtils.ZERO_VECTOR;
        headPivot = VRMathUtils.ZERO_VECTOR;

        elements = List.of(
                hmd,
                mainHand, offhand
        );

    }

    public void update(PoseDataBuffer poseData,
                       Vector3fc origin){
        this.origin = origin;

        var hmdPose = poseData.hmd();
        var mainHandPose = poseData.mainHand();
        var offhandPose = poseData.offhand();

        Vector3f hmdDir = hmdPose
                .orientation().transform(VRMathUtils.BACK_VECTOR, new Vector3f());
        Vector3f mainHandDir = mainHandPose
                .orientation().transform(VRMathUtils.BACK_VECTOR, new Vector3f());
        Vector3f offhandDir = offhandPose
                .orientation().transform(VRMathUtils.BACK_VECTOR, new Vector3f());

        this.hmd.update(
                hmdPose.position(),
                hmdPose.orientation().get(new Matrix4f()),
                hmdDir,
                this.origin,
                0,
                1.0f
        );

        this.mainHand.update(
                mainHandPose.position(),
                mainHandPose.orientation().get(new Matrix4f()),
                mainHandDir,
                this.origin,
                0,
                1.0f
        );
        this.offhand.update(
                offhandPose.position(),
                offhandPose.orientation().get(new Matrix4f()),
                offhandDir,
                this.origin,
                0,
                1.0f
        );

        this.bodyYaw = calcBodyYaw();
        this.headPivot = calcHeadPivot();
    }

    public void copyFrom(PlayerPoseServerImpl other){

        this.origin = new Vector3f(other.origin);
        this.bodyYaw = other.bodyYaw;
        this.headPivot = new Vector3f(other.headPivot);

        hmd.copyFrom(other.hmd);
        mainHand.copyFrom(other.mainHand);
        offhand.copyFrom(other.offhand);
    }

    @Override
    public Player getMcPlayer() {
        return null;
    }

    @Override
    public float getWorldScale() {
        return 1.0f;
    }


    @Override
    public float getRotationY() {
        return 0;
    }

    public void resetOrigin(Vector3fc newOrigin){
        this.origin = newOrigin;
        elements.forEach(
                it->it.onOriginChanged(this.origin)
        );
    }



    private float calcBodyYaw() {
        Vector3f bodyPos = this.offhand.getPosition()
                .sub(this.mainHand.getPosition(), new Vector3f())
                .normalize()
                .rotateY((-(float) Math.PI / 2F));
        var hmdDirection = this.hmd.getDirection();

        if (bodyPos.dot(hmdDirection) < 0.0D) {
            bodyPos = bodyPos.mul(-1);
        }

        bodyPos = hmdDirection.lerp(bodyPos, 0.7f, new Vector3f());
        return (float) Mth.atan2(-bodyPos.x, bodyPos.z);
    }

    private Vector3f calcHeadPivot() {
        var hmdPosition = this.hmd.getPosition();
        Vector3f transform = this.hmd.getRotation()
                .transformPosition(
                        new Vector3f(
                                0.0F,
                                -0.1F,
                                0.1F
                        )
                );
        return new Vector3f(
                transform.x() + hmdPosition.x(),
                transform.y() + hmdPosition.y(),
                transform.z() + hmdPosition.z()
        );
    }

    @Override
    public VRPose getActiveHand() {
        return player.getActiveHand() == HandType.MAIN ? mainHand : offhand;
    }


    @Override
    public String toString() {
        return String.format(
                "VRClientPose:%n" +
                        "  Origin             : %s%n" +
                        "  HMD                : %s%n" +
                        "  Main Hand          : %s%n" +
                        "  Offhand            : %s%n",
                origin,
                hmd,
                mainHand,
                offhand
        );
    }

}
