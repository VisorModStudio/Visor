package me.phoenixra.visor.core.client.gui.screens.settings.overlays;

import lombok.Getter;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionsScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.widgets.WidgetsFactory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

@Getter
public class OptionsScreenGlobal extends OverlayOptionsScreen<OverlayOptionsGlobal> {
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



    public OptionsScreenGlobal(@NotNull OverlayOptionsGlobal optionCategory,
                               float mainMenuWidth, float mainMenuHeight) {
        super(optionCategory,mainMenuWidth,mainMenuHeight);
    }

    @Override
    protected void init() {
        clearWidgets();

        mouseEdgeX = width - BACKGROUND_WIDTH;
        mouseEdgeY = height - BACKGROUND_HEIGHT;

        mouseEdgeWidth = width - mouseEdgeX;
        mouseEdgeHeight = height - mouseEdgeY;


        this.addRenderableWidget(
                Button.builder(
                                Component.translatable(
                                        "visor.overlaySettings.global.widget.update_options",
                                        optionCategory.getUpdateOptionsType().getName().getString()
                                ),
                                (p) ->
                                {
                                    optionCategory.setUpdateOptionsType(
                                            optionCategory.getUpdateOptionsType().next()
                                    );
                                    p.setMessage(
                                            Component.translatable(
                                                    "visor.overlaySettings.global.widget.update_options",
                                                    optionCategory.getUpdateOptionsType().getName().getString()
                                            )
                                    );
                                }
                        )
                        .pos(
                                mouseEdgeX + (mouseEdgeWidth /2 - 95/2),
                                mouseEdgeY + 50
                        )
                        .size(95,25)
                        .tooltip(Tooltip.create(Component.translatable("visor.overlaySettings.global.widget.update_options.tooltip")))
                        .build()
        );

        addRenderableWidget(
                WidgetsFactory.createFormulaEditor(
                        mouseEdgeX + (mouseEdgeWidth /2 - 95/2),
                        mouseEdgeY + 85,
                        95,20,
                        Component.translatable("visor.overlaySettings.global.widget.overlay_scale"),
                        optionCategory.getFormulaOverlayScale(),
                        AtumColor.WHITE,
                        it->{
                            optionCategory.setFormulaOverlayScale(
                                    it
                            );
                            optionCategory.update(true);
                        }
                )
        );
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
        super.render(guiGraphics, i, j, f);
    }
}
