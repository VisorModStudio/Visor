package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButton;
import me.phoenixra.visor.api.client.gui.widgets.ImageButton;
import me.phoenixra.visor.api.client.gui.widgets.sets.FilterListBinaryWidgetSet;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets.CreateOverlayWidgetSet;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.widgets.OverlaysWidgetSet;
import me.phoenixra.visor.api.client.gui.widgets.sets.WidgetSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class VROverlaySettings extends VROverlayScreen
        implements VREventListener {
    public static final String ID = "settings";

    public static final AtumColor TEXT_COLOR = AtumColor.WHITE.blend(AtumColor.BLACK, 0.2f);

    public static final Component TEXT_FIND = Component.translatable("visor.overlay.options.find");

    private static final ResourceLocation BACKGROUND_OVERLAYS = new ResourceLocation(
            "visor:textures/gui/overlays/settings/background_1.png"
    );
    private static final ResourceLocation BACKGROUND_CREATE = new ResourceLocation(
            "visor:textures/gui/overlays/settings/background_2.png"
    );

    private static final ResourceLocation BACKGROUND_EXTRA = new ResourceLocation(
            "visor:textures/gui/overlays/settings/background_extra_1.png"
    );
    private static final ResourceLocation BACKGROUND_EXTRA_EXTENDED = new ResourceLocation(
            "visor:textures/gui/overlays/settings/background_extra_2.png"
    );

    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 256;


    private static final Component TITLE_CREATE_OVERLAY
            = Component.translatable("visor.overlay.options.main.create_overlay");
    private static final Component TITLE_OVERLAYS
            = Component.translatable("visor.overlay.options.main.overlays");


    private final Vector3f posOffset = new Vector3f(0, 0, -0.75f);
    private final Vector3f rotationOffset = new Vector3f(0, 0, 0);

    private Vector3fc roomPosition = null;
    private Matrix4f roomRotation = null;

    @Getter
    private SettingsTab settingsTab = SettingsTab.OVERLAYS;
    @Getter
    private WidgetSet widgetSet;

    @Getter
    @Setter
    private boolean backgroundExtended = false;


    @Getter
    private int menuEdgeX, menuEdgeY, menuEdgeWidth, menuEdgeHeight;

    @Getter
    @Setter
    private int cursorEdgeOffsetX, cursorEdgeOffsetY,
            cursorEdgeOffsetWidth, cursorEdgeOffsetHeight;

    private ImageButton tabButton;
    private ImageButton closeButton;
    private ImageButton dragButton;


    private final OverlaysWidgetSet overlaysWidgetSet;
    private final CreateOverlayWidgetSet createOverlayWidgetSet;

    public VROverlaySettings(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id, ElementPriority.NORMAL, 0.55f);
        VisorAPI.eventBus().registerListener(owner, this);
        overlaysWidgetSet = new OverlaysWidgetSet(
                this, this::repopulateWidgets
        );
        createOverlayWidgetSet = new CreateOverlayWidgetSet(
                this, this::repopulateWidgets
        );
    }

    public enum SettingsTab {
        OVERLAYS,
        CREATE_OVERLAY;

        public WidgetSet widgetSet(VROverlaySettings settings) {
            return this == OVERLAYS
                    ? settings.overlaysWidgetSet
                    : settings.createOverlayWidgetSet;
        }

        private ResourceLocation background() {
            return this == OVERLAYS ? BACKGROUND_OVERLAYS : BACKGROUND_CREATE;
        }

        private ResourceLocation backgroundExtra(VROverlaySettings settings) {
            return this == OVERLAYS ? BACKGROUND_EXTRA
                    : settings.isBackgroundExtended()
                    ? BACKGROUND_EXTRA_EXTENDED
                    : BACKGROUND_EXTRA;
        }

        private void changeTab(VROverlaySettings settings) {
            settings.settingsTab = this == OVERLAYS
                    ? CREATE_OVERLAY
                    : OVERLAYS;
            settings.init();
        }
    }


    @VREventHandler
    public void disableWorldHands(AllowClientFeatureVREvent event) {
        if (event.getFeature() == ClientFeature.VR_WORLD_HANDS
                || event.getFeature() == ClientFeature.AIM_EFFECTS
                || event.getFeature() == ClientFeature.INPUT_MOVEMENT) {
            if (isVisible()) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    protected void init() {
        clearWidgets();
        setDragged(false);
        backgroundExtended = false;

        menuEdgeX = (width - BACKGROUND_WIDTH + 10) / 2;
        menuEdgeY = (height - BACKGROUND_HEIGHT) / 2;

        menuEdgeWidth = BACKGROUND_WIDTH + 10;
        menuEdgeHeight = BACKGROUND_HEIGHT;

        updateCursorEdges();

        //TAB BUTTON
        var tabTexture = settingsTab == SettingsTab.OVERLAYS
                ? SettingsTextures.BUTTON_TAB_RIGHT
                : SettingsTextures.BUTTON_TAB_LEFT;
        WidgetInfoButton tabInfo = new WidgetInfoButton(
                tabTexture,
                tabTexture,
                menuEdgeX
                        + (settingsTab == SettingsTab.OVERLAYS ? 115 : 0),
                menuEdgeY + 6,
                115, 23
        ).setTextColor(TEXT_COLOR)
                .setText(settingsTab == SettingsTab.OVERLAYS
                        ? TITLE_CREATE_OVERLAY
                        : TITLE_OVERLAYS
                );

        tabButton = new ImageButton(
                tabInfo,
                (it) -> {
                    settingsTab.changeTab(this);
                }
        );
        this.addRenderableWidget(
                tabButton
        );

        //CLOSE BUTTON
        closeButton = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.BUTTON_CLOSE,
                        SettingsTextures.BUTTON_CLOSE_HOVERED,
                        menuEdgeX + 235,
                        menuEdgeY + 12,
                        19, 19
                ),
                (it) -> setEnabled(false)
        );
        this.addRenderableWidget(
                closeButton
        );

        //DRAG BUTTON
        dragButton = new ImageButton(
                new WidgetInfoButton(
                        SettingsTextures.BUTTON_DRAG,
                        SettingsTextures.BUTTON_DRAG_HOVERED,
                        menuEdgeX + 235,
                        menuEdgeY + 35,
                        19, 19
                ).setTextureSelected(SettingsTextures.BUTTON_DRAG_SELECTED),
                (it) -> setDragged(true)
        );

        this.addRenderableWidget(
                dragButton
        );

        widgetSet = settingsTab.widgetSet(this);
        widgetSet.initWidgets()
                .forEach(this::addRenderableWidget);

        VROverlayOptionsMenu optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager
                .getOverlay(VROverlayOptionsMenu.ID);
        assert optionsMenu != null;
        optionsMenu.setEnabled(false);
    }

    @Override
    public void onPreRender(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float partialTicks) {
        //MAIN BACKGROUND
        guiGraphics.blit(
                settingsTab.background(),
                menuEdgeX, menuEdgeY,
                0, 0,
                256, 256
        );
        //EXTRA BACKGROUND
        guiGraphics.blit(
                settingsTab.backgroundExtra(this),
                menuEdgeX + 230, menuEdgeY + 6,
                0, 0,
                backgroundExtended ? 128 : 27, 245,
                backgroundExtended ? 128 : 27, 245
        );

        //WIDGET SET TITLE
        Font font = Minecraft.getInstance().font;
        Component text = settingsTab == SettingsTab.OVERLAYS
                ? TITLE_OVERLAYS
                : TITLE_CREATE_OVERLAY;
        GuiHelper.renderScalableText(
                guiGraphics,
                font,
                text.getString(),
                TEXT_COLOR.toInt(),
                menuEdgeX
                        + (settingsTab == SettingsTab.OVERLAYS ? 0 : 115),
                menuEdgeY + 6,
                115, 23,
                true
        );

        widgetSet.onPreRender(guiGraphics, pMouseX, pMouseY, partialTicks);

        updateCursorEdges();
    }

    private void updateCursorEdges() {
        cursorBoundsX = menuEdgeX + cursorEdgeOffsetX;
        cursorBoundsY = menuEdgeY + cursorEdgeOffsetY;
        cursorBoundsWidth = menuEdgeWidth + cursorEdgeOffsetWidth;
        cursorBoundsHeight = menuEdgeHeight + cursorEdgeOffsetHeight;
        if (backgroundExtended) {
            cursorBoundsWidth += 100;
        }
    }

    public void repopulateWidgets() {
        clearWidgets();
        addRenderableWidget(tabButton);
        addRenderableWidget(closeButton);
        addRenderableWidget(dragButton);
        widgetSet.getWidgets().forEach(this::addRenderableWidget);
    }

    public void setSettingsTab(SettingsTab settingsTab) {
        this.settingsTab = settingsTab;
        init();
    }

    public void setOverlaysTab(@NotNull VROverlay select) {
        this.settingsTab = SettingsTab.OVERLAYS;
        init();
        var overlayListWidget = ((OverlaysWidgetSet) widgetSet).getOverlaysList();
        var overlaysList = overlayListWidget.getList();


        var filtersWidget = (FilterListBinaryWidgetSet<String>) overlayListWidget.getFilterWidgetSet();
        //main filters
        filtersWidget.getFiltersWidgetFirst().getList()
                .changeSelectedAll(false);
        filtersWidget.getFiltersWidgetFirst().getList()
                .setSelected("has_options");
        //addon filters
        filtersWidget.getFiltersWidgetSecond().getList()
                .changeSelectedAll(true);
        var overlayEntry = overlaysList.getEntry(select.getId());
        if (overlayEntry != null) {
            overlaysList.setSelected(overlayEntry);
            overlaysList.scrollTo(overlayEntry);
        }
    }

    @Override
    protected void onPreTick() {
        if (!isInViewDistance()) {
            setEnabled(false);
        }
    }

    @Override
    protected void onTick() {
        widgetSet.onTick();
    }


    @Override
    protected void onUpdatePose(float partialTicks) {
        VROverlayHelper.applyRoomPose(
                this,
                getPose().getScale(),
                roomPosition,
                roomRotation
        );
    }

    @Override
    protected boolean updateVisibility() {
        return true;
    }

    @Override
    public boolean supportsLight() {
        return false;
    }

    @Override
    public void onEnable() {
        VROverlayHelper.applyPose(
                this,
                PoseAnchor.HMD,
                PoseAnchor.HMD,
                getPose().getScale(),
                true,
                posOffset,
                rotationOffset
        );
        roomPosition = ClientContext.player.getPoseData(PoseDataType.ROOM)
                .convertPositionFrom(PoseDataType.RENDER, getPose().getPosition());
        roomRotation = ClientContext.player.getPoseData(PoseDataType.ROOM)
                .convertRotationFrom(PoseDataType.RENDER, getPose().getRotation());
    }

    @Override
    public void onDisable() {
        VROverlayOptionsMenu optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager
                .getOverlay(VROverlayOptionsMenu.ID);
        assert optionsMenu != null;
        optionsMenu.setEnabled(false);
        setDragged(false);
        settingsTab = SettingsTab.OVERLAYS;

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int buttonType) {
        if (getForcedAnchor() != null) {
            setDragged(false);
            return true;
        }

        VROverlayDemo demo = (VROverlayDemo) ClientContext.overlayManager
                .getOverlay(VROverlayDemo.ID);
        if (demo != null && demo.getMovingByAnchor() != null) {
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, buttonType);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        if (getForcedAnchor() != null) {
            setDragged(false);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, buttonType);
    }

    private void setDragged(boolean flag) {
        if (dragButton != null) {
            dragButton.setSelected(flag);
        }
        if (!flag && getForcedAnchor() != null) {
            if (ClientContext.cursorHandler.getForceFocused() == this) {
                ClientContext.cursorHandler.setForceFocused(
                        null
                );
            }
            setForcedAnchor(null);
        } else if (flag) {
            ClientContext.cursorHandler.setForceFocused(
                    this
            );
            PoseAnchor anchor = ClientContext.cursorHandler
                    .getCursorHand() == ControllerHand.MAIN
                    ? PoseAnchor.MAIN_HAND
                    : PoseAnchor.OFFHAND;
            setForcedAnchor(anchor);
        }
    }

    @Override
    public void setForcedAnchor(@Nullable PoseAnchor forcedAnchor) {
        if(getForcedAnchor() != null && forcedAnchor == null){
            roomPosition = ClientContext.player.getPoseData(PoseDataType.ROOM)
                    .convertPositionFrom(PoseDataType.RENDER, getPose().getPosition());
            roomRotation = ClientContext.player.getPoseData(PoseDataType.ROOM)
                    .convertRotationFrom(PoseDataType.RENDER, getPose().getRotation());
        }
        super.setForcedAnchor(forcedAnchor);
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
    }
}
