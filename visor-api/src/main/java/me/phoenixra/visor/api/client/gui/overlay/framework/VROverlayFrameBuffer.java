package me.phoenixra.visor.api.client.gui.overlay.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayCursorData;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayPose;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
    protected RenderTarget renderTarget;

    @Getter
    protected final VROverlayCursorData activeCursorData = new VROverlayCursorData();
    @Getter
    protected final VROverlayCursorData inactiveCursorData = new VROverlayCursorData();


    @Getter
    private final VROverlayPose pose;


    @Getter
    private final ElementPriority priority;

    @Getter
    private boolean enabled = false;
    @Getter
    private boolean visible = false;


    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id){
        this(owner, id, ElementPriority.NORMAL, null, 1.0f);
    }

    public VROverlayFrameBuffer(@NotNull VisorAddon owner,
                                @NotNull String id,
                                @NotNull ElementPriority priority,
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

        this.pose = new VROverlayPose(overlayScale);

    }


    protected void onPreTick() {}

    protected void onTick() {}


    protected void onPreRender(float partialTicks) {}

    protected void onRender(float partialTicks) {}


    protected abstract boolean updateVisibility();

    protected void onEnable() {}

    protected void onDisable() {}

    @Override
    public final void tick(){
        onPreTick();


        visible = enabled && updateVisibility() && renderTarget != null;

        onTick();
    }

    public void render(float partialTick){
        onPreRender(partialTick);
        onRender(partialTick);
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
    public boolean supportsCursor() {
        return false;
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
