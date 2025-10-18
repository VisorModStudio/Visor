package me.phoenixra.visor.core.client.gui.screens.overlayoptions;

import lombok.Getter;
import me.phoenixra.atumconfig.api.utils.StringUtils;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlays.options.OptionsScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.widgets.lists.DropDownListWidget;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.ModificationType;
import me.phoenixra.visor.core.client.gui.screens.overlayoptions.pose.widgets.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



@Getter
public class OptionsScreenPose extends OptionsScreen<OverlayOptionsPose> {



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

    public OptionsScreenPose(@NotNull OverlayOptionsPose optionCategory) {
        super(optionCategory, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        clearWidgets();

        demoOverlay = ClientContext.overlayManager.getOverlay(
                VROverlayDemo.ID,
                VROverlayDemo.class
        );

        int width = cursorBoundsWidth;
        int height = cursorBoundsHeight;

        int middleX = cursorBoundsX + width/2;

        Component demoName = Component.translatable("visor.overlay.options.pose.demo");
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
                                    teleportButton.visible = demoDisplayed && !demoOverlay.isEmulatingPose();
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
                                cursorBoundsY + 10
                        )
                        .tooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.demo.tooltip")))
                        .size(40,10)
                        .build()
        );

        teleportButton = Button.builder(
                        Component.translatable("visor.overlay.options.pose.teleport"),
                        (p) ->
                        {
                            demoOverlay.teleportToHMD();

                        }
                )
                .pos(
                        middleX -10,
                        cursorBoundsY + 10
                )
                .tooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.teleport.tooltip")))
                .size(20,10)
                .build();
        this.addRenderableWidget(
                teleportButton
        );

        Component emuName = Component.translatable("visor.overlay.options.pose.emulation");
        emulationButton = Button.builder(
                        Component.literal(
                                StringUtils.formatColorCodes(
                                        (demoOverlay.isEmulatingPose()? "&a" :"&c") +emuName.getString()
                                )
                        ),
                        (p) ->
                        {
                            demoOverlay.setEmulatingPose(!demoOverlay.isEmulatingPose());

                        }
                )
                .pos(
                        middleX + 35,
                        cursorBoundsY + 10
                )
                .tooltip(Tooltip.create(
                        Component.translatable("visor.overlay.options.pose.emulation.tooltip")
                        )
                )
                .size(40,10)
                .build();
        this.addRenderableWidget(
                emulationButton
        );

        emulationButton.visible = demoDisplayed;
        teleportButton.visible = demoDisplayed && !demoOverlay.isEmulatingPose();
        demoOverlay.setEnabled(false);

        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "visor.overlay.options.pose.tick",
                                        String.valueOf(optionCategory.isTickModelView())
                                ),
                                (p) ->
                                {
                                    optionCategory.setTickModelView(!optionCategory.isTickModelView());
                                    p.setMessage(Component.translatable(
                                            "visor.overlay.options.pose.tick",
                                            String.valueOf(optionCategory.isTickModelView())
                                    ));
                                }
                        )
                        .pos(
                                middleX - 150/2,
                                cursorBoundsY + 30
                        )
                        .size(70,15)
                        .tooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.tick.tooltip")))
                        .build()
        );
        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "visor.overlay.options.pose.aimed",
                                        String.valueOf(optionCategory.isAimRotation())
                                ),
                                (p) ->
                                {
                                    optionCategory.setAimRotation(!optionCategory.isAimRotation());
                                    p.setMessage(
                                            Component.translatable(
                                                    "visor.overlay.options.pose.aimed",
                                                    String.valueOf(optionCategory.isAimRotation())
                                            )
                                    );
                                }
                        )
                        .pos(
                                middleX + 5,
                                cursorBoundsY + 30
                        )
                        .size(70,15)
                        .tooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.aimed.tooltip")))
                        .build()
        );


        List<Component> elements = new ArrayList<>();
        List<PoseAnchor> anchorList = Arrays.stream(PoseAnchor.values()).toList();
        for(PoseAnchor anchor : anchorList){
            elements.add(anchor.getName());
        }

        posTypeWidget = DropDownListWidget.builder(elements)
                .pos(middleX - 150/2, cursorBoundsY + 50)
                .size(70,15)
                .setStartIndex(
                        anchorList.indexOf(
                                optionCategory.getPositionAnchor()
                        )
                )
                .setMessage(Component.translatable("visor.overlay.options.pose.posType"))
                .setResponder(it->{
                    optionCategory.setPositionAnchor(
                            anchorList.get(it)
                    );
                }).build();
        this.addRenderableWidget(
                posTypeWidget
        );

        rotationTypeWidget = DropDownListWidget.builder(elements)
                .pos(middleX + 5, cursorBoundsY + 50)
                .size(70,15)
                .setStartIndex(
                        anchorList.indexOf(
                                optionCategory.getRotationAnchor()
                        )
                )
                .setMessage(Component.translatable("visor.overlay.options.pose.rotation_type"))
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
                .pos(middleX - 150/2, cursorBoundsY + 70)
                .size(150,15)
                .setVisibleItems(7)
                .setStartIndex(
                        modificationType == null
                                ? -1
                                : modificationsList.indexOf(modificationType)
                )
                .setMessage(Component.translatable("visor.overlay.options.pose.howModify"))

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
                    cursorBoundsX,
                    cursorBoundsY + 90,
                    width,
                    height - 65
            ).forEach(this::addRenderableWidget);
            addedWidgetSet = true;
        }



    }

    @Override
    public void onRender(@NotNull GuiGraphics guiGraphics, int i, int j, float f) {
        renderBackground(guiGraphics);

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

    }



    @Override
    public void tick() {
        if(demoDisplayed && !demoOverlay.isEnabled()){
            demoOverlay.showDemo(optionCategory.getOwner());
        }else if(!demoDisplayed && demoOverlay.isEnabled()){
            demoOverlay.setEnabled(false);
        }
        if(emulateModelViewCache != demoOverlay.isEmulatingPose()){
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

        Component emuName = Component.translatable("visor.overlay.options.pose.emulation");
        emulationButton.setMessage(
                Component.literal(
                        StringUtils.formatColorCodes(
                                (emulationActive? "&a" :"&c") +emuName.getString()
                        )
                )
        );
    }

}
