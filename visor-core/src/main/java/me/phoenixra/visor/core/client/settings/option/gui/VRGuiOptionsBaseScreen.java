package me.phoenixra.visor.core.client.settings.option.gui;

import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.settings.lang.LangHandler;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionButton;
import me.phoenixra.visor.core.client.settings.option.gui.elements.VRGuiOptionElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;


public abstract class VRGuiOptionsBaseScreen extends Screen {
    protected final Screen previousScreen;

    private int nextButtonIndex = 0;
    protected boolean initAgain;

    public VRGuiOptionsBaseScreen(Screen previousScreen, Component title) {
        super(title);
        this.previousScreen = previousScreen;
    }

    protected abstract VRGuiOption[] getOptionTypes();

    protected abstract VRGuiOptionEntry[] getOptionEntries();

    @Override
    protected void init() {
        VRGuiOptionEntry[] entries = getOptionEntries();
        if (entries != null && entries.length > 0) {
            initOptionEntries(entries, true);
        }

        VRGuiOption[] types = getOptionTypes();
        if (types == null || types.length < 1) return;

        initOptionTypes(types, entries == null || entries.length < 1);
    }

    protected void initOptionEntries(VRGuiOptionEntry[] entries, boolean clear) {
        if (clear) {
            this.clearWidgets();
        }
        for (final VRGuiOptionEntry entry : entries) {
            this.addRenderableWidget(entry.asGuiElement(this));
        }
        if (clear) {
            addDefaultButtons();
        }
    }
    protected void initOptionTypes(VRGuiOption[] options, boolean clear) {
        if (clear) {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        ArrayList<VRGuiOptionEntry> result = new ArrayList<>();

        int i = this.nextButtonIndex;
        for (VRGuiOption option : options) {
            VRGuiOptionPosition optionPos = i % 2 == 0
                    ? VRGuiOptionPosition.LEFT
                    : VRGuiOptionPosition.RIGHT;

            if (option != VRGuiOption.NONE) {
                result.add(
                        new VRGuiOptionEntry(
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
                result.toArray(new VRGuiOptionEntry[0]), false
        );
        if (clear) {
            addDefaultButtons();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics,
                       int pMouseX, int pMouseY,
                       float pPartialTicks) {
        if (this.initAgain) {
            this.initAgain = false;
            VRGuiOption selected = this.getFocused()
                    instanceof VRGuiOptionButton option
                    ? option.getGuiOptionType()
                    : null;
            this.init();
            if (selected != null) {
                List<?> items = this.children().stream()
                        .filter(listener ->
                                listener instanceof VRGuiOptionButton button
                                        && button.getGuiOptionType() == selected
                        ).toList();
                if (!items.isEmpty()) {
                    this.setFocused((GuiEventListener) items.get(0));
                }
            }
        }
        super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

        guiGraphics.drawCenteredString(this.font, title, this.width / 2, 15, 16777215);

        renderTooltip(guiGraphics, pMouseX, pMouseY);
    }

    private void renderTooltip(GuiGraphics guiGraphics,
                               int pMouseX, int pMouseY) {
        GuiEventListener hovered = null;
        for (GuiEventListener child : children()) {
            if (child instanceof AbstractWidget widget
                    && this.isMouseOver(widget, pMouseX, pMouseY)) {
                hovered = child;
                break;
            }
        }

        if (!(hovered instanceof VRGuiOptionElement guiHover)
                || guiHover.getGuiOptionType() == null) return;

        String tooltipLang = "visor.option."
                + guiHover.getGuiOptionType().name() + ".tooltip";
        if (!LangHandler.existsLangKey(tooltipLang)) return;

        String tooltip = LangHandler.getText(tooltipLang, (Object) null);

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
        if (guiHover.getY() + guiHover.getHeight()
                + textToCheckSize.size() * (font.lineHeight + 1) + 14
                < this.height) {
            guiGraphics.renderTooltip(this.font,
                    font.split(Component.literal(tooltip), 308),
                    this.width / 2 - 166,
                    guiHover.getY() + guiHover.getHeight() + 14
            );
        } else {
            guiGraphics.renderTooltip(this.font,
                    font.split(Component.literal(tooltip), 308),
                    this.width / 2 - 166,
                    guiHover.getY() - textToCheckSize.size()
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
            if (!(child instanceof VRGuiOptionElement optionButton)) {
                continue;
            }
            ClientContext.settingsHandler
                    .loadDefaultGuiOption(
                            optionButton.getGuiOptionType()
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
