package me.phoenixra.visor.core.server.player;

import lombok.Getter;
import me.phoenixra.visor.api.common.player.PoseElement;
import me.phoenixra.visor.api.common.network.buffer.PoseDataBuffer;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.api.server.player.PlayerPoseServer;
import me.phoenixra.visor.core.client.player.pose.RemotePlayerPose;
import me.phoenixra.visor.core.common.player.PoseElementImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.joml.*;

import java.lang.Math;
import java.util.List;

@Getter
public class PlayerPoseServerImpl implements PlayerPoseServer {

    protected final PoseElementImpl hmd;
    protected final PoseElementImpl mainHand;
    protected final PoseElementImpl offhand;

    private final List<PoseElementImpl> elements;


    private PoseDataBuffer buffer;


    private Vector3fc origin;


    private float bodyYaw;
    private Vector3fc headPivot;

    public PlayerPoseServerImpl() {

        this.hmd = new PoseElementImpl();

        this.mainHand = new PoseElementImpl();
        this.offhand = new PoseElementImpl();


        elements = List.of(
                hmd,
                mainHand, offhand
        );

    }

    public void update(PoseDataBuffer poseData,
                       Vector3fc origin){
        this.origin = origin;
        this.buffer = poseData;

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
                this.origin,
                0,
                1.0f,
                hmdPose.position(),
                hmdPose.orientation().get(new Matrix4f()),
                hmdDir
        );

        this.mainHand.update(
                this.origin,
                0,
                1.0f,
                mainHandPose.position(),
                mainHandPose.orientation().get(new Matrix4f()),
                mainHandDir
        );
        this.offhand.update(
                this.origin,
                0,
                1.0f,
                offhandPose.position(),
                offhandPose.orientation().get(new Matrix4f()),
                offhandDir
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
    public PoseElement getActiveHand() {
        return mainHand;
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
