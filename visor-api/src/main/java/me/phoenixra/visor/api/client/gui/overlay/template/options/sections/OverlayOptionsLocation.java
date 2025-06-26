package me.phoenixra.visor.api.client.gui.overlay.template.options.sections;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsBase;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.OverlayTemplate;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.function.Consumer;

//@TODO maybe should not call update in onTick and onRender always? make optional?
@Getter @Setter
public class OverlayOptionsLocation extends OverlayOptionsBase<OverlayOptionsLocation> {
    public static final String ID = "location";


    private boolean tickModelView;

    @NotNull
    private PoseAnchor positionAnchor;
    @NotNull
    private PoseAnchor rotationAnchor;

    private boolean aimRotation;

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


    private Vector3f posOffset;

    private Vector3f rotationOffsetVec;



    public OverlayOptionsLocation(@NotNull OverlayTemplate owner,
                                  @NotNull Consumer<OverlayOptionsLocation> defaultSettings){
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

        var configManager = owner.getTypeConfig().getConfigOwner();
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
    }

    @Override
    protected void onLoad(@NotNull Config section){
        var configManager = owner.getTypeConfig().getConfigOwner();

        tickModelView = section.getBool("tick");

        positionAnchor =
                PoseAnchor.valueOf(
                        section.getStringOrDefault("position.type","HMD")
                                .toUpperCase()
                );
        rotationAnchor =
                PoseAnchor.valueOf(
                        section.getStringOrDefault("rotation.type","HMD")
                                .toUpperCase()
                );

        aimRotation = section.getBool("rotation.aim");


        // ---Position
        float x = 0;
        float y = 0;
        float z = 0;

        formulaPosX = section.getStringOrNull("position.offset.x");
        try{
            if(formulaPosX != null) {
                x = (float) Double.parseDouble(formulaPosX);
            }
            updatablePosX = false;
        }catch (NumberFormatException e){
            x = (float) VRMathUtils.getEvaluated(configManager, formulaPosX);
            updatablePosX = true;
        }

        formulaPosY = section.getStringOrNull("position.offset.y");
        try{
            if(formulaPosY != null) {
                y = (float) Double.parseDouble(formulaPosY);
            }
            updatablePosY = false;
        }catch (NumberFormatException e){
            y = (float) VRMathUtils.getEvaluated(configManager, formulaPosY);
            updatablePosY = true;
        }

        formulaPosZ = section.getStringOrNull("position.offset.z");
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

        formulaRotationX = section.getStringOrNull("rotation.offset.x");
        try{
            if(formulaRotationX != null) {
                x = (float) Double.parseDouble(formulaRotationX);
            }
            updatableRotationX = false;
        }catch (NumberFormatException e){
            x = (float) VRMathUtils.getEvaluated(configManager, formulaRotationX);
            updatableRotationX = true;
        }

        formulaRotationY = section.getStringOrNull("rotation.offset.y");
        try{
            if(formulaRotationY != null) {
                y = (float) Double.parseDouble(formulaRotationY);
            }
            updatableRotationY = false;
        }catch (NumberFormatException e){
            y = (float) VRMathUtils.getEvaluated(configManager, formulaRotationY);
            updatableRotationY = true;
        }

        formulaRotationZ = section.getStringOrNull("rotation.offset.z");
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


    }

    @Override
    public void onSave(@NotNull Config section){
        section.set("tick", tickModelView);

        section.set("position.type", positionAnchor.name());
        section.set("rotation.type", rotationAnchor.name());

        section.set("rotation.aim", aimRotation);

        section.set("position.offset.x", formulaPosX);
        section.set("position.offset.y", formulaPosY);
        section.set("position.offset.z", formulaPosZ);

        section.set("rotation.offset.x", formulaRotationX);
        section.set("rotation.offset.y", formulaRotationY);
        section.set("rotation.offset.z", formulaRotationZ);
    }

    @Override
    public @NotNull OverlayOptionsScreen<?> getScreen(float mainMenuWidth, float mainMenuHeight) {
        return VisorAPI.client().getGuiManager()
                .getOverlayManager()
                .getOptionsScreenFor(
                this,mainMenuWidth,mainMenuHeight
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("visor.overlaySettings.modelView");
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
