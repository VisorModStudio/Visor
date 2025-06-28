package me.phoenixra.visor.core.client.render;

import com.mojang.math.Axis;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import me.phoenixra.visor.core.client.ClientContext;


public class VRGameCamera extends Camera {

    @Override
    public void setup(@NotNull BlockGetter level,
                      @NotNull Entity entity,
                      boolean thirdPerson,
                      boolean thirdPersonReverse,
                      float partialTicks) {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            super.setup(level, entity, thirdPerson, thirdPersonReverse, partialTicks);
        } else {
            setupVR(level, entity);
        }
    }


    @Override
    public void tick() {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            super.tick();
        }
    }


    @Override
    public boolean isDetached() {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            return super.isDetached();
        }
        return VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON;
    }



    private void setupVR(BlockGetter level, Entity entity) {
        this.initialized = true;
        this.level = level;
        this.entity = entity;

        VRDisplay display = VRRenderState.getCurrentVRDisplay();
        PoseElement eye = ClientContext.player
                .getPose(PoseDataType.RENDER)
                .getElementForDisplay(display);

        // Position
        this.setPosition(new Vec3(
                (Vector3f) RenderPoseHelper.getCameraPosition(
                        display,
                        ClientContext.player.getPose(PoseDataType.RENDER)
                )
        ));

        // Orientation
        this.xRot = -eye.getPitch();
        this.yRot =  eye.getYaw();

        // Look, Up, Left vectors
        var dir = eye.getDirection();
        var upVec = eye.getCustomVector(VRMathUtils.UP_VECTOR);
        var leftVec = eye.getCustomVector(VRMathUtils.RIGHT_VECTOR);

        this.getLookVector().set(dir.x(), dir.y(), dir.z());
        this.getUpVector().set(upVec.x, upVec.y, upVec.z);
        this.getLeftVector().set(leftVec.x, leftVec.y, leftVec.z);

        // Build rotation quaternion: Yaw then Pitch
        this.rotation().identity()
                .mul(Axis.YP.rotationDegrees(-this.yRot))
                .mul(Axis.XP.rotationDegrees( this.xRot));
    }

}
