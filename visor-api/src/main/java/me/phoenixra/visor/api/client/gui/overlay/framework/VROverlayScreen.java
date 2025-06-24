package me.phoenixra.visor.api.client.gui.overlay.framework;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.gui.VRGuiManager;
import me.phoenixra.visor.api.client.gui.VROverlayManager;
import me.phoenixra.visor.api.client.gui.overlay.RegisterOverlayType;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.options.sections.OverlayOptionsModelView;
import me.phoenixra.visor.api.client.input.InputHelper;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public abstract class VROverlayScreen extends Screen implements VROverlay {
    @Getter @NotNull
    private final String id;
    @Getter @NotNull
    private final VisorAddon owner;


    @Getter
    private final OverlayCursorData activeCursorData = new OverlayCursorData();
    @Getter
    private final OverlayCursorData inactiveCursorData = new OverlayCursorData();

    @Getter @Setter
    private RenderTarget renderTarget;

    @Getter @Setter
    private Vector3fc position = new Vector3f(0f, 0f, 0f);
    @Setter
    private Matrix4f rotation = new Matrix4f();


    @Getter @Setter
    protected float overlayScale = 1.0f;


    @Getter @Setter
    private ElementPriority priority = ElementPriority.NORMAL;

    @Getter
    private boolean enabled = false;
    private boolean visible;



    @Getter
    private final String displayName;

    //screen edges to consider valid for cursor
    @Getter
    protected int mouseEdgeX = -1;
    @Getter
    protected int mouseEdgeY = -1;
    @Getter
    protected int mouseEdgeWidth = -1;
    @Getter
    protected int mouseEdgeHeight = -1;

    protected boolean initAgain;


    @Getter
    private final String overlayType;

    @Getter
    private final ConfigFile config;
    protected final HashMap<Class<? extends OverlayOptionCategory>, OverlayOptionCategory> options;

    protected final OverlayOptionsGlobal optionsGlobal;
    protected final OverlayOptionsModelView optionsModelView;
    private boolean initializedModelView;


    public VROverlayScreen(@NotNull VisorAddon owner,
                           @NotNull String id) {
        super(Component.literal(id));
        Objects.requireNonNull(owner);
        Objects.requireNonNull(id);

        this.owner = owner;
        this.id = id;
        minecraft = Minecraft.getInstance();

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
            config.set("type",overlayType);
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

    protected abstract void onRender(GuiGraphics guiGraphics,
                                     int mouseX, int mouseY,
                                     float partialTicks);
    protected abstract void onTick();
    protected abstract boolean updateVisibility();
    @NotNull
    protected abstract List<OverlayOptionCategory> createOptions();

    @Override
    public void render(GuiGraphics guiGraphics,
                       int pMouseX, int pMouseY,
                       float partialTicks
    ) {
        if(initAgain){
            init();
            initAgain = false;
        }
        if(optionsGlobal != null && optionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.FRAME) {
            options.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }

        super.render(guiGraphics,pMouseX,pMouseY,partialTicks);
        onRender(
                guiGraphics,
                pMouseX, pMouseY,
                partialTicks
        );
    }
    @Override
    public final void tick(){
        if(optionsGlobal != null
                && optionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.TICK) {
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

        visible = enabled && updateVisibility();
        VisorAPI.client().getRenderer().updateOverlayTarget(
                this
        );
        //making sure there is a render target to draw on
        visible = visible && renderTarget != null;

        onTick();
    }

    @Override
    public void applyModelView(float partialTick) {
        if(optionsModelView == null) return;
        if(optionsModelView.getMovingDemoAnchor() != null) {
            //silly way to have same position with demo when its moving
            //@TODO find a better approach
            VROverlayHelper.applyModelView(
                    this,
                    optionsModelView.getMovingDemoAnchor(),
                    optionsModelView.getMovingDemoAnchor(),
                    false,
                    new Vector3f(0,0,-0.3f),
                    new Vector3f(0,0,0)
            );
            return;
        }
        if(!initializedModelView || optionsModelView.isTickModelView()) {
            PoseData renderPose = VisorAPI.client().getPlayer()
                    .getPose(PoseType.RENDER);
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
    public void updateMousePosition(boolean activeCursorHand, float rawX, float rawY) {
        if (!enabled) return;
        if(rawX == -1 && rawY == -1){
            OverlayCursorData cursorData = activeCursorHand ? activeCursorData : inactiveCursorData;

            cursorData.rawCursorX = 0;
            cursorData.rawCursorY = 0;
            cursorData.cursorInGuiX = 0;
            cursorData.cursorInGuiY = 0;

            cursorData.mouseX = 0;
            cursorData.mouseY = 0;
            mouseMoved(cursorData.mouseX, cursorData.mouseY);
            return;
        }
        VRGuiManager guiManager = VisorAPI.client().getGuiManager();
        float cursorInGuiX;
        float cursorInGuiY;
        if (rawX >= 0.0F && rawY >= 0.0F
                && rawX <= 1.0F && rawY <= 1.0F) {
            cursorInGuiX = (float) (
                    (int) (rawX * guiManager.getScaledGuiWidth())
            );
            cursorInGuiY = (float) (
                    (int) (rawY * guiManager.getScaledGuiHeight())
            );
        } else {
            cursorInGuiX = (float) (
                    (int) (Math.min(1.0f, Math.max(rawX, 0.0f)) * guiManager.getScaledGuiWidth())
            );
            cursorInGuiY = (float) (
                    (int) (Math.min(1.0f, Math.max(rawY, 0.0f)) * guiManager.getScaledGuiHeight())
            );
        }

        OverlayCursorData cursorData = activeCursorHand ? activeCursorData : inactiveCursorData;

        cursorData.rawCursorX = rawX;
        cursorData.rawCursorY = rawY;
        cursorData.cursorInGuiX = cursorInGuiX;
        cursorData.cursorInGuiY = cursorInGuiY;

        int oldMouseX = cursorData.mouseX;
        int oldMouseY = cursorData.mouseY;
        cursorData.mouseX = (int)(cursorData.cursorInGuiX * (double) this.width / (double) guiManager.getScaledGuiWidth());
        cursorData.mouseY = (int)(cursorData.cursorInGuiY * (double) this.height / (double) guiManager.getScaledGuiHeight());

       mouseMoved(cursorData.mouseX, cursorData.mouseY);

       if(InputHelper.canDragMouse()) {
           for (int button : new int[]{0, 1, 2}) {
               if (!InputHelper.isMousePressed(button)) continue;
               int deltaX = cursorData.mouseX - oldMouseX;
               int deltaY = cursorData.mouseY - oldMouseY;
               mouseDragged(
                       cursorData.mouseX, cursorData.mouseY,
                       button,
                       deltaX, deltaY
               );
               break;
           }
       }

    }

    @Override
    public boolean isCursorWithinBounds(boolean activeCursorHand, float rawX, float rawY) {
        if(rawX < 0f
                || rawX > 1f
                || rawY < 0f
                || rawY > 1f) return false;

        if(mouseEdgeX == -1
                || mouseEdgeY == -1
                || mouseEdgeWidth == -1
                || mouseEdgeHeight == -1) return true;


        OverlayCursorData cursorData = activeCursorHand ? activeCursorData : inactiveCursorData;

        float oldRawX = cursorData.cursorInGuiX;
        float oldRawY = cursorData.cursorInGuiY;
        int oldMouseX = cursorData.mouseX;
        int oldMouseY = cursorData.mouseY;

        updateMousePosition(activeCursorHand, rawX, rawY);
        boolean result = cursorData.mouseX >= mouseEdgeX
                && cursorData.mouseY >= mouseEdgeY
                && cursorData.mouseX <= mouseEdgeWidth +mouseEdgeX
                && cursorData.mouseY <= mouseEdgeHeight +mouseEdgeY;

        cursorData.cursorInGuiX = oldRawX;
        cursorData.cursorInGuiY = oldRawY;
        cursorData.mouseX = oldMouseX;
        cursorData.mouseY = oldMouseY;
        return result;
    }

    @Override
    public void setEnabled(boolean flag) {
        if(flag == enabled) return;
        VRGuiManager guiManager = VisorAPI.client().getGuiManager();

        if (flag) {
            enabled = true;
            init(
                    Minecraft.getInstance(),
                    guiManager.getScaledGuiWidth(),
                    guiManager.getScaledGuiHeight()
            );
            onEnable();
        } else {
            enabled = false;
            initializedModelView = false;
            VROverlayManager overlayHandler = VisorAPI.client()
                    .getGuiManager()
                    .getOverlayManager();
            if(overlayHandler.getKeyboardAttachedTo() == this){
                overlayHandler.showKeyboard(false);
            }
            onDisable();
        }
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics) {
        //empty
    }

    public boolean isVisible() {
        return visible && enabled;
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
    public @NotNull Matrix4fc getRotation() {
        return rotation;
    }


    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        return super.mouseClicked(x, y, buttonType);
    }

    @Override
    public boolean mouseReleased(double x, double y, int buttonType) {
        return super.mouseReleased(x, y, buttonType);
    }

    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY,
                                int button,
                                double deltaX, double deltaY) {
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
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
    public @Nullable <T extends OverlayOptionCategory> T getOptionCategory(@NotNull Class<T> type) {

        return (T) options.get(type);
    }

    @Override
    public Collection<OverlayOptionCategory> getOptionsList() {
        return options.values();
    }

}
