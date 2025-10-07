package me.phoenixra.visor.core.client.gui.screens.settings;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.utils.LangHelper;
import me.phoenixra.visor.core.client.settings.VROptionCategory;
import me.phoenixra.visor.core.client.settings.VROptionWidgetType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class VROptionsBaseScreen extends Screen {
    protected final VROptionCategory category;
    protected final Screen previousScreen;


    private final Map<AbstractWidget, VROptionWidgetType> optionWidgets = new HashMap<>();
    private int nextButtonIndex = 0;
    protected boolean initAgain;

    public VROptionsBaseScreen(VROptionCategory category,
                               Screen previousScreen) {
        super(Component.translatable("visor.options."+category.getKey()));
        this.category = category;
        this.previousScreen = previousScreen;
    }

    protected abstract VROptionWidgetType[] getOptionTypes();

    protected abstract OptionWidgetEntry[] getOptionEntries();

    @Override
    protected void init() {
        OptionWidgetEntry[] entries = getOptionEntries();
        if (entries != null && entries.length > 0) {
            initOptionEntries(entries, true);
        }

        VROptionWidgetType[] types = getOptionTypes();
        if (types == null || types.length < 1) return;

        initOptionTypes(types, entries == null || entries.length < 1);
    }

    protected void initOptionEntries(OptionWidgetEntry[] entries, boolean clear) {
        if (clear) {
            this.clearWidgets();
        }
        for (final OptionWidgetEntry entry : entries) {
            var widget = entry.createWidget(this);
            this.addRenderableWidget(widget);
            optionWidgets.put(widget, entry.getOptionType());
        }
        if (clear) {
            addDefaultButtons();
        }
    }
    protected void initOptionTypes(VROptionWidgetType[] options, boolean clear) {
        if (clear) {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        ArrayList<OptionWidgetEntry> result = new ArrayList<>();

        int i = this.nextButtonIndex;
        for (VROptionWidgetType option : options) {
            OptionWidgetPosition optionPos = i % 2 == 0
                    ? OptionWidgetPosition.LEFT
                    : OptionWidgetPosition.RIGHT;

            if (option != VROptionWidgetType.EMPTY) {
                result.add(
                        new OptionWidgetEntry(
                                option,
                                optionPos,
                                (int) Math.floor((float) i / 2.0F),
                                null
                        )
                );

            }
            ++i;
        }
        this.nextButtonIndex = i;

        this.initOptionEntries(
                result.toArray(new OptionWidgetEntry[0]), false
        );
        if (clear) {
            addDefaultButtons();
        }
    }

    @Override
    protected void clearWidgets() {
        super.clearWidgets();
        optionWidgets.clear();
    }
    public VROptionWidgetType getTypeFromWidget(AbstractWidget widget){
        return optionWidgets.get(widget);
    }

    @Override
    public void render(GuiGraphics guiGraphics,
                       int pMouseX, int pMouseY,
                       float pPartialTicks) {
        renderBackground(guiGraphics);
        if (this.initAgain) {
            this.initAgain = false;
            this.init();
        }
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

        guiGraphics.drawCenteredString(this.font, title, this.width / 2, 15, 16777215);

        renderTooltip(guiGraphics, pMouseX, pMouseY);
    }

    private void renderTooltip(GuiGraphics guiGraphics,
                               int pMouseX, int pMouseY) {
        AbstractWidget hovered = null;
        VROptionWidgetType hoveredOption = null;
        for (GuiEventListener child : children()) {
            if (child instanceof AbstractWidget widget
                    && this.isMouseOver(widget, pMouseX, pMouseY)) {
                hovered = widget;
                hoveredOption = getTypeFromWidget(widget);
                break;
            }
        }

        if (hoveredOption == null) return;

        String tooltipLang = "visor.options."
                + hoveredOption.getKey() + ".tooltip";
        if (!LangHelper.existsLangKey(tooltipLang)) return;

        String tooltip = LangHelper.getText(tooltipLang, (Object) null);

        if (tooltip.isEmpty()) return;

        tooltip = tooltip.replace("\n", "§r\n");
        List<FormattedText> textToCheckSize = font.getSplitter().splitLines(
                tooltip, 310, Style.EMPTY
        );
        tooltip +=
                " ".repeat((310 - (textToCheckSize.isEmpty()
                        ? 0
                        : font.width(textToCheckSize.get(textToCheckSize.size() - 1))))
                        / font.width(" ")
                );

        // if tooltip is not too low, draw below button, otherwise above
        if (hovered.getY() + hovered.getHeight()
                + textToCheckSize.size() * (font.lineHeight + 1) + 14
                < this.height) {
            guiGraphics.renderTooltip(this.font,
                    font.split(Component.literal(tooltip), 308),
                    this.width / 2 - 166,
                    hovered.getY() + hovered.getHeight() + 14
            );
        } else {
            guiGraphics.renderTooltip(this.font,
                    font.split(Component.literal(tooltip), 308),
                    this.width / 2 - 166,
                    hovered.getY() - textToCheckSize.size()
                            * (font.lineHeight + 1) + 9
            );
        }
    }


    protected void addDefaultButtons() {
        this.addRenderableWidget(
                new Button.Builder(
                        Component.translatable("gui.back"),
                        (button) -> {
                            ClientContext.settingsHandler.saveOptions();
                            this.minecraft.setScreen(this.previousScreen);
                        }
                ).pos(this.width / 2 + 5, this.height - 30)
                        .size(150, 20)
                        .build()
        );
        this.addRenderableWidget(
                new Button.Builder(
                        Component.translatable("visor.button.load_defaults"),
                        (button) -> {
                            this.loadDefaultSettings();
                            ClientContext.settingsHandler.saveOptions();
                            this.initAgain = true;
                        }
                ).pos(this.width / 2 - 155, this.height - 30)
                        .size(150, 20)
                        .build()
        );
    }

    protected void loadDefaultSettings() {
        for (GuiEventListener child : this.children()) {
            if (!(child instanceof AbstractWidget widget)) {
                continue;
            }
            var optionType = getTypeFromWidget(widget);
            if(optionType == null) continue;
            ClientContext.settingsHandler
                    .loadDefaultOptionValue(
                            optionType.getKey()
                    );
        }
    }


    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode == 256) {
            ClientContext.settingsHandler.saveOptions();
            this.minecraft.setScreen(this.previousScreen);

            return true;
        } else {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    private boolean isMouseOver(AbstractWidget widget, double x, double y) {
        return widget.visible
                && x >= widget.getX()
                && y >= widget.getY()
                && x < (widget.getX() + widget.getWidth())
                && y < (widget.getY() + widget.getHeight());
    }

}
