package me.phoenixra.visor.core.client.gui.screens.settings.overlays;

import lombok.Getter;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.options.sections.OverlayOptionsLocation;
import me.phoenixra.visor.api.client.gui.widgets.DropDownListWidget;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview.ModificationType;
import me.phoenixra.visor.core.client.gui.screens.settings.overlays.modelview.widgets.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Getter
public class OptionsScreenModelView extends OverlayOptionsScreen<OverlayOptionsLocation> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation(
            "visor:textures/gui/overlays_settings.png"
    );
    private final int BACKGROUND_WIDTH = 164;
    private final int BACKGROUND_HEIGHT = 246;


    private final Vector3f positionOffset = new Vector3f(
            -mainMenuWidth/2,0,0
    );
    private final Vector3f rotationOffset = new Vector3f(
            0,0,0
    );

    @Getter
    private VROverlayDemo demoOverlay = null;

    private ModificationType modificationType = null;
    private Button emulationButton = null;
    private Button teleportButton = null;

    private DropDownListWidget posTypeWidget;
    private DropDownListWidget rotationTypeWidget;
    private DropDownListWidget modifyTypeWidget;

    private WidgetSet widgetSet;

    private boolean addedWidgetSet;

    private boolean demoDisplayed;

    private boolean emulateModelViewCache;

    public OptionsScreenModelView(@NotNull OverlayOptionsLocation optionCategory,
                               float mainMenuWidth, float mainMenuHeight) {
        super(optionCategory,mainMenuWidth,mainMenuHeight);
    }

    @Override
    protected void init() {
        clearWidgets();

        demoOverlay = (VROverlayDemo) ClientContext.overlayManager.getOverlay(
                VROverlayDemo.ID
        );
        mouseEdgeX = width - BACKGROUND_WIDTH;
        mouseEdgeY = height - BACKGROUND_HEIGHT;

        mouseEdgeWidth = width - mouseEdgeX;
        mouseEdgeHeight = height - mouseEdgeY;

        int width = mouseEdgeWidth;
        int height = mouseEdgeHeight;

        int middleX = mouseEdgeX + width/2;

        Component demoName = Component.translatable("visor.overlaySettings.modelView.widget.demo");
        this.addRenderableWidget(
                Button.builder(
                                Component.literal(
                                                StringUtils.formatColorCodes(
                                                        (demoDisplayed? "&a" :"&c") +demoName.getString()
                                                )
                                ),
                                (p) ->
                                {
                                    demoDisplayed = !demoDisplayed;
                                    emulationButton.visible = demoDisplayed;
                                    teleportButton.visible = demoDisplayed && !demoOverlay.isEmulatingModelView();
                                    p.setMessage(
                                            Component.literal(
                                                    StringUtils.formatColorCodes(
                                                            (demoDisplayed? "&a" :"&c") +demoName.getString()
                                                    )
                                            )
                                    );

                                }
                        )
                        .pos(
                                middleX - 150/2,
                                mouseEdgeY + 10
                        )
                        .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.demo.tooltip")))
                        .size(40,10)
                        .build()
        );

        teleportButton = Button.builder(
                        Component.translatable("visor.overlaySettings.modelView.widget.tp"),
                        (p) ->
                        {
                            demoOverlay.teleportToHMD();

                        }
                )
                .pos(
                        middleX -10,
                        mouseEdgeY + 10
                )
                .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.tp.tooltip")))
                .size(20,10)
                .build();
        this.addRenderableWidget(
                teleportButton
        );

        Component emuName = Component.translatable("visor.overlaySettings.modelView.widget.emulation");
        emulationButton = Button.builder(
                        Component.literal(
                                StringUtils.formatColorCodes(
                                        (demoOverlay.isEmulatingModelView()? "&a" :"&c") +emuName.getString()
                                )
                        ),
                        (p) ->
                        {
                            demoOverlay.setEmulatingModelView(!demoOverlay.isEmulatingModelView());

                        }
                )
                .pos(
                        middleX + 35,
                        mouseEdgeY + 10
                )
                .tooltip(Tooltip.create(
                        Component.translatable("visor.overlaySettings.modelView.widget.emulation.tooltip")
                        )
                )
                .size(40,10)
                .build();
        this.addRenderableWidget(
                emulationButton
        );

        emulationButton.visible = demoDisplayed;
        teleportButton.visible = demoDisplayed && !demoOverlay.isEmulatingModelView();
        demoOverlay.setEnabled(false);

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "visor.overlaySettings.modelView.widget.tick",
                                        String.valueOf(optionCategory.isTickModelView())
                                ),
                                (p) ->
                                {
                                    optionCategory.setTickModelView(!optionCategory.isTickModelView());
                                    p.setMessage(Component.translatable(
                                            "visor.overlaySettings.modelView.widget.tick",
                                            String.valueOf(optionCategory.isTickModelView())
                                    ));
                                }
                        )
                        .pos(
                                middleX - 150/2,
                                mouseEdgeY + 30
                        )
                        .size(70,15)
                        .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.tick.tooltip")))
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "visor.overlaySettings.modelView.widget.aimed",
                                        String.valueOf(optionCategory.isAimRotation())
                                ),
                                (p) ->
                                {
                                    optionCategory.setAimRotation(!optionCategory.isAimRotation());
                                    p.setMessage(
                                            Component.translatable(
                                                    "visor.overlaySettings.modelView.widget.aimed",
                                                    String.valueOf(optionCategory.isAimRotation())
                                            )
                                    );
                                }
                        )
                        .pos(
                                middleX + 5,
                                mouseEdgeY + 30
                        )
                        .size(70,15)
                        .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.modelView.widget.aimed.tooltip")))
                        .build()
        );


        List<Component> elements = new ArrayList<>();
        List<PoseAnchor> anchorList = Arrays.stream(PoseAnchor.values()).toList();
        for(PoseAnchor anchor : anchorList){
            elements.add(anchor.getName());
        }

        posTypeWidget = DropDownListWidget.builder(elements)
                .pos(middleX - 150/2, mouseEdgeY + 50)
                .size(70,15)
                .setStartIndex(
                        anchorList.indexOf(
                                optionCategory.getPositionAnchor()
                        )
                )
                .setMessage(Component.translatable("visor.overlaySettings.modelView.widget.posType"))
                .setResponder(it->{
                    optionCategory.setPositionAnchor(
                            anchorList.get(it)
                    );
                }).build();
        this.addRenderableWidget(
                posTypeWidget
        );

        rotationTypeWidget = DropDownListWidget.builder(elements)
                .pos(middleX + 5, mouseEdgeY + 50)
                .size(70,15)
                .setStartIndex(
                        anchorList.indexOf(
                                optionCategory.getRotationAnchor()
                        )
                )
                .setMessage(Component.translatable("visor.overlaySettings.modelView.widget.rotationType"))
                .setResponder(it->{
                    optionCategory.setRotationAnchor(
                            anchorList.get(it)
                    );
                }).build();
        this.addRenderableWidget(
                rotationTypeWidget
        );


        List<ModificationType> modificationsList = Arrays.stream(ModificationType.values()).toList();
        elements = new ArrayList<>();
        for(ModificationType entry : modificationsList){
            elements.add(entry.getName());
        }

        modifyTypeWidget =  DropDownListWidget.builder(elements)
                .pos(middleX - 150/2, mouseEdgeY + 70)
                .size(150,15)
                .setVisibleItems(7)
                .setStartIndex(
                        modificationType == null
                                ? -1
                                : modificationsList.indexOf(modificationType)
                )
                .setMessage(Component.translatable("visor.overlaySettings.modelView.widget.howModify"))

                .setResponder(it->{
                    modificationType =  modificationsList.get(it);
                    init();
                }).build();
        this.addRenderableWidget(
                modifyTypeWidget
        );


        if(modificationType == null) return;
        switch (modificationType){
            case FORMULA_POSITION -> {
                widgetSet = new FormulaPosWidgets(this);
            }
            case FORMULA_ROTATION -> {
                widgetSet = new FormulaRotationWidgets(this);
            }
            case SLIDERS_POSITION -> {
                widgetSet = new SlidersPositionWidgets(this);
            }
            case SLIDERS_ROTATION -> {
                widgetSet = new SlidersRotationWidgets(this);
            }
            case BY_OFFSET -> {
                widgetSet = new ByOffsetWidgets(this);
            }
            case BY_HAND -> {
                widgetSet = new ByHandWidgets(this);
            }
        }
        if(widgetSet != null) {
            widgetSet.initWidgets(
                    mouseEdgeX,
                    mouseEdgeY + 90,
                    width,
                    height - 65
            ).forEach(this::addRenderableWidget);
            addedWidgetSet = true;
        }



    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        VROverlayHelper.renderImage(
                guiGraphics,
                BACKGROUND,
                mouseEdgeX, mouseEdgeY,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT
        );

        boolean modifyTypeDisabled = (posTypeWidget.isExpanded()
                || rotationTypeWidget.isExpanded());
        if(modifyTypeWidget.visible
                && modifyTypeDisabled){
            modifyTypeWidget.visible = false;
            removeWidget(modifyTypeWidget);
        }else if(!modifyTypeWidget.visible && !modifyTypeDisabled){
            modifyTypeWidget.visible = true;
            addRenderableWidget(modifyTypeWidget);
        }
        boolean widgetSetDisabled = (modifyTypeWidget.isExpanded()
                || posTypeWidget.isExpanded()
                || rotationTypeWidget.isExpanded());
        if(addedWidgetSet
                && widgetSetDisabled){
            widgetSet.getWidgets().forEach(
                    this::removeWidget
            );
            addedWidgetSet = false;
        } else if (!addedWidgetSet
                && widgetSet != null && !widgetSetDisabled) {
            widgetSet.getWidgets().forEach(
                    this::addRenderableWidget
            );
            addedWidgetSet = true;
        }
        super.render(guiGraphics, i, j, f);

    }



    @Override
    public void tick() {
        if(demoDisplayed && !demoOverlay.isEnabled()){
            demoOverlay.showDemo(optionCategory.getOwner());
        }else if(!demoDisplayed && demoOverlay.isEnabled()){
            demoOverlay.setEnabled(false);
        }
        if(emulateModelViewCache != demoOverlay.isEmulatingModelView()){
            emulateModelViewCache = !emulateModelViewCache;
            onEmulationUpdate(emulateModelViewCache);
        }
        super.tick();
    }

    @Override
    public void removed() {
        demoOverlay.setEnabled(false);
    }


    public void onEmulationUpdate(boolean emulationActive){
        teleportButton.visible = demoDisplayed && !emulationActive;

        Component emuName = Component.translatable("visor.overlaySettings.modelView.widget.emulation");
        emulationButton.setMessage(
                Component.literal(
                        StringUtils.formatColorCodes(
                                (emulationActive? "&a" :"&c") +emuName.getString()
                        )
                )
        );
    }

}
