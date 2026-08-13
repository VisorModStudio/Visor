package org.vmstudio.visor.core.client.gui.screens.overlayoptions;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.SliderWidget;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoSlider;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlayDemo;
import org.vmstudio.visor.core.client.gui.screens.overlayoptions.pose.OptionsPoseTextures;
import org.vmstudio.visor.core.client.gui.screens.overlayoptions.pose.PoseEditorWidgetSet;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


@Getter
public class OptionsScreenPose extends OptionsScreen<OverlayOptionsPose> {



    @Getter
    private VROverlayDemo demoOverlay = null;



    private ButtonImaged demoButton;
    private ButtonImaged emulateButton;
    private ButtonImaged aimButton;


    private ButtonImaged applyOffsetButton;
    private ButtonImaged teleportButton;
    private ButtonImaged dragButton;

    private SliderWidget<PoseAnchor> positionAnchorSlider;
    private SliderWidget<PoseAnchor> rotationAnchorSlider;


    private PoseEditorWidgetSet poseEditorWidgetSet;

    private boolean demoDisplayed;

    private boolean emulateCache;
    private boolean dragCache;

    public OptionsScreenPose(@NotNull OverlayOptionsPose optionsGroup) {
        super(optionsGroup, Background.VERTICAL_WIDER);
    }

