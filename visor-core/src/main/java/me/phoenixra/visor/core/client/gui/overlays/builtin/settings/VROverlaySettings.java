package me.phoenixra.visor.core.client.gui.overlays.builtin.settings;


import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.overlay.ModelViewAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayType;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.widgets.DropDownListWidget;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
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


public class VROverlaySettings extends VROverlayScreen {
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

    private boolean movingPosition;

    protected VROverlay selectedOverlay;
    private int selectedOverlayIndex = -1;

    private DropDownListWidget selectOverlayWidget;
    private List<VROverlay> selectableOverlays;

    public VROverlaySettings(@NotNull VisorAddon owner,
                             @NotNull String id) {
        super(owner, id);

        overlayScale = 0.55f;


    }


    @Override
    protected void init() {
        clearWidgets();

        addedWidgetSet = false;
        movingPosition = false;
        mouseEdgeX = width / 2 - BACKGROUND_WIDTH / 2;
        mouseEdgeY = height / 2 - BACKGROUND_HEIGHT / 2;

        mouseEdgeWidth = (width / 2 + BACKGROUND_WIDTH / 2) - mouseEdgeX;
        mouseEdgeHeight = (height / 2 + BACKGROUND_HEIGHT / 2) - mouseEdgeY;

        selectableOverlays = ClientContext.overlayManager
                .getOverlaysRegistry().getSortedElements().stream().filter(
                        it->!it.getOptionsList().isEmpty()
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
                                mouseEdgeX + mouseEdgeWidth - 20,
                                mouseEdgeY
                        )
                        .size(20, 20)
                        .build()
        );

        this.addRenderableWidget(
                Button.builder(
                                Component.literal("§a<->"),
                                (p) ->
                                {
                                    movingPosition = true;
                                }
                        )
                        .pos(
                                mouseEdgeX,
                                mouseEdgeY
                        )
                        .size(20, 20)
                        .build()
        );

