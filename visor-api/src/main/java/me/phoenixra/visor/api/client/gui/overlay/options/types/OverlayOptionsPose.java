package me.phoenixra.visor.api.client.gui.overlay.options.types;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

//@TODO maybe should not call update in onTick and onRender always? make optional?
@Getter @Setter
public class OverlayOptionsPose extends OverlayOptionGroup<OverlayOptionsPose> {
    public static final String ID = "pose";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);

    @Nullable
    private String formulaPosX;
    private boolean updatablePosX;
    @Nullable
    private String formulaPosY;
    private boolean updatablePosY;
    @Nullable
    private String formulaPosZ;
    private boolean updatablePosZ;

    @Nullable
    private String formulaRotationX;
    private boolean updatableRotationX;
    @Nullable
    private String formulaRotationY;
    private boolean updatableRotationY;
    @Nullable
    private String formulaRotationZ;
    private boolean updatableRotationZ;

    @Nullable
    private String formulaScale;
    private boolean updatableScale;


    @NotNull
    private PoseAnchor positionAnchor;
    @NotNull
    private PoseAnchor rotationAnchor;

    private Vector3f posOffset;

    private Vector3f rotationOffsetVec;

    private float scale;


    private boolean aimRotation;

    private boolean tickModelView;

    public OverlayOptionsPose(@NotNull VROverlay owner,
                              @NotNull Consumer<OverlayOptionsPose> defaultSettings){
        super(owner, defaultSettings);
        if(posOffset==null){
            posOffset = new Vector3f();
        }
        if(rotationOffsetVec==null){
            rotationOffsetVec = new Vector3f();
        }
    }

    @Override
    public void update(boolean force) {

        var configManager = owner.getOptionsConfig().getConfigOwner();
        if(force){
            float x = 0;
            float y = 0;
            float z = 0;

            // ---Position

            try{
                if(formulaPosX != null) {
                    x = (float) Double.parseDouble(formulaPosX);
                }
                updatablePosX = false;
            }catch (NumberFormatException e){
                x = (float) VRMathUtils.getEvaluated(configManager, formulaPosX);
                updatablePosX = true;
            }

            try{
                if(formulaPosY != null) {
                    y = (float) Double.parseDouble(formulaPosY);
                }
                updatablePosY = false;
            }catch (NumberFormatException e){
                y = (float) VRMathUtils.getEvaluated(configManager, formulaPosY);
                updatablePosY = true;
            }

            try{
                if(formulaPosZ != null) {
                    z = (float) Double.parseDouble(formulaPosZ);
                }
                updatablePosZ = false;
            }catch (NumberFormatException e){
                z = (float) VRMathUtils.getEvaluated(configManager, formulaPosZ);
                updatablePosZ = true;
            }
            posOffset = new Vector3f(x, y, z);


            // ---Rotation
            x = 0;
            y = 0;
            z = 0;

            try{
                if(formulaRotationX != null) {
                    x = (float) Double.parseDouble(formulaRotationX);
                }
                updatableRotationX = false;
            }catch (NumberFormatException e){
                x = (float) VRMathUtils.getEvaluated(configManager, formulaRotationX);
                updatableRotationX = true;
            }

            try{
                if(formulaRotationY != null) {
                    y = (float) Double.parseDouble(formulaRotationY);
                }
                updatableRotationY = false;
            }catch (NumberFormatException e){
                y = (float) VRMathUtils.getEvaluated(configManager, formulaRotationY);
                updatableRotationY = true;
            }

            try{
                if(formulaRotationZ != null) {
                    z = (float) Double.parseDouble(formulaRotationZ);
                }
                updatableRotationZ = false;
            }catch (NumberFormatException e){
                z = (float) VRMathUtils.getEvaluated(configManager, formulaRotationZ);
                updatableRotationZ = true;
            }

            rotationOffsetVec = new Vector3f(x,y,z);

            // ---Scale
            scale = 1.0f;
            try{
                if(formulaScale != null) {
                    scale = (float) Double.parseDouble(formulaScale);
                }
                updatableScale = false;
            }catch (NumberFormatException e){

                scale = (float) VRMathUtils.getEvaluated(configManager, formulaScale);
                updatableScale = true;
            }
            scale = scale <= 0 ? 1.0f : scale;
            return;
        }


        float x = posOffset.x;
        float y = posOffset.y;
        float z = posOffset.z;
        boolean updated = false;
        if(updatablePosX) {
            x = (float) VRMathUtils.getEvaluated(configManager, formulaPosX);
            updated = true;
        }
        if(updatablePosY) {
            y = (float) VRMathUtils.getEvaluated(configManager, formulaPosY);
            updated = true;
        }
        if(updatablePosZ) {
            z = (float) VRMathUtils.getEvaluated(configManager, formulaPosZ);
            updated = true;
        }
        if(updated) {
            posOffset = new Vector3f(x, y, z);
        }

        x = rotationOffsetVec.x;
        y = rotationOffsetVec.y;
        z = rotationOffsetVec.z;
        updated = false;
        if(updatableRotationX) {
            x = (float) VRMathUtils.getEvaluated(configManager, formulaRotationX);
            updated = true;
        }
        if(updatableRotationY) {
            y = (float) VRMathUtils.getEvaluated(configManager, formulaRotationY);
            updated = true;
        }
        if(updatableRotationZ) {
            z = (float) VRMathUtils.getEvaluated(configManager, formulaRotationZ);
            updated = true;
        }
        if(updated) {
            rotationOffsetVec = new Vector3f(x,y,z);

        }

        if(updatableScale){
            float overlayScale = (float) VRMathUtils.getEvaluated(configManager, formulaScale);
            overlayScale = overlayScale <= 0 ? 1.0f : overlayScale;
            this.scale = overlayScale;
        }
    }

    @Override
    protected void onLoad(@NotNull Config config){
        var configManager = owner.getOptionsConfig().getConfigOwner();

        tickModelView = config.getBool("tick");

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

        aimRotation = config.getBool("rotation.aim");


        // ---Position
        float x = 0;
        float y = 0;
        float z = 0;

        formulaPosX = config.getStringOrNull("position.offset.x");
        try{
            if(formulaPosX != null) {
                x = (float) Double.parseDouble(formulaPosX);
            }
            updatablePosX = false;
        }catch (NumberFormatException e){
            x = (float) VRMathUtils.getEvaluated(configManager, formulaPosX);
            updatablePosX = true;
        }

        formulaPosY = config.getStringOrNull("position.offset.y");
        try{
            if(formulaPosY != null) {
                y = (float) Double.parseDouble(formulaPosY);
            }
            updatablePosY = false;
        }catch (NumberFormatException e){
            y = (float) VRMathUtils.getEvaluated(configManager, formulaPosY);
            updatablePosY = true;
        }

        formulaPosZ = config.getStringOrNull("position.offset.z");
        try{
            if(formulaPosZ != null) {
                z = (float) Double.parseDouble(formulaPosZ);
            }
            updatablePosZ = false;
        }catch (NumberFormatException e){
            z = (float) VRMathUtils.getEvaluated(configManager, formulaPosZ);
            updatablePosZ = true;
        }

        posOffset = new Vector3f(x,y,z);


        // ---Rotation
        x = 0;
        y = 0;
        z = 0;

        formulaRotationX = config.getStringOrNull("rotation.offset.x");
        try{
            if(formulaRotationX != null) {
                x = (float) Double.parseDouble(formulaRotationX);
            }
            updatableRotationX = false;
        }catch (NumberFormatException e){
            x = (float) VRMathUtils.getEvaluated(configManager, formulaRotationX);
            updatableRotationX = true;
        }

        formulaRotationY = config.getStringOrNull("rotation.offset.y");
        try{
            if(formulaRotationY != null) {
                y = (float) Double.parseDouble(formulaRotationY);
            }
            updatableRotationY = false;
        }catch (NumberFormatException e){
            y = (float) VRMathUtils.getEvaluated(configManager, formulaRotationY);
            updatableRotationY = true;
        }

        formulaRotationZ = config.getStringOrNull("rotation.offset.z");
        try{
            if(formulaRotationZ != null) {
                z = (float) Double.parseDouble(formulaRotationZ);
            }
            updatableRotationZ = false;
        }catch (NumberFormatException e){
            z = (float) VRMathUtils.getEvaluated(configManager, formulaRotationZ);
            updatableRotationZ = true;
        }

        rotationOffsetVec = new Vector3f(x,y,z);


        // ---Scale
        formulaScale = config.getStringOrNull("scale");
        scale = 1.0f;
        try{
            if(formulaScale != null) {
                scale = (float) Double.parseDouble(formulaScale);
            }
            updatableScale = false;
        }catch (NumberFormatException e){

            scale = (float) VRMathUtils.getEvaluated(configManager, formulaScale);
            updatableScale = true;
        }
        scale = scale <= 0 ? 1.0f : scale;
    }

    @Override
    public void onSave(@NotNull Config config){
        config.set("tick", tickModelView);

        config.set("position.type", positionAnchor.name());
        config.set("rotation.type", rotationAnchor.name());

        config.set("rotation.aim", aimRotation);

        config.set("position.offset.x", formulaPosX);
        config.set("position.offset.y", formulaPosY);
        config.set("position.offset.z", formulaPosZ);

        config.set("rotation.offset.x", formulaRotationX);
        config.set("rotation.offset.y", formulaRotationY);
        config.set("rotation.offset.z", formulaRotationZ);

        config.set("scale", formulaScale);
    }

    @Override
    public boolean supportsCopying() {
        return true;
    }

    @Override
    public @NotNull OverlayOptionsScreen<?> getScreen() {
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