    @Override
    protected void onInit() {
        clearWidgets();

        demoOverlay = ClientContext.overlayManager.getOverlay(
                VROverlayDemo.ID,
                VROverlayDemo.class
        );


        // ----- Top buttons
        demoButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+14, cursorBoundsY+14)
                        .size(40,40)
                        .setTexture(OptionsPoseTextures.BUTTON_DEMO)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.demo.tooltip"))),
                (p) ->
                        setDemonstrating(!demoDisplayed)
        );

        emulateButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+121, cursorBoundsY+14)
                        .size(40,40)
                        .setTexture(OptionsPoseTextures.BUTTON_EMULATE)
                        .setTextureInactive(OptionsPoseTextures.BUTTON_EMULATE_INACTIVE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.emulation.tooltip"))),
                (p) ->
                        setEmulating(!demoOverlay.isEmulatingPose())
        );

        aimButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+70, cursorBoundsY+27)
                        .size(35,35)
                        .setTexture(OptionsPoseTextures.BUTTON_AIM)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.aimed.tooltip"))),
                (p) ->
                        setAimed(!optionsGroup.isAimedRotation())
        );


        // ----- Bottom buttons

        applyOffsetButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+14, cursorBoundsY+218)
                        .size(24,24)
                        .setTexture(OptionsPoseTextures.BUTTON_APPLY_OFFSET)
                        .setTextureInactive(OptionsPoseTextures.BUTTON_APPLY_OFFSET_INACTIVE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.apply_offset.tooltip"))),
                (p) ->
                        applyOffset()
        );

        teleportButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+75, cursorBoundsY+218)
                        .size(24,24)
                        .setTexture(OptionsPoseTextures.BUTTON_TELEPORT)
                        .setTextureInactive(OptionsPoseTextures.BUTTON_TELEPORT_INACTIVE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.teleport.tooltip"))),
                (p) ->
                        teleport()
        );

        dragButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(cursorBoundsX+137, cursorBoundsY+218)
                        .size(24,24)
                        .setTexture(OptionsPoseTextures.BUTTON_DRAG)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.drag.tooltip"))),
                (p) ->
                        setHandDragging(true)
        );



        // ---- Anchor selection

        positionAnchorSlider = new SliderWidget<>(
                new WidgetInfoSlider()
                        .pos(cursorBoundsX+14,cursorBoundsY+79)
                        .size(60,15)
                        .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                        .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.position_anchor.tooltip")))
                        .setDynamicTextScale(true),
                Lists.newArrayList(PoseAnchor.values()),
                (it)->{
                    optionsGroup.setPositionAnchor(it.getSelected());
                    it.setText(
                            Component.translatable(
                                    "visor.overlay.options.pose.position_anchor",
                                    optionsGroup.getPositionAnchor()
                            )
                    );
                }
        );
        positionAnchorSlider.setSelected(
                optionsGroup.getPositionAnchor(),
                true
        );

        rotationAnchorSlider = new SliderWidget<>(
                new WidgetInfoSlider()
                        .pos(cursorBoundsX+99,cursorBoundsY+79)
                        .size(60,15)
                        .setBackgroundTexture(OptionTextures.GRAY_TEXTURE)
                        .setKnobTexture(OptionTextures.LIGHT_GRAY_TEXTURE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT)
                        .setTooltip(Tooltip.create(Component.translatable("visor.overlay.options.pose.rotation_anchor.tooltip")))
                        .setDynamicTextScale(true),
                Lists.newArrayList(PoseAnchor.values()),
                (it)->{
                    optionsGroup.setRotationAnchor(it.getSelected());
                    it.setText(
                            Component.translatable(
                                    "visor.overlay.options.pose.rotation_anchor",
                                    optionsGroup.getRotationAnchor()
                            )
                    );
                }
        );
        rotationAnchorSlider.setSelected(
                optionsGroup.getRotationAnchor(),
                true
        );


        poseEditorWidgetSet = new PoseEditorWidgetSet(
                cursorBoundsX + 14, cursorBoundsY + 106,
                optionsGroup,
                this::repopulateWidgets
        );

        addRenderableWidget(demoButton);
        addRenderableWidget(aimButton);
        addRenderableWidget(emulateButton);

        addRenderableWidget(applyOffsetButton);
        addRenderableWidget(teleportButton);
        addRenderableWidget(dragButton);

        addRenderableWidget(positionAnchorSlider);
        addRenderableWidget(rotationAnchorSlider);

        poseEditorWidgetSet.initWidgets().forEach(this::addRenderableWidget);


        setAimed(optionsGroup.isAimedRotation());
        setDemonstrating(true);
        setEmulating(true);



    }

    @Override
    public void onRender(@NotNull GuiGraphics guiGraphics,
                         int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
        poseEditorWidgetSet.onPreRender(guiGraphics, mouseX, mouseY, partialTicks);
    }



    @Override
    public void tick() {
        if(demoDisplayed && !demoOverlay.isEnabled()){
            demoOverlay.showDemo(optionsGroup.getOwner());
        }else if(!demoDisplayed && demoOverlay.isEnabled()){
            demoOverlay.setEnabled(false);
        }
        if(emulateCache != demoOverlay.isEmulatingPose()){
            setEmulating(demoOverlay.isEmulatingPose());
        }
        if(dragCache != demoOverlay.isMovingByAnchor()){
            setHandDragging(demoOverlay.isMovingByAnchor());
        }
        updateEditors();
        poseEditorWidgetSet.onTick();
        super.tick();
    }




    @Override
    public void removed() {
        demoOverlay.setEnabled(false);
    }

    private void updateEditors(){
        var posOffset = optionsGroup.getPositionOffset();
        var scale = optionsGroup.getScale();

        var xPosEditor = poseEditorWidgetSet.getXPositionEditor();
        var yPosEditor = poseEditorWidgetSet.getYPositionEditor();
        var zPosEditor = poseEditorWidgetSet.getZPositionEditor();

        var scaleEditor = poseEditorWidgetSet.getScaleEditor();

        if(xPosEditor.getValue() != posOffset.x){
            xPosEditor.setValue(posOffset.x, true);
        }
        if(yPosEditor.getValue() != posOffset.y){
            yPosEditor.setValue(posOffset.y, true);
        }
        if(zPosEditor.getValue() != posOffset.z){
            zPosEditor.setValue(posOffset.z, true);
        }

        if(scaleEditor.getValue() != scale){
            scaleEditor.setValue(scale, true);
        }
    }
    public void repopulateWidgets() {
        clearWidgets();
        addRenderableWidget(demoButton);
        addRenderableWidget(emulateButton);
        addRenderableWidget(aimButton);
        addRenderableWidget(applyOffsetButton);
        addRenderableWidget(teleportButton);
        addRenderableWidget(dragButton);
        addRenderableWidget(rotationAnchorSlider);
        addRenderableWidget(positionAnchorSlider);
        poseEditorWidgetSet.getWidgets().forEach(this::addRenderableWidget);
    }


    public void setEmulating(boolean flag){
        boolean emulate = flag;
        emulateCache = emulate;
        demoOverlay.setEmulatingPose(emulate);
        emulateButton.getWidgetInfo().setTexture(
                emulate
                        ? OptionsPoseTextures.BUTTON_EMULATE_ACTIVE
                        : OptionsPoseTextures.BUTTON_EMULATE
        );
        teleportButton.active = !emulateCache;
        applyOffsetButton.active = !emulateCache;
    }
    public void setDemonstrating(boolean flag){
        demoDisplayed = flag;
        demoButton.getWidgetInfo().setTexture(
                demoDisplayed
                        ? OptionsPoseTextures.BUTTON_DEMO_ACTIVE
                        : OptionsPoseTextures.BUTTON_DEMO
        );
        emulateButton.active = demoDisplayed;
        if(!demoDisplayed) {
            setEmulating(false);
        }
    }
    public void setAimed(boolean flag){
        boolean aimed = flag;
        optionsGroup.setAimedRotation(aimed);
        aimButton.getWidgetInfo().setTexture(
                aimed
                        ? OptionsPoseTextures.BUTTON_AIM_ACTIVE
                        : OptionsPoseTextures.BUTTON_AIM
        );
    }
    public void setHandDragging(boolean flag){
        if(!demoDisplayed) {
            return;
        }
        if(flag) {
            demoOverlay.startMovingByAnchor();
        }else{
            demoOverlay.stopMovingByAnchor();
        }
        dragCache = flag;
        dragButton.getWidgetInfo().setTexture(
                dragCache
                        ? OptionsPoseTextures.BUTTON_DRAG_ACTIVE
                        : OptionsPoseTextures.BUTTON_DRAG
        );
    }
    public void applyOffset(){
        if(!demoDisplayed
                || demoOverlay.isEmulatingPose()) {
            return;
        }
        demoOverlay.applyNewOffset();
    }
    public void teleport(){
        if(!demoOverlay.isEmulatingPose()) {
            demoOverlay.teleportToHMD();
        }
    }




}