        List<Component> elements = new ArrayList<>();
        elements.add(Component.translatable("visor.button.create_new"));
        for(VROverlay overlay : selectableOverlays){
            elements.add(Component.literal(overlay.getDisplayName()));
        }
        selectOverlayWidget = DropDownListWidget.builder(elements)
                .pos(mouseEdgeX + ((mouseEdgeWidth)/2 - 95/2), mouseEdgeY + 50)
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
    public void render(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float partialTicks) {
        VROverlayHelper.renderImage(
                guiGraphics,
                BACKGROUND,
                mouseEdgeX, mouseEdgeY,
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
        super.render(guiGraphics, pMouseX, pMouseY, partialTicks);
    }

    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {

    }



    @Override
    protected void onTick() {
        if(widgetSet != null) {
            widgetSet.onTick();
        }
    }

    @Override
    public void applyModelView(float partialTick) {
        if(movingPosition){
            ModelViewAnchor anchor = ClientContext.cursorHandler
                    .getActiveCursorHand() == ControllerHand.MAIN ?
                    ModelViewAnchor.MAIN_HAND : ModelViewAnchor.OFFHAND;
            VROverlayHelper.applyModelView(
                    this,
                    anchor,
                    anchor,
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
        VROverlayHelper.applyModelView(
                this,
                ModelViewAnchor.HMD,
                ModelViewAnchor.HMD,
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
        movingPosition = false;

    }

    @Override
    public boolean mouseClicked(double d, double e, int i) {
        if(movingPosition){
            movingPosition = false;
            return true;
        }
        VROverlayDemo demo = (VROverlayDemo) ClientContext.overlayManager
                .getOverlay(VROverlayDemo.ID);
        if(demo != null && demo.getMovingByAnchor() != null){
            return true;
        }

        return super.mouseClicked(d, e, i);
    }

    @Override
    public boolean mouseReleased(double d, double e, int i) {
        if(movingPosition){
            movingPosition = false;
            return true;
        }
        return super.mouseReleased(d, e, i);
    }


    @Override
    protected @NotNull List<OverlayOptionCategory> createOptions() {
        return List.of(
        );
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
        protected DropDownListWidget overlayTypeWidget;
        protected List<VROverlayType> selectableOverlayTypes;

        protected EditBox overlayIdField;

        private Button confirmButton;

        public NewOverlayWidgets() {
            super(State.CREATE_NEW);
        }

        @Override
        public List<AbstractWidget> initWidgets() {
            selectableOverlayTypes = ClientContext.overlayManager
                    .getOverlayTypesRegistry()
                    .getAllElements().stream().toList();

            List<Component> elements = new ArrayList<>();
            for(VROverlayType overlayType : selectableOverlayTypes){
                elements.add(Component.literal(overlayType.id()));
            }
            overlayTypeWidget =  new DropDownListWidget(
                    mouseEdgeX + ((mouseEdgeWidth)/2 - 95/2),
                    mouseEdgeY + 80,
                    95,25,
                    Component.translatable("visor.overlaySettings.main.widget.choose_overlay_type"),
                    elements

            );

            overlayIdField = new EditBox(
                    MC.font,
                    mouseEdgeX + ((mouseEdgeWidth)/2 - 95/2),
                    mouseEdgeY + 110,
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
                                int index = overlayTypeWidget.getSelectedIndex();
                                if(index<0) return;
                                if(index >= selectableOverlayTypes.size()) return;

                                String id = overlayIdField.getValue();
                                VROverlayRegistry registry = ClientContext.overlayManager.getOverlaysRegistry();
                                if(id.isBlank() || registry.getElement(id) != null) {
                                    return;
                                }

                                VROverlayType overlayType = selectableOverlayTypes.get(index);
                                try {
                                    VROverlay overlay = overlayType.constructor().newInstance(
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
                                    VisorState.destroyVRWithError(e);
                                }
                            }
                    )
                    .pos(
                            mouseEdgeX + ((mouseEdgeWidth)/2 - 95/2),
                            mouseEdgeY + 150
                    )
                    .size(95, 20)
                    .build();
            return List.of(overlayTypeWidget, overlayIdField, confirmButton);
        }

        @Override
        public List<AbstractWidget> getWidgets() {
            return List.of(overlayTypeWidget, overlayIdField, confirmButton);
        }

        public void onRender(){
            overlayIdField.setVisible(!overlayTypeWidget.isExpanded());
            confirmButton.visible = !overlayTypeWidget.isExpanded();
        }

        @Override
        public void onTick() {
            overlayIdField.tick();
        }
    }

    private class WidgetSetSetupExisting extends WidgetSet{
        protected Button removeOverlayButton;
        protected DropDownListWidget optionCategoryWidget;
        protected List<OverlayOptionCategory> selectableOptionCategories;

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
                            mouseEdgeX + ((mouseEdgeWidth)/2 + 95/2) + 5,
                            mouseEdgeY + 50
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

                                OverlayOptionCategory currentCategory =
                                        selectableOptionCategories.get(categoryIndex);
                                currentCategory.saveDefaults();

                                var optionsMenu = (VROverlayOptionsMenu) ClientContext.overlayManager.getOverlay(VROverlayOptionsMenu.ID);
                                assert optionsMenu != null;
                                optionsMenu.init();
                            }
                    )
                    .pos(
                            mouseEdgeX + ((mouseEdgeWidth)/2 - 95/2),
                            mouseEdgeY + 120
                    )
                    .size(95, 25)
                    .build();

            selectableOptionCategories = selectedOverlay.getOptionsList().stream().toList();
            List<Component> elements = new ArrayList<>();
            for(OverlayOptionCategory optionCategory : selectableOptionCategories){
                elements.add(optionCategory.getDisplayName());
            }
            optionCategoryWidget =  DropDownListWidget.builder(elements)
                    .pos(mouseEdgeX + ((mouseEdgeWidth)/2 - 95/2), mouseEdgeY + 80)
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

                                OverlayOptionCategory currentCategory =
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
