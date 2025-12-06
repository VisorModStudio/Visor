package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.player.pose.PoseAnchor;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.gui.helpers.GuiHelper;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import me.phoenixra.visor.api.client.gui.widgets.ButtonImaged;
import me.phoenixra.visor.api.client.gui.widgets.sets.FilterListBinaryWidgetSet;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
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
            "visor:textures/gui/overlays/settings/bg_main_1.png"
    );
    private static final ResourceLocation BACKGROUND_CREATE = new ResourceLocation(
            "visor:textures/gui/overlays/settings/bg_main_2.png"
    );

    private static final ResourceLocation BACKGROUND_EXTRA = new ResourceLocation(
            "visor:textures/gui/overlays/settings/bg_main_extra_1.png"
    );
    private static final ResourceLocation BACKGROUND_EXTRA_EXTENDED = new ResourceLocation(
            "visor:textures/gui/overlays/settings/bg_main_extra_2.png"
    );

    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 256;


    private static final Component TITLE_CREATE_OVERLAY
            = Component.translatable("visor.overlay.options.main.create_overlay");
    private static final Component TITLE_OVERLAYS
            = Component.translatable("visor.overlay.options.main.overlays");


    private final Vector3f posOffset = new Vector3f(0, 0, -0.75f);
    private final Vector3f rotationOffset = new Vector3f(0, 0, 0);

    private Vector3fc relativePosition = null;
    private Matrix4f relativeRotation = null;

    @Getter
    private SettingsTab settingsTab = SettingsTab.OVERLAYS;
    @Getter
    private WidgetSet widgetSet;

    @Getter
    @Setter
    private boolean backgroundExtended = false;


    @Getter
    private int menuBoundsX, menuBoundsY, menuBoundsWidth, menuBoundsHeight;

    @Getter
    @Setter
    private int cursorBoundsOffsetX, cursorBoundsOffsetY,
            cursorBoundsOffsetWidth, cursorBoundsOffsetHeight;

    private ButtonImaged tabButton;
    private ButtonImaged closeButton;
    private ButtonImaged dragButton;


    private final OverlaysWidgetSet overlaysWidgetSet;
    private final CreateOverlayWidgetSet createOverlayWidgetSet;

    public VROverlaySettings(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id, ElementPriority.NORMAL, 0.6f);
        VisorAPI.eventBus().registerListener(owner, this);
        overlaysWidgetSet = new OverlaysWidgetSet(
                this, this::repopulateWidgets
        );
        createOverlayWidgetSet = new CreateOverlayWidgetSet(
                this, this::repopulateWidgets
        );
    }





    @Override
    protected void init() {
        clearWidgets();
        setDragged(false);
        backgroundExtended = false;

        menuBoundsX = (width - BACKGROUND_WIDTH + 10) / 2;
        menuBoundsY = (height - BACKGROUND_HEIGHT) / 2;

        menuBoundsWidth = BACKGROUND_WIDTH + 10;
        menuBoundsHeight = BACKGROUND_HEIGHT;

        updateCursorEdges();

        //TAB BUTTON
        var tabTexture = settingsTab == SettingsTab.OVERLAYS
                ? SettingsTextures.BUTTON_TAB_RIGHT
                : SettingsTextures.BUTTON_TAB_LEFT;
        WidgetInfoButtonImaged tabInfo = new WidgetInfoButtonImaged()
                .pos(menuBoundsX + (settingsTab == SettingsTab.OVERLAYS ? 115 : 0), menuBoundsY + 6)
                .size(115, 23)
                .setTexture(tabTexture)
                .setTextColor(TEXT_COLOR)
                .setText(settingsTab == SettingsTab.OVERLAYS
                        ? TITLE_CREATE_OVERLAY
                        : TITLE_OVERLAYS
                );

        tabButton = new ButtonImaged(
                tabInfo,
                (it) -> {
                    settingsTab.changeTab(this);
                }
        );
        this.addRenderableWidget(
                tabButton
        );

        //CLOSE BUTTON
        closeButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(menuBoundsX + 235, menuBoundsY + 12)
                        .size(19, 19)
                        .setTexture(SettingsTextures.BUTTON_CLOSE)
                        .setTextureHovered(SettingsTextures.BUTTON_CLOSE_HOVERED),
                (it) -> setEnabled(false)
        );
        this.addRenderableWidget(
                closeButton
        );

        //DRAG BUTTON
        dragButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(menuBoundsX + 235, menuBoundsY + 35)
                        .size(19, 19)
                        .setTexture(SettingsTextures.BUTTON_DRAG)
                        .setTextureHovered(SettingsTextures.BUTTON_DRAG_HOVERED)
                        .setTextureSelected(SettingsTextures.BUTTON_DRAG_SELECTED),
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
                menuBoundsX, menuBoundsY,
                0, 0,
                256, 256
        );
        //EXTRA BACKGROUND
        guiGraphics.blit(
                settingsTab.backgroundExtra(this),
                menuBoundsX + 230, menuBoundsY + 6,
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
                menuBoundsX
                        + (settingsTab == SettingsTab.OVERLAYS ? 0 : 115),
                menuBoundsY + 6,
                115, 23,
                true
        );

        widgetSet.onPreRender(guiGraphics, pMouseX, pMouseY, partialTicks);

        updateCursorEdges();
    }

    private void updateCursorEdges() {
        cursorBoundsX = menuBoundsX + cursorBoundsOffsetX;
        cursorBoundsY = menuBoundsY + cursorBoundsOffsetY;
        cursorBoundsWidth = menuBoundsWidth + cursorBoundsOffsetWidth;
        cursorBoundsHeight = menuBoundsHeight + cursorBoundsOffsetHeight;
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
        VROverlayHelper.applyRelativePose(
                this,
                getPose().getScale(),
                relativePosition,
                relativeRotation
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
        relativePosition = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE)
                .convertPositionFrom(PlayerPoseType.RENDER, getPose().getPosition());
        relativeRotation = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE)
                .convertRotationFrom(PlayerPoseType.RENDER, getPose().getRotation());
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

    @Override
    public void setForcedAnchor(@Nullable PoseAnchor forcedAnchor) {
        if(getForcedAnchor() != null && forcedAnchor == null){
            relativePosition = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE)
                    .convertPositionFrom(PlayerPoseType.RENDER, getPose().getPosition());
            relativeRotation = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE)
                    .convertRotationFrom(PlayerPoseType.RENDER, getPose().getRotation());
        }
        super.setForcedAnchor(forcedAnchor);
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
                    .getCursorHand() == HandType.MAIN
                    ? PoseAnchor.MAIN_HAND
                    : PoseAnchor.OFFHAND;
            setForcedAnchor(anchor);
        }
    }



    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
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
}
