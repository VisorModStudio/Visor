package org.vmstudio.visor.core.client.gui.screens.overlayoptions.pose;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.helpers.GuiHelper;
import org.vmstudio.visor.api.client.gui.helpers.TexturesHelper;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoValueDrag;
import org.vmstudio.visor.api.client.gui.widgets.sets.DynamicWidgetSet;
import org.vmstudio.visor.api.client.gui.widgets.sets.ValueEditorFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PoseEditorWidgetSet extends DynamicWidgetSet {
    private OverlayOptionsPose optionsPose;
    private int startX;
    private int startY;

    private GuiTexture BACKGROUND = TexturesHelper.getColorGuiTexture(AtumColor.immutable(75,75,75,255));


    private ButtonImaged positionButton;
    private ButtonImaged rotationButton;
    private ButtonImaged scaleButton;


    @Getter
    private ValueEditorFloat xPositionEditor;
    @Getter
    private ValueEditorFloat yPositionEditor;
    @Getter
    private ValueEditorFloat zPositionEditor;

    @Getter
    private ValueEditorFloat xRotationEditor;
    @Getter
    private ValueEditorFloat yRotationEditor;
    @Getter
    private ValueEditorFloat zRotationEditor;

    private float lastShownRotX, lastShownRotY, lastShownRotZ;

    @Getter
    private ValueEditorFloat scaleEditor;

    @Getter
    private EditorType editorType = EditorType.POSITION;

    public PoseEditorWidgetSet(int startX, int startY,
                               @NotNull OverlayOptionsPose optionsPose,
                               @NotNull Runnable onWidgetsChanged) {
        super(onWidgetsChanged);
        this.optionsPose = optionsPose;
        this.startX = startX;
        this.startY = startY;
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> initWidgets() {
        positionButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX+3,startY+3)
                        .size(40,13)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT)
                        .setHighlightThickness(0.9f)
                        .setText(Component.translatable("visor.overlay.options.pose.position"))
                        .setDynamicTextScale(true),
                (it)->{
                    selectEditor(EditorType.POSITION);
                }
        );
        positionButton.setSelected(true);

        rotationButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX+53,startY+3)
                        .size(40,13)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT)
                        .setText(Component.translatable("visor.overlay.options.pose.rotation"))
                        .setDynamicTextScale(true),
                (it)->{
                    selectEditor(EditorType.ROTATION);
                }
        );
        scaleButton = new ButtonImaged(
                new WidgetInfoButtonImaged()
                        .pos(startX+104,startY+3)
                        .size(40,13)
                        .setTexture(OptionTextures.BLACK_TEXTURE)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT,
                                OptionTextures.SELECTED_HIGHLIGHT)
                        .setText(Component.translatable("visor.overlay.options.pose.scale"))
                        .setDynamicTextScale(true),
                (it)->{
                    selectEditor(EditorType.SCALE);
                }
        );


        // --- Position
        xPositionEditor = new ValueEditorFloat.Builder(
                optionsPose.getPositionOffset().x,
                startX+24, startY+31,
                93, 13
        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(optionsPose::setPositionOffsetX).build();
        xPositionEditor.initWidgets();

        yPositionEditor = new ValueEditorFloat.Builder(
                optionsPose.getPositionOffset().y,
                startX+24, startY+55,
                93, 13

        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(optionsPose::setPositionOffsetY).build();
        yPositionEditor.initWidgets();

        zPositionEditor = new ValueEditorFloat.Builder(
                optionsPose.getPositionOffset().z,
                startX+24, startY+79,
                93, 13

        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(optionsPose::setPositionOffsetZ).build();
        zPositionEditor.initWidgets();

        // --- Rotation
        lastShownRotX = 0f;
        lastShownRotY = 0f;
        lastShownRotZ = 0f;

        xRotationEditor = new ValueEditorFloat.Builder(
                0f,
                startX+24, startY+31,
                93, 13

        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(newValue -> {
            float delta = newValue - lastShownRotX;
            lastShownRotX = newValue;
            optionsPose.rotateLocalX(delta);
        }).build();
        xRotationEditor.initWidgets();

        yRotationEditor = new ValueEditorFloat.Builder(
                0f,
                startX+24, startY+55,
                93, 13

        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(newValue -> {
            float delta = newValue - lastShownRotY;
            lastShownRotY = newValue;
            optionsPose.rotateLocalY(delta);
        }).build();
        yRotationEditor.initWidgets();

        zRotationEditor = new ValueEditorFloat.Builder(
                0f,
                startX+24, startY+79,
                93, 13

        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(newValue -> {
            float delta = newValue - lastShownRotZ;
            lastShownRotZ = newValue;
            optionsPose.rotateLocalZ(delta);
        }).build();
        zRotationEditor.initWidgets();
        // ---- Scale

        scaleEditor = new ValueEditorFloat.Builder(
                optionsPose.getScale(),
                startX+24, startY+55,
                93, 13

        ).editBox(
                new WidgetInfoEditBox()
                        .setTexture(OptionTextures.BLACK_TEXTURE)
        ).leftArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_LEFT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).rightArrow(
                new WidgetInfoValueDrag()
                        .setTexture(OptionTextures.ARROW_BLACK_RIGHT)
                        .highlight(OptionTextures.HOVERED_HIGHLIGHT, OptionTextures.HOVERED_HIGHLIGHT)
                        .setStep(0.002)
        ).setResponder(optionsPose::setScale).build();
        scaleEditor.initWidgets();

        return getWidgets();
    }

    @Override
    public <T extends GuiEventListener
            & Renderable
            & NarratableEntry> List<T> getWidgets() {
        List<T> widgets = new ArrayList<>();
        widgets.add((T) positionButton);
        widgets.add((T) rotationButton);
        widgets.add((T) scaleButton);
        switch (editorType){
            case POSITION -> {
                widgets.addAll(xPositionEditor.getWidgets());
                widgets.addAll(yPositionEditor.getWidgets());
                widgets.addAll(zPositionEditor.getWidgets());
            }
            case ROTATION -> {
                widgets.addAll(xRotationEditor.getWidgets());
                widgets.addAll(yRotationEditor.getWidgets());
                widgets.addAll(zRotationEditor.getWidgets());
            }
            case SCALE -> {
                widgets.addAll(scaleEditor.getWidgets());
            }
        }
        return widgets;
    }

    @Override
    public void onPreRender(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        BACKGROUND.blit(
                guiGraphics,
                startX, startY,
                147, 101
        );
        if(editorType != EditorType.SCALE) {
            GuiHelper.renderScalableText(
                    guiGraphics,
                    Minecraft.getInstance().font,
                    "X",
                    AtumColor.LIGHT_GRAY.asInt(),
                    startX + 24, startY + 23,
                    93, 8,
                    true
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    Minecraft.getInstance().font,
                    "Y",
                    AtumColor.LIGHT_GRAY.asInt(),
                    startX + 24, startY + 47,
                    93, 8,
                    true
            );
            GuiHelper.renderScalableText(
                    guiGraphics,
                    Minecraft.getInstance().font,
                    "Z",
                    AtumColor.LIGHT_GRAY.asInt(),
                    startX + 24, startY + 71,
                    93, 8,
                    true
            );
        }
    }

    @Override
    public void onTick() {
        switch (editorType){
            case POSITION -> {
                xPositionEditor.onTick();
                yPositionEditor.onTick();
                zPositionEditor.onTick();
            }
            case ROTATION -> {
                xRotationEditor.onTick();
                yRotationEditor.onTick();
                zRotationEditor.onTick();
            }
            case SCALE -> {
                scaleEditor.onTick();
            }
        }
    }


    public void selectEditor(@NotNull EditorType type){
        switch (type){
            case POSITION -> {
                positionButton.setSelected(true);
                rotationButton.setSelected(false);
                scaleButton.setSelected(false);
            }
            case ROTATION -> {
                positionButton.setSelected(false);
                rotationButton.setSelected(true);
                scaleButton.setSelected(false);
            }
            case SCALE -> {
                positionButton.setSelected(false);
                rotationButton.setSelected(false);
                scaleButton.setSelected(true);
            }
        }
        editorType = type;
        widgetsChanged();
    }

    public enum EditorType {
        POSITION,
        ROTATION,
        SCALE;

        public Component getName(){
            return Component.translatable("visor.overlay.options.pose.enum.ModificationType."+name());
        }
    }

}
