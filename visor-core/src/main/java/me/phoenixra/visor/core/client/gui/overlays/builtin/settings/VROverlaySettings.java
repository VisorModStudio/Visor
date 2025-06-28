package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;


import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplateRecord;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.DropDownListWidget;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.gui.registry.VROverlayRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


public class VROverlaySettings extends VROverlayScreen
        implements VREventListener {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            "visor:textures/gui/overlays_settings.png"
    );

    public static final String ID = "settings";


    private final int BACKGROUND_WIDTH = 164;
    private final int BACKGROUND_HEIGHT = 246;

    private final Vector3f posOffset = new Vector3f(0, 0, -0.75f);

    private final Vector3f posMovingOffset = new Vector3f(0, 0, -0.3f);
    private final Vector3f rotationOffset = new Vector3f(0, 0, 0);


    private State currentState = State.NOT_SELECTED;

    private WidgetSet widgetSet;

    private boolean addedWidgetSet;

    private boolean draggingByCursorHand;

    protected VROverlayTemplate selectedOverlay;
    private int selectedOverlayIndex = -1;

    private DropDownListWidget selectOverlayWidget;
    private List<VROverlayTemplate> selectableOverlays;

    public VROverlaySettings(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id, ElementPriority.NORMAL,0.55f);
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void disableWorldHands(AllowClientFeatureVREvent event){
        if(event.getFeature() == ClientFeature.VR_WORLD_HANDS
                || event.getFeature() == ClientFeature.AIM_EFFECTS
                || event.getFeature() == ClientFeature.INPUT_MOVEMENT) {
            if(isVisible()){
                event.setCanceled(true);
            }
        }
    }

    @Override
    protected void init() {
        clearWidgets();

        addedWidgetSet = false;
        resetDragging();

        cursorEdgeX = width / 2 - BACKGROUND_WIDTH / 2;
        cursorEdgeY = height / 2 - BACKGROUND_HEIGHT / 2;

        cursorEdgeWidth = (width / 2 + BACKGROUND_WIDTH / 2) - cursorEdgeX;
        cursorEdgeHeight = (height / 2 + BACKGROUND_HEIGHT / 2) - cursorEdgeY;

        selectableOverlays = ClientContext.overlayManager
                .getOverlaysRegistry().getSortedElements().stream()
                .map(VROverlay::asTemplate)
                .filter(it ->
                        it != null && !it.getTemplateOptions().isEmpty()
                ).toList();
        this.addRenderableWidget(
                Button.builder(
                                Component.literal("§cx"),
                                (p) ->
                                {
                                    setEnabled(false);
                                }
                        )
                        .pos(
                                cursorEdgeX + cursorEdgeWidth - 20,
                                cursorEdgeY
                        )
                        .size(20, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("§a<->"),
                                (p) ->
                                {
                                    ClientContext.cursorHandler.setForceFocused(
                                            this
                                    );
                                    draggingByCursorHand = true;
                                }
                        )
                        .pos(
                                cursorEdgeX,
                                cursorEdgeY
                        )
                        .size(20, 20)
                        .build()
        );

        List<Component> elements = new ArrayList<>();
        elements.add(Component.translatable("visor.button.create_new"));
        for(var overlay : selectableOverlays){
            elements.add(overlay.getOverlayName());
        }
        selectOverlayWidget = DropDownListWidget.builder(elements)
                .pos(cursorEdgeX + ((cursorEdgeWidth)/2 - 95/2), cursorEdgeY + 50)
                .size(95,25)
                .setMessage(Component.translatable("visor.overlaySettings.main.widget.choose_overlay"))
                .setStartIndex(selectedOverlayIndex)
                .setResponder(
                        selectedIndex ->{
                            selectedOverlayIndex = selectedIndex;

                            VROverlayOptionsMenu optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager
                                    .getOverlay(VROverlayOptionsMenu.ID);
                            assert optionsMenu != null;

                            if(selectedIndex < 0){
                                currentState = State.NOT_SELECTED;
                                optionsMenu.setEnabled(false);
                                selectedOverlay = null;
                                init();
                                return;
                            }
                            if(selectedIndex == 0){
                                currentState = State.CREATE_NEW;
                                optionsMenu.setEnabled(false);
                                selectedOverlay = null;
                                init();
                                return;
                            }
                            currentState = State.SETUP_EXISTING;
                            selectedOverlay = selectableOverlays.get(selectedIndex-1);
                            optionsMenu.setEnabled(false);
                            init();
                        }
                )
                .build();
        addRenderableWidget(selectOverlayWidget);

        switch (currentState){
            case CREATE_NEW -> {
                widgetSet = new NewOverlayWidgets();
            }
            case SETUP_EXISTING -> {
                widgetSet = new WidgetSetSetupExisting();
            }
            case NOT_SELECTED -> {
                widgetSet = null;
            }
        }
        if(widgetSet != null){
            widgetSet.initWidgets()
                    .forEach(this::addRenderableWidget);
            addedWidgetSet = true;

        }

    }

    @Override
    public void onPreRender(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float partialTicks) {
        VROverlayHelper.renderImage(
                guiGraphics,
                BACKGROUND,
                cursorEdgeX, cursorEdgeY,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT
        );
        if(widgetSet != null) {
            boolean widgetSetDisabled = selectOverlayWidget.isExpanded();
            if (widgetSetDisabled && addedWidgetSet) {
                widgetSet.getWidgets().forEach(this::removeWidget);
                addedWidgetSet = false;
            }else if(!widgetSetDisabled && !addedWidgetSet){
                widgetSet.getWidgets().forEach(this::addRenderableWidget);
                addedWidgetSet = true;
            }
            widgetSet.onRender();
        }

    }


    @Override
    protected void onPreTick() {
        if(!isInViewDistance()){
            setEnabled(false);
        }
    }

    @Override
    protected void onTick() {
        if(widgetSet != null) {
            widgetSet.onTick();
        }
    }

    @Override
    public void updatePose(float partialTicks) {
        if(draggingByCursorHand){
            PoseAnchor anchor = ClientContext.cursorHandler
                    .getCursorHand() == ControllerHand.MAIN ?
                    PoseAnchor.MAIN_HAND : PoseAnchor.OFFHAND;
            VROverlayHelper.applyPose(
                    this,
                    anchor,
                    anchor,
                    getPose().getScale(),
                    true,
                    posMovingOffset,
                    rotationOffset
            );
        }
    }

    @Override
    public boolean updateVisibility() {
        return true;
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
    }

    @Override
    public void onDisable() {
        VROverlayOptionsMenu optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager
                .getOverlay(VROverlayOptionsMenu.ID);
        assert optionsMenu != null;
        optionsMenu.setEnabled(false);

        selectedOverlayIndex = -1;
        selectedOverlay = null;
        currentState = State.NOT_SELECTED;
        resetDragging();

    }

    @Override
    public boolean mouseClicked(double x, double y, int buttonType) {
        if(draggingByCursorHand){
            resetDragging();

            return true;
        }
        VROverlayDemo demo = (VROverlayDemo) ClientContext.overlayManager
                .getOverlay(VROverlayDemo.ID);
        if(demo != null && demo.getMovingByAnchor() != null){
            return true;
        }

        return super.mouseClicked(x, y, buttonType);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonType) {
        if(draggingByCursorHand){
            resetDragging();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, buttonType);
    }



    private void resetDragging(){
        if(draggingByCursorHand){
            if(ClientContext.cursorHandler.getForceFocused() == this) {
                ClientContext.cursorHandler.setForceFocused(
                        null
                );
            }
            draggingByCursorHand = false;
        }
    }

    private enum State{
        NOT_SELECTED,
        CREATE_NEW,
        SETUP_EXISTING
    }




    private abstract class WidgetSet{
        protected final State state;

        

        public WidgetSet(State state){
            this.state = state;
        }

        public abstract List<AbstractWidget> getWidgets();
        public abstract List<AbstractWidget> initWidgets();

        public abstract void onRender();
        public abstract void onTick();

    }

    private class NewOverlayWidgets extends WidgetSet{
        protected DropDownListWidget overlayTemplateWidget;
        protected List<VROverlayTemplateRecord> selectableOverlayTemplates;

        protected EditBox overlayIdField;

        private Button confirmButton;

        public NewOverlayWidgets() {
            super(State.CREATE_NEW);
        }

        @Override
        public List<AbstractWidget> initWidgets() {
            selectableOverlayTemplates = ClientContext.overlayManager
                    .getOverlayTemplatesRegistry()
                    .getAllElements().stream()
                    .filter(VROverlayTemplateRecord::isPublic).toList();

            List<Component> elements = new ArrayList<>();
            for(var overlayTemplate : selectableOverlayTemplates){
                elements.add(Component.literal(overlayTemplate.id()));
            }
            overlayTemplateWidget =  new DropDownListWidget(
                    cursorEdgeX + ((cursorEdgeWidth)/2 - 95/2),
                    cursorEdgeY + 80,
                    95,25,
                    Component.translatable("visor.overlaySettings.main.widget.choose_overlay_type"),
                    elements

            );

            overlayIdField = new EditBox(
                    MC.font,
                    cursorEdgeX + ((cursorEdgeWidth)/2 - 95/2),
                    cursorEdgeY + 110,
                    95,20,
                    Component.empty()
            );
            overlayIdField.setMaxLength(25);
            overlayIdField.setBordered(true);
            overlayIdField.setVisible(true);
            overlayIdField.setTextColor(AtumColor.WHITE.toInt());
            overlayIdField.setResponder(
                    it->{
                        if(it.isBlank()){
                            overlayIdField.setTextColor(AtumColor.RED.toInt());
                            return;
                        }
                        VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
                        if(registry.getElement(it) != null) {
                            overlayIdField.setTextColor(AtumColor.RED.toInt());
                        }else{
                            overlayIdField.setTextColor(16777215);
                        }
                    }
            );
            overlayIdField.setTooltip(
                    Tooltip.create(
                            Component.translatable("visor.overlaySettings.main.widget.edit_overlay_id"),
                            null
                    )
            );

            confirmButton = Button.builder(
                            Component.translatable("visor.button.confirm"),
                            (p) ->
                            {
                                int index = overlayTemplateWidget.getSelectedIndex();
                                if(index<0) return;
                                if(index >= selectableOverlayTemplates.size()) return;

                                String id = overlayIdField.getValue();
                                VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
                                if(id.isBlank() || registry.getElement(id) != null) {
                                    return;
                                }

                                VROverlayTemplateRecord templateRecord = selectableOverlayTemplates.get(index);
                                try {
                                    VROverlay overlay = templateRecord.constructor().newInstance(
                                            ClientContext.coreAddon,
                                            id
                                    );
                                    ClientContext.overlayManager.getOverlaysRegistry()
                                            .registerElement(overlay);

                                    init();
                                    selectOverlayWidget.setSelectedIndex(
                                            selectableOverlays.indexOf(overlay) + 1,
                                            true
                                    );
                                }catch (Exception e){
                                    VisorState.destroyVRWithErrorScreen(e);
                                }
                            }
                    )
                    .pos(
                            cursorEdgeX + ((cursorEdgeWidth)/2 - 95/2),
                            cursorEdgeY + 150
                    )
                    .size(95, 20)
                    .build();
            return List.of(overlayTemplateWidget, overlayIdField, confirmButton);
        }

        @Override
        public List<AbstractWidget> getWidgets() {
            return List.of(overlayTemplateWidget, overlayIdField, confirmButton);
        }

        public void onRender(){
            overlayIdField.setVisible(!overlayTemplateWidget.isExpanded());
            confirmButton.visible = !overlayTemplateWidget.isExpanded();
        }

        @Override
        public void onTick() {
            overlayIdField.tick();
        }
    }

    private class WidgetSetSetupExisting extends WidgetSet{
        protected Button removeOverlayButton;
        protected DropDownListWidget optionCategoryWidget;
        protected List<OverlayOptions> selectableOptionCategories;

        protected Button loadDefaultsButton;

        public WidgetSetSetupExisting() {
            super(State.SETUP_EXISTING);
        }


        @Override
        public List<AbstractWidget> initWidgets() {
            removeOverlayButton = Button.builder(
                    Component.translatable("visor.overlaySettings.modelView.remove"),
                            (p) ->
                            {
                                if(selectedOverlay == null){
                                    return;
                                }
                                ClientContext.overlayManager.getOverlaysRegistry()
                                        .unregisterElement(selectedOverlay.getId());
                                selectedOverlayIndex = -1;
                                selectedOverlay = null;
                                currentState = State.NOT_SELECTED;
                                init();
                            }
                    )
                    .pos(
                            cursorEdgeX + ((cursorEdgeWidth)/2 + 95/2) + 5,
                            cursorEdgeY + 50
                    )
                    .size(25, 25)
                    .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.remove.tooltip")))
                    .build();
            loadDefaultsButton = Button.builder(
                            Component.translatable("visor.button.load_defaults"),
                            (p) ->
                            {
                                if(selectedOverlay == null){
                                    return;
                                }
                                int categoryIndex = optionCategoryWidget.getSelectedIndex();

                                if(categoryIndex < 0
                                        || categoryIndex >= selectableOptionCategories.size()){

                                    return;
                                }

                                OverlayOptions currentCategory =
                                        selectableOptionCategories.get(categoryIndex);
                                currentCategory.saveDefaults();

                                var optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager.getOverlay(VROverlayOptionsMenu.ID);
                                assert optionsMenu != null;
                                optionsMenu.init();
                            }
                    )
                    .pos(
                            cursorEdgeX + ((cursorEdgeWidth)/2 - 95/2),
                            cursorEdgeY + 120
                    )
                    .size(95, 25)
                    .build();

            selectableOptionCategories = selectedOverlay.getTemplateOptions().stream().toList();
            List<Component> elements = new ArrayList<>();
            for(OverlayOptions optionCategory : selectableOptionCategories){
                elements.add(optionCategory.getDisplayName());
            }
            optionCategoryWidget =  DropDownListWidget.builder(elements)
                    .pos(cursorEdgeX + ((cursorEdgeWidth)/2 - 95/2), cursorEdgeY + 80)
                    .size(95,25)
                    .setMessage(Component.translatable("visor.overlaySettings.main.widget.choose_option_category"))
                    .setResponder(
                            selectedIndex->{
                                /// ///
                                VROverlayOptionsMenu optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager
                                        .getOverlay(VROverlayOptionsMenu.ID);
                                assert optionsMenu != null;


                                if(selectedIndex < 0
                                        || selectedIndex >= selectableOptionCategories.size()){
                                    optionsMenu.setEnabled(false);
                                    return;
                                }

                                OverlayOptions currentCategory =
                                        selectableOptionCategories.get(selectedIndex);
                                optionsMenu.openMenu(
                                        VROverlaySettings.this,
                                        currentCategory
                                );
                                /// //
                            }
                    )
                    .build();

            return List.of(removeOverlayButton, optionCategoryWidget, loadDefaultsButton);
        }

        @Override
        public List<AbstractWidget> getWidgets() {
            return List.of(removeOverlayButton, optionCategoryWidget, loadDefaultsButton);
        }

        @Override
        public void onRender() {
            loadDefaultsButton.visible = !optionCategoryWidget.isExpanded()
                    && optionCategoryWidget.getSelectedIndex() >= 0;
        }

        @Override
        public void onTick() {

        }
    }


}
