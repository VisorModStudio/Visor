package org.vmstudio.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import java.util.function.Consumer;

@Getter
public class OverlayOptionsPose extends OverlayOptionGroup<OverlayOptionsPose> {
    public static final String ID = "pose";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    private PoseAnchor positionAnchor;
    private PoseAnchor rotationAnchor;

    private Vector3f positionOffset;

    private Quaternionf rotationOffset;

    private float scale;

    private boolean aimedRotation;

    private boolean tickPose;

    public OverlayOptionsPose(@NotNull VROverlay owner,
                              @NotNull Consumer<OverlayOptionsPose> defaultSettings){
        super(owner, defaultSettings);
        positionOffset = new Vector3f();
        rotationOffset = new Quaternionf();
    }


    @Override
    public void update(boolean force) {
    }

    @Override
    protected void onLoad(@NotNull Config config){

        tickPose = config.getBool("tick");
        aimedRotation = config.getBool("rotation.aim");

        positionAnchor =
                PoseAnchor.valueOf(
                        config.getStringOrDefault("position.type","HMD")
                                .toUpperCase()
                );
        rotationAnchor =
                PoseAnchor.valueOf(
                        config.getStringOrDefault("rotation.type","HMD")
                                .toUpperCase()
                );


        // ---Position
        positionOffset = new Vector3f(
                config.getFloatOrDefault("position.offset.x", 0),
                config.getFloatOrDefault("position.offset.y", 0),
                config.getFloatOrDefault("position.offset.z", 0)
        );


        // ---Rotation
        if (config.hasPath("rotation.quaternion.w")) {
            rotationOffset = new Quaternionf(
                    config.getFloatOrDefault("rotation.quaternion.x", 0),
                    config.getFloatOrDefault("rotation.quaternion.y", 0),
                    config.getFloatOrDefault("rotation.quaternion.z", 0),
                    config.getFloatOrDefault("rotation.quaternion.w", 1)
            ).normalize();
        } else {
            //@TODO compatibility with old settings. [Remove in 0.5.0]
            float ex = config.getFloatOrDefault("rotation.offset.x", 0);
            float ey = config.getFloatOrDefault("rotation.offset.y", 0);
            float ez = config.getFloatOrDefault("rotation.offset.z", 0);
            rotationOffset = new Quaternionf().rotationZYX(ez, ey, ex);
        }


        // ---Scale
        scale = config.getFloatOrDefault("scale", 1.0f);
        scale = scale <= 0 ? 1.0f : scale;
    }

    @Override
    public void onSave(@NotNull Config config){
        config.set("tick", tickPose);

        config.set("position.type", positionAnchor.name());
        config.set("rotation.type", rotationAnchor.name());

        config.set("rotation.aim", aimedRotation);

        config.set("position.offset.x", positionOffset.x);
        config.set("position.offset.y", positionOffset.y);
        config.set("position.offset.z", positionOffset.z);

        config.set("rotation.quaternion.x", rotationOffset.x);
        config.set("rotation.quaternion.y", rotationOffset.y);
        config.set("rotation.quaternion.z", rotationOffset.z);
        config.set("rotation.quaternion.w", rotationOffset.w);

        config.set("scale", scale);
    }


    public void setPositionAnchor(PoseAnchor newValue) {
        if(this.positionAnchor == newValue){
            return;
        }
        this.positionAnchor = newValue;
        onChanged();
    }

    public void setRotationAnchor(PoseAnchor newValue) {
        if(this.rotationAnchor == newValue){
            return;
        }
        this.rotationAnchor = newValue;
        onChanged();
    }

    public void setTickPose(boolean newValue) {
        if(this.tickPose == newValue){
            return;
        }
        this.tickPose = newValue;
        onChanged();
    }

    public void setAimedRotation(boolean newValue) {
        if(this.aimedRotation == newValue){
            return;
        }
        this.aimedRotation = newValue;
        onChanged();
    }

    public void setPositionOffset(Vector3f newValue) {
        if(this.positionOffset == newValue){
            return;
        }
        this.positionOffset = newValue;
        onChanged();
    }
    public void setPositionOffset(float x, float y, float z) {
        if(this.positionOffset.x == x
                && this.positionOffset.y == y
                && this.positionOffset.z == z){
            return;
        }
        this.positionOffset = new Vector3f(x,y,z);
        onChanged();
    }
    public void setPositionOffsetX(float x) {
        if(this.positionOffset.x == x){
            return;
        }
        this.positionOffset.x = x;
        onChanged();
    }
    public void setPositionOffsetY(float y) {
        if(this.positionOffset.y == y){
            return;
        }
        this.positionOffset.y = y;
        onChanged();
    }
    public void setPositionOffsetZ(float z) {
        if(this.positionOffset.z == z){
            return;
        }
        this.positionOffset.z = z;
        onChanged();
    }

    public void setRotationOffset(Quaternionfc newValue) {
        if (this.rotationOffset.equals(newValue)) {
            return;
        }
        this.rotationOffset = new Quaternionf(newValue).normalize();
        onChanged();
    }

    public void setRotationOffset(Matrix4fc rotationMatrix) {
        setRotationOffset(rotationMatrix.getNormalizedRotation(new Quaternionf()));
    }
    public void setRotationOffset(Vector3f euler) {
        setRotationOffset(euler.x, euler.y, euler.z);
    }
    public void setRotationOffset(float radianX, float radianY, float radianZ) {
        setRotationOffset(new Quaternionf().rotationZYX(radianZ, radianY, radianX));
    }
    public void rotateLocalX(float radians) {
        if (radians == 0f) return;
        this.rotationOffset = new Quaternionf(rotationOffset).rotateX(radians).normalize();
        onChanged();
    }
    public void rotateLocalY(float radians) {
        if (radians == 0f) return;
        this.rotationOffset = new Quaternionf(rotationOffset).rotateY(radians).normalize();
        onChanged();
    }
    public void rotateLocalZ(float radians) {
        if (radians == 0f) return;
        this.rotationOffset = new Quaternionf(rotationOffset).rotateZ(radians).normalize();
        onChanged();
    }


    public void setScale(float newValue) {
        if (!Float.isFinite(newValue) || newValue <= 0f) {
            return;
        }
        if(this.scale == newValue){
            return;
        }
        this.scale = newValue;
        onChanged();
    }

    private void onChanged(){
        changesNotSaved = true;
    }
    @Override
    public boolean supportsCopying() {
        return true;
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getOptionsScreenFor(
                        this
                );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}