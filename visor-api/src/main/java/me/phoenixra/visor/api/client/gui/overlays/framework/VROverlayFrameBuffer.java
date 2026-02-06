package me.phoenixra.visor.api.client.gui.overlays.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.player.pose.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.*;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.common.addon.component.ComponentPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;

/**
 * {@link VROverlay} that renders
 * frame buffer from specified {@link RenderTarget}
 */
public abstract class VROverlayFrameBuffer implements VROverlay {
    @Getter @NotNull
    private final String id;
    @Getter @NotNull
    private final VisorAddon owner;

    @Getter
    private final ComponentPriority priority;

    @Getter
    private final VROverlayPose pose;

    @Getter @Setter
    private @Nullable PoseAnchor forcedAnchor;


    @Getter
    protected RenderTarget renderTarget;



    protected final Map<String, OverlayOptionGroup<?>> optionsMap;

    @Getter
    private final @NotNull Collection<OverlayOptionGroup<?>> options;

    @Getter
    protected final ConfigFile optionsConfig;



    @Getter
    protected final VROverlayCursorData activeCursorData = new VROverlayCursorData();
    @Getter
    protected final VROverlayCursorData inactiveCursorData = new VROverlayCursorData();




    @Getter
    private boolean enabled = false;
    @Getter
    private boolean visible = false;


    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id){
        this(owner, id, ComponentPriority.NORMAL, null, 1.0f);
    }

    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id,
                                @NotNull ComponentPriority priority,
                                @Nullable RenderTarget renderTarget,
                                float overlayScale) {
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);
        Objects.requireNonNull(priority);
        if(overlayScale <=0){
            throw new RuntimeException("overlayScale cannot be less or equal '0'");
        }

        this.owner = owner;
        this.id = id;
        this.renderTarget = renderTarget;
        this.priority = priority;

        this.pose = new VROverlayPose(this, overlayScale);

        optionsMap = new LinkedHashMap<>();
        List<OverlayOptionGroup<?>> preOptions = createOptions();
        preOptions.forEach(it->{
            optionsMap.put(it.getId(),it);
        });
        options = Collections.unmodifiableCollection(optionsMap.values());

        if(!optionsMap.isEmpty()){
            try {
                this.optionsConfig = VisorAPI.client()
                        .getGuiManager()
                        .getOverlayManager()
                        .getOverlayConfigAccessor()
                        .getConfigOrCreate(this);
                initOptions();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else{
            this.optionsConfig = null;
        }

    }


    protected void onPreTick() {}

    protected void onTick() {}


    protected void onPreRender(float partialTicks) {}

    protected void onRender(float partialTicks) {}

    protected abstract void onUpdatePose(float partialTicks);


    protected abstract boolean updateVisibility();

    protected void onEnable() {}

    protected void onDisable() {}

    /**
     * Create options for overlay.
     * If no options required, return empty list.
     *
     * <p>
     *     If method returns non-empty list, the optionsConfig is created
     * </p>
     * <p>If overlay is a {@link VROverlayTemplate}, the method has to return non-empty list</p>
     * @return options
     */
    @NotNull
    protected List<OverlayOptionGroup<?>> createOptions() {
        return List.of();
    }

    protected void initOptions(){
        for(var option : options){
            option.init();
        }
    }

    @Override
    public final void tick(){
        onPreTick();
        visible = enabled && updateVisibility() && renderTarget != null;
        onTick();
    }

    public void render(float partialTick){
        if(supportsVisibilityUpdateOnRender()) {
            visible = enabled && updateVisibility() && renderTarget != null;
            if(!visible) return;
        }
        onPreRender(partialTick);
        onRender(partialTick);
    }

    @Override
    public final void updatePose(float partialTicks) {
        if(forcedAnchor != null) {
            VROverlayHelper.applyPose(
                    this,
                    forcedAnchor,
                    forcedAnchor,
                    getPose().getScale(),
                    false,
                    new Vector3f(0,0,-0.3f),
                    new Vector3f(0,0,0)
            );
            return;
        }
        onUpdatePose(partialTicks);
    }



    @Override
    public void setEnabled(boolean flag) {
        if(flag == enabled) return;

        this.enabled = flag;
        if(enabled){
            onEnable();
        }else{
            visible = false;
            onDisable();
        }
    }

    @Override
    public @Nullable OverlayOptionGroup<?> getOption(@NotNull String id) {
        return optionsMap.get(id);
    }

    @Override
    public boolean supportsCursor() {
        return false;
    }


    @Override
    public int getWidth() {
        return renderTarget != null ? renderTarget.width : 1;
    }

    @Override
    public int getHeight() {
        return renderTarget != null ? renderTarget.height : 1;
    }

    @Override
    public void updateCursorData(boolean activeCursor, float rawX, float rawY) {

    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {

    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int buttonType, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int keyScan, int modifiers) {
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



}
