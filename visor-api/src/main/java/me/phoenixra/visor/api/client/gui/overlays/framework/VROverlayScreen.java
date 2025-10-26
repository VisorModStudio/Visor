package me.phoenixra.visor.api.client.gui.overlays.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.*;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.*;

/**
 * {@link VROverlay} that is rendered
 * as a minecraft {@link Screen}
 */
public abstract class VROverlayScreen extends Screen implements VROverlay {

    @Getter @NotNull
    private final String id;

    @Getter @NotNull
    private final VisorAddon owner;

    @Getter
    private final ElementPriority priority;

    @Getter
    private final VROverlayPose pose;

    @Getter @Setter
    private @Nullable PoseAnchor forcedAnchor;


    @Getter @Setter
    private RenderTarget renderTarget;




    protected final Map<String, OverlayOptionGroup<?>> optionsMap;

    @Getter
    private final @NotNull Collection<OverlayOptionGroup<?>> options;

    @Getter
    protected final ConfigFile optionsConfig;


    @Getter
    private final VROverlayCursorData activeCursorData = new VROverlayCursorData();
    @Getter
    private final VROverlayCursorData inactiveCursorData = new VROverlayCursorData();


    @Getter
    protected int guiScaleFactor = 0;

    @Getter
    protected int cursorBoundsX = -1;
    @Getter
    protected int cursorBoundsY = -1;
    @Getter
    protected int cursorBoundsWidth = -1;
    @Getter
    protected int cursorBoundsHeight = -1;


    @Getter
    private boolean enabled = false;

    private boolean visible;

    protected boolean initAgain;


    private static long mouseDragDelay;



    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id) {
        this(owner, id, ElementPriority.NORMAL,1.0f);
    }

    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id,
                           @NotNull ElementPriority priority,
                           float overlayScale) {
        super(Component.literal(id));
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);
        Objects.requireNonNull(priority);
        if(overlayScale <=0){
            throw new RuntimeException("overlayScale cannot be less or equal '0'");
        }

        this.owner = owner;
        this.id = id;
        this.priority = priority;
        this.pose = new VROverlayPose(this, overlayScale);

        this.minecraft = Minecraft.getInstance();

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

    protected void onTick() {};


    protected void onPreRender(GuiGraphics guiGraphics,
                               int mouseX, int mouseY,
                               float partialTicks) {}

    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {}

    protected abstract void onUpdatePose(float partialTicks);


    protected abstract boolean updateVisibility();

    protected void onEnable() {};

    protected void onDisable() {};

    public int getRequestedWidth(){
        return VisorAPI.client().getGuiManager().getGuiWidth();
    }
    public int getRequestedHeight(){
        return VisorAPI.client().getGuiManager().getGuiHeight();
    }
    public final int getRequestedWidthScaled(){
        return Mth.ceil(getRequestedWidth() / (float) guiScaleFactor);
    }
    public final int getRequestedHeightScaled(){
        return Mth.ceil(getRequestedHeight() / (float) guiScaleFactor);
    }

    /**
     * Create options for overlay.
     * If no options required, return empty list.
     *
     * <p>
     *     If method returns non-empty list, the optionsConfig is created
     * </p>
     * <p>
     *     If overlay is the {@link VROverlayTemplate},
     *     the method always return non-empty list
     * </p>
     * @return options list
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
    public final void tick() {
        onPreTick();
        visible = enabled && updateVisibility();
        VisorAPI.client().getRenderer().updateOverlayTarget(
                this
        );
        //making sure there is a render target to draw on
        visible = visible && renderTarget != null;
        onTick();
    }


    @Override
    public final void render(@NotNull GuiGraphics guiGraphics,
                             int pMouseX, int pMouseY,
                             float partialTicks
    ) {
        if (initAgain) {
            init();
            initAgain = false;
        }
        if(supportsVisibilityUpdateOnRender()) {
            visible = enabled && updateVisibility();
            VisorAPI.client().getRenderer().updateOverlayTarget(
                    this
            );
            //making sure there is a render target to draw on
            visible = visible && renderTarget != null;
            if(!visible) return;
        }

        onPreRender(
                guiGraphics,
                pMouseX, pMouseY,
                partialTicks
        );

        super.render(guiGraphics, pMouseX, pMouseY, partialTicks);

        onRender(
                guiGraphics,
                pMouseX, pMouseY,
                partialTicks
        );
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
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        //empty
    }

    @Override
    public void setEnabled(boolean flag) {
        if (flag == enabled) return;
        if (flag) {
            enabled = true;
            updateSize();
            onEnable();
        } else {
            enabled = false;
            visible = false;
            var keyboardAccessor = VisorAPI.client().getGuiManager()
                    .getOverlayManager()
                    .getKeyboardAccessor();
            if (keyboardAccessor.getAttachedTo() == this) {
                keyboardAccessor.setVisible(false);
            }
            onDisable();
        }
    }

    public boolean canDragMouse(){
        return mouseDragDelay < System.currentTimeMillis();
    }
    public void startDragMouse(){
        mouseDragDelay = System.currentTimeMillis() + 100L;
    }
    public void finishDragMouse(){
        mouseDragDelay = Long.MAX_VALUE;
    }
    public void updateSize(){
        guiScaleFactor = VisorAPI.client().getGuiManager().calculateScale(
                0,
                getRequestedWidth(),
                getRequestedHeight()
        );
        init(
                Minecraft.getInstance(),
                getRequestedWidthScaled(),
                getRequestedHeightScaled()
        );
    }

    @Override
    public void updateCursorData(boolean activeCursor, float rawX, float rawY) {
        if (!enabled) return;
        if (rawX < 0f || rawX > 1f
                || rawY < 0f || rawY > 1f) {
             return;
        }

        // ---- Preparing
        VROverlayCursorData cursorData = activeCursor ? activeCursorData : inactiveCursorData;


        int oldMouseX = cursorData.getCursorX();
        int oldMouseY = cursorData.getCursorY();

        // ---- Updating mouse data
        cursorData.setRawCursorX(rawX);
        cursorData.setRawCursorY(rawY);


        cursorData.setCursorX(
                (int) (rawX * (double) this.width)
        );
        cursorData.setCursorY(
                (int) (rawY * (double) this.height)
        );

        if(!activeCursor){
            return;
        }

        // ---- Move and Drag events
        mouseMoved(cursorData.getCursorX(), cursorData.getCursorY());

        if (canDragMouse()) {
            int deltaX = cursorData.getCursorX() - oldMouseX;
            int deltaY = cursorData.getCursorY() - oldMouseY;
            mouseDragged(
                    cursorData.getCursorX(), cursorData.getCursorY(),
                    0,
                    deltaX, deltaY
            );
        }

    }




    public boolean isVisible() {
        return visible && enabled;
    }

    @Override
    public @Nullable OverlayOptionGroup<?> getOption(@NotNull String id) {
        return optionsMap.get(id);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        return super.mouseClicked(mouseX, mouseY, buttonType);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        return super.mouseReleased(mouseX, mouseY, buttonType);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
    }
    @Override
    public boolean mouseDragged(double mouseX, double mouseY,
                                int buttonType,
                                double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, buttonType, deltaX, deltaY);
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        return super.keyReleased(i, j, k);
    }
    @Override
    public boolean charTyped(char chr, int modifiers) {
        return super.charTyped(chr, modifiers);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}
