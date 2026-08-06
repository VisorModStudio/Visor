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
import java.util.ArrayList;
import java.util.List;

@Getter
public class PlayerPoseServerImpl implements PlayerPoseServer {
    private VRServerPlayerImpl player;

    protected final VRPoseImpl hmd;
    protected final VRPoseImpl mainHand;
    protected final VRPoseImpl offhand;

    protected final ServerTrackersPose trackers;

    protected final ServerHandsPose hands;

    private final List<VRPose> elements;

    private final boolean roomRelative;

    private Vector3fc origin;
    private float rotationY;


    private float bodyYaw;
    private Vector3fc headPivot;

    public PlayerPoseServerImpl(@NotNull VRServerPlayerImpl player,
                                boolean roomRelative) {
        this.player = player;
        this.roomRelative = roomRelative;

        this.hmd = new VRPoseImpl();

        this.mainHand = new VRPoseImpl();
        this.offhand = new VRPoseImpl();

        this.trackers = new ServerTrackersPose(this);
        this.hands = new ServerHandsPose(this);

        origin = VRMathUtils.ZERO_VECTOR;
        headPivot = VRMathUtils.ZERO_VECTOR;

        elements = new ArrayList<>(List.of(
                hmd,
                mainHand, offhand
        ));

    }

    public void resetPoseElements(){
        elements.clear();
        elements.addAll(
                List.of(
                        hmd,
                        mainHand, offhand
                )
        );
        elements.addAll(trackers.getActiveTrackersPose());
        elements.addAll(hands.getActiveJointsPose());
    }


    public void update(PoseDataBuffer poseData,
                       Vector3fc origin){
        this.origin = origin;

        float turnRotationY = player.getRotationY();
        this.rotationY = roomRelative ? 0.0f : turnRotationY;

        var hmdPose = poseData.hmd();
        var mainHandPose = poseData.mainHand();
        var offhandPose = poseData.offhand();

        Vector3f hmdPos = hmdPose.position()
                .rotateY(-turnRotationY, new Vector3f());
        Matrix4f hmdRotation = new Matrix4f().rotationY(-turnRotationY)
                .mul(hmdPose.orientation().get(new Matrix4f()));
        Vector3f hmdDir = hmdPose.orientation()
                .transform(VRMathUtils.BACK_VECTOR, new Vector3f())
                .rotateY(-turnRotationY);

        Vector3f mainHandPos = mainHandPose.position()
                .rotateY(-turnRotationY, new Vector3f());
        Matrix4f mainHandRotation = new Matrix4f().rotationY(-turnRotationY)
                .mul(mainHandPose.orientation().get(new Matrix4f()));
        Vector3f mainHandDir = mainHandPose.orientation()
                .transform(VRMathUtils.BACK_VECTOR, new Vector3f())
                .rotateY(-turnRotationY);

        Vector3f offhandPos = offhandPose.position()
                .rotateY(-turnRotationY, new Vector3f());
        Matrix4f offhandRotation = new Matrix4f().rotationY(-turnRotationY)
                .mul(offhandPose.orientation().get(new Matrix4f()));
        Vector3f offhandDir = offhandPose.orientation()
                .transform(VRMathUtils.BACK_VECTOR, new Vector3f())
                .rotateY(-turnRotationY);

        this.hmd.update(
                hmdPos,
                hmdRotation,
                hmdDir,
                this.origin,
                this.rotationY,
                1.0f
        );

        this.mainHand.update(
                mainHandPos,
                mainHandRotation,
                mainHandDir,
                this.origin,
                this.rotationY,
                1.0f
        );
        this.offhand.update(
                offhandPos,
                offhandRotation,
                offhandDir,
                this.origin,
                this.rotationY,
                1.0f
        );

        this.bodyYaw = calcBodyYaw();
        this.headPivot = calcHeadPivot();
    }

    public void copyFrom(PlayerPoseServerImpl other){

        this.origin = new Vector3f(other.origin);
        this.rotationY = other.rotationY;
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
        return rotationY;
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
