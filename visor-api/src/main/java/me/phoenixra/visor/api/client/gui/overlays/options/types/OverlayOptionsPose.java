package me.phoenixra.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;

//@TODO maybe should not call update in onTick and onRender always? make optional?
@Getter
public class OverlayOptionsPose extends OverlayOptionGroup<OverlayOptionsPose> {
    public static final String ID = "pose";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    private PoseAnchor positionAnchor;
    private PoseAnchor rotationAnchor;

    private Vector3f positionOffset;

    private Vector3f rotationOffset;

    private float scale;


    private boolean aimedRotation;

    private boolean tickPose;

    public OverlayOptionsPose(@NotNull VROverlay owner,
                              @NotNull Consumer<OverlayOptionsPose> defaultSettings){
        super(owner, defaultSettings);
        positionOffset = new Vector3f();
        rotationOffset = new Vector3f();
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
        rotationOffset = new Vector3f(
                config.getFloatOrDefault("rotation.offset.x", 0),
                config.getFloatOrDefault("rotation.offset.y", 0),
                config.getFloatOrDefault("rotation.offset.z", 0)
        );


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

        config.set("rotation.offset.x", rotationOffset.x);
        config.set("rotation.offset.y", rotationOffset.y);
        config.set("rotation.offset.z", rotationOffset.z);

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

    public void setRotationOffset(Vector3f newValue) {
        if(this.rotationOffset == newValue){
            return;
        }
        this.rotationOffset = newValue;
        onChanged();
    }
    public void setRotationOffset(float radianX, float radianY, float radianZ) {
        if(this.rotationOffset.x == radianX
                && this.rotationOffset.y == radianY
                && this.rotationOffset.z == radianZ){
            return;
        }
        this.rotationOffset = new Vector3f(radianX,radianY,radianZ);
        onChanged();
    }
    public void setRotationOffsetX(float radianX) {
        if(this.rotationOffset.x == radianX){
            return;
        }
        this.rotationOffset.x = radianX;
        onChanged();
    }
    public void setRotationOffsetY(float radianY) {
        if(this.rotationOffset.y == radianY){
            return;
        }
        this.rotationOffset.y = radianY;
        onChanged();
    }
    public void setRotationOffsetZ(float radianZ) {
        if(this.rotationOffset.z == radianZ){
            return;
        }
        this.rotationOffset.z = radianZ;
        onChanged();
    }

    public void setScale(float newValue) {
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
