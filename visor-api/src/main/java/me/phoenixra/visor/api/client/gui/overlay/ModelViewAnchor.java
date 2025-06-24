package me.phoenixra.visor.api.client.gui.overlay;

import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.common.ControllerHand;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Math;
import java.util.function.Function;

public enum ModelViewAnchor {

    NONE,

    HMD((renderPose)-> new AnchoredPosition(
            renderPose.getHmd()
    )),

    MAIN_HAND((renderPose)-> new AnchoredPosition(
            renderPose.getController(ControllerHand.MAIN)
    )),

    OFFHAND((renderPose)-> new AnchoredPosition(
            renderPose.getController(ControllerHand.OFFHAND)
    ));


    @Getter
    private final Function<PoseData, AnchoredPosition> supplier;

    ModelViewAnchor(Function<PoseData, AnchoredPosition> supplier){
        this.supplier = supplier;
    }
    ModelViewAnchor(){
        this.supplier = null;
    };


    public AnchoredPosition getAnchor(@NotNull PoseData renderPose){
        if(supplier == null) return null;
        return this.supplier.apply(renderPose);
    }

    public Component getName(){
        return Component.translatable("visor.enums.ModelViewAnchor."+name());
    }

    public Vector3f anchorPos(@NotNull PoseData renderPose,
                              Vector3f offset){
        AnchoredPosition pose = getAnchor(renderPose);

        float worldScale = renderPose.getWorldScale();
        offset = new Vector3f(
                offset.x * worldScale,
                offset.y * worldScale,
                offset.z * worldScale
        );
        if(pose == null){
            return new Vector3f(offset.x, offset.y, offset.z);
        }

        return pose.component
                .getCustomVector(offset)
                .add(pose.position);

    }
    public Matrix4f anchorRotation(@NotNull PoseData renderPose,
                                   @NotNull Vector3fc offset){
        AnchoredPosition pose = getAnchor(renderPose);
        if(pose == null){
            return new Matrix4f();
        }

        PoseElement poseElement = pose.component;

        Matrix4f overlayRot = poseElement
                .getRotation()
                .mul(
                        new Matrix4f().rotationZ(offset.z()),
                        new Matrix4f()
                );
        overlayRot.mul(new Matrix4f().rotationY(offset.y()));
        overlayRot.mul(new Matrix4f().rotationX(offset.x()));

        return overlayRot;
    }
    public Matrix4f anchorRotationAim(@NotNull PoseData renderPose,
                                      @NotNull Vector3fc offset,
                                      @NotNull Vector3fc objPosition){
        AnchoredPosition pose = getAnchor(renderPose);
        if(pose == null){
            return new Matrix4f();
        }

        Vector3fc elementPos = pose.position;
        Vector3f directionToTarget = new Vector3f(
                objPosition.x() - elementPos.x(),
                objPosition.y() - elementPos.y(),
                objPosition.z() - elementPos.z()
        );
        float rotationX = (float) Math.asin(
                directionToTarget.y() / directionToTarget.length()
        );
        float rotationY = (float) (
                (double) (float) Math.PI +
                        Mth.atan2(
                                directionToTarget.x(),
                                directionToTarget.z()
                        )
        );
        Matrix4f rotation = new Matrix4f().rotationZ(offset.z());
        rotation.mul(new Matrix4f().rotationY(rotationY + offset.y()));
        rotation.mul(new Matrix4f().rotationX(rotationX + offset.x()));

        return rotation;
    }


    public Vector3f reverseAnchorRotation(Matrix4fc componentRotation,
                                          Matrix4fc overlayRot) {

        Matrix4f invController = componentRotation.invert(new Matrix4f());


        Matrix4f matrix4f = invController.mul(overlayRot);

        float offsetY = (float) Math.asin(-matrix4f.m20());
        float offsetZ = (float) Mth.atan2(matrix4f.m10(), matrix4f.m00());
        float offsetX = (float) Mth.atan2(matrix4f.m21(), matrix4f.m22());

        return new Vector3f(offsetX, offsetY, offsetZ);
    }

    @Getter
    public static class AnchoredPosition {
        private final PoseElement component;
        private final Vector3fc position;
        private final Matrix4f rotationMatrix;

        public AnchoredPosition(PoseElement playerComponent) {

            this.position = playerComponent.getPosition();
            this.rotationMatrix = new Matrix4f(playerComponent.getRotation());
            this.component = playerComponent;
        }

    }

}
