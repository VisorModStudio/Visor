package me.phoenixra.visor.api.client.gui.overlay.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.overlay.RegisterOverlayType;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class VROverlayFrameBuffer implements VROverlay {
    @Getter @NotNull
    private final String id;
    @Getter @NotNull
    private final VisorAddon owner;


    @Getter
    protected RenderTarget renderTarget;

    @Getter
    protected final OverlayCursorData activeCursorData = new OverlayCursorData();
    @Getter
    protected final OverlayCursorData inactiveCursorData = new OverlayCursorData();


    @Getter @Setter
    private Vec3 position = new Vec3(0.0D, 0.0D, 0.0D);
    @Setter
    private Matrix4f rotation = new Matrix4f();


    @Getter @Setter
    protected float overlayScale = 1.0f;


    @Getter @Setter
    private ElementPriority priority = ElementPriority.NORMAL;

    @Getter
    private boolean enabled = false;
    @Getter
    private boolean visible = false;

    @Getter
    private final String overlayType;

    @Getter
    private final String displayName;

    @Getter
    private final ConfigFile config;
    private final HashMap<Class<? extends OverlayOptionCategory>, OverlayOptionCategory> options;

    private final OverlayOptionsGlobal optionsGlobal;
    private final OverlayOptionsModelView optionsModelView;
    private boolean initializedModelView;


    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id,
                                @Nullable RenderTarget renderTarget) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);

        this.owner = owner;
        this.id = id;
        this.renderTarget = renderTarget;

        try {
            this.config = VisorAPI.client()
                    .getGuiManager()
                    .getOverlayManager()
                    .getOverlayCatalog()
                    .getConfigOrCreate(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        RegisterOverlayType overlayTypeAnnotation = getClass().getAnnotation(
                RegisterOverlayType.class
        );
        if(overlayTypeAnnotation != null){
            overlayType = overlayTypeAnnotation.id();
            config.set("type", overlayType);
            try {
                config.save();
            }catch (Exception e){
                //empty
            }
        }else{
            overlayType = null;
        }

        options = new HashMap<>();
        List<OverlayOptionCategory> preOptions = createOptions();
        preOptions.forEach(it->{
            options.put(it.getClass(),it);
        });
        optionsGlobal = (OverlayOptionsGlobal) options.get(OverlayOptionsGlobal.class);
        optionsModelView = (OverlayOptionsModelView) options.get(OverlayOptionsModelView.class);

        displayName = config.getStringOrDefault("displayName", id);
    }

    /**
     * Highly suggested to make updates for a frameBuffer here,
     * to have overlay image properly updated
     * (otherwise you may cause delay by 1 render tick)
     * @param partialTicks p
     */
    public abstract void onRender(float partialTicks);
    protected abstract void onTick();
    protected abstract boolean updateVisibility();

    @NotNull
    protected abstract List<OverlayOptionCategory> createOptions();

    public void render(float partialTick){
        if(optionsGlobal != null && optionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.FRAME) {
            options.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }
        onRender(partialTick);
    }

    @Override
    public final void tick(){
        if(optionsGlobal != null && optionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.TICK) {
            options.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }
        if(optionsModelView != null
                && optionsModelView.isTickModelView()){
            applyModelView(1);
        }

        visible = enabled && updateVisibility() && renderTarget != null;

        onTick();
    }

    @Override
    public void applyModelView(float partialTick) {
        if(optionsModelView == null) return;
        if(!initializedModelView || optionsModelView.isTickModelView()) {
            PoseData renderPose = VisorAPI.client().getPlayer().getPose(PoseType.RENDER);
            position = optionsModelView.getPositionAnchor().anchorPos(
                    renderPose,
                    optionsModelView.getPosOffset()
            );
            if(optionsModelView.isAimRotation()){
                rotation = optionsModelView.getRotationAnchor().anchorRotationAim(
                        renderPose,
                        optionsModelView.getRotationOffsetVec(),
                        position
                );
            }else {
                rotation = optionsModelView.getRotationAnchor().anchorRotation(
                        renderPose,
                        optionsModelView.getRotationOffsetVec()
                );
            }
            initializedModelView = true;
        }
    }



    @Override
    public void setEnabled(boolean flag) {
        if(flag == enabled) return;

        this.enabled = flag;
        if(enabled){
            onEnable();
        }else{
            initializedModelView = false;
            onDisable();
        }
    }

    @Override
    public int getMouseX() {
        return activeCursorData.mouseX;
    }
    @Override
    public int getMouseY() {
        return activeCursorData.mouseY;
    }

    @Override
    public float getRawCursorX() {
        return activeCursorData.rawCursorX;
    }

    @Override
    public float getRawCursorY() {
        return activeCursorData.rawCursorY;
    }


    @Override
    public void updateMousePosition(boolean activeCursorHand, float rawX, float rawY) {

    }

    @Override
    public @NotNull Matrix4fc getRotation() {
        return rotation;
    }

    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int buttonType) {
        return false;
    }

    @Override
    public void mouseMoved(double x, double y) {

    }

    @Override
    public boolean mouseDragged(double d, double e, int i, double f, double g) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return false;
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
    @Override
    public boolean keyPressed(int keyCode, int keyScan, int modifiers) {
        return false;
    }

    @Override
    public boolean isCursorSupported() {
        return false;
    }

    @Override
    public @Nullable <T extends OverlayOptionCategory> T getOptionCategory(@NotNull Class<T> type) {
        return (T) options.get(type);
    }

    @Override
    public Collection<OverlayOptionCategory> getOptionsList() {
        return options.values();
    }
}
