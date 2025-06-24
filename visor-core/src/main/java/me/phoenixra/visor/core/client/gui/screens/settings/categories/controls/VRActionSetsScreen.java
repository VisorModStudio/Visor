package me.phoenixra.visor.core.client.gui.screens.settings.categories.controls;

import com.mojang.blaze3d.platform.InputConstants;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRActionSetsScreen extends Screen {

    private final Screen previousScreen;

    private ActionSetsList list;

    public VRActionSetsScreen(Screen previous) {
        super(Component.translatable("visor.option.screen.actionSets"));
        this.previousScreen = previous;
    }

    @Override
    protected void init() {
        super.init();

        //Sets
        List<VisorActionSet> sets = ClientContext.inputManager
                .getActionSetRegistry()
                .getSortedElements();

        this.list = new ActionSetsList(
                this.width, this.height,
                32, this.height - 32, 24
        );

        int rowWidth = this.list.getRowWidth();
        for (int i = 0; i < sets.size(); i += 2) {
            VisorActionSet left  = sets.get(i);
            VisorActionSet right = (i + 1 < sets.size() ? sets.get(i + 1) : null);
            this.list.children().add(new ActionSetEntry(left, right, rowWidth));
        }
        list.setRenderBackground(false);
        list.setRenderTopAndBottom(false);
        this.addWidget(this.list);

        //Back button
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.back"), btn -> {
                            MC.setScreen(this.previousScreen);
                        })
                        .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                        .build()
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            ClientContext.settingsHandler.saveOptions();
            MC.setScreen(this.previousScreen);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(guiGraphics);

        this.list.renderBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }


    private static class ActionSetsList extends ObjectSelectionList<ActionSetEntry> {
        public ActionSetsList(int width, int height, int top, int bottom, int itemHeight) {
            super(MC, width, height, top, bottom, itemHeight);
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return Math.min(300, this.width - 50);
        }

        @Override
        protected void renderBackground(GuiGraphics guiGraphics) {
            guiGraphics.fill(
                    this.x0, this.y0,
                    this.x1, this.y1,
                    AtumColor.BLACK.withAlpha(0.5f).toInt()
            );
        }
    }


    private class ActionSetEntry extends ObjectSelectionList.Entry<ActionSetEntry> {
        private final Button leftButton, rightButton;

        public ActionSetEntry(VisorActionSet left, @Nullable VisorActionSet right, int rowWidth) {

            int spacing = 5;
            int buttonWidth = (rowWidth - spacing) / 2;
            int buttonH = 20;



            this.leftButton = Button.builder(
                    left.getName().copy().append("..."),
                            b -> MC.setScreen(new VRActionsScreen(left, VRActionSetsScreen.this))
                    )
                    .bounds(0, 0, buttonWidth, buttonH)
                    .build();

            if (right != null) {
                this.rightButton = Button.builder(
                                right.getName().copy().append("..."),
                                b -> MC.setScreen(new VRActionsScreen(right, VRActionSetsScreen.this))
                        )
                        .bounds(0, 0, buttonWidth, buttonH)
                        .build();
            } else {
                this.rightButton = null;
            }
        }

        @Override
        public void render(GuiGraphics gui, int index, int top, int left, int listWidth, int slotHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTicks) {
            int spacing = 5;
            int btnW = leftButton.getWidth();
            int totalW = btnW + (rightButton != null ? btnW + spacing : 0);
            int startX = left + (listWidth - totalW) / 2;

            leftButton.setX(startX);
            leftButton.setY(top);
            leftButton.render(gui, mouseX, mouseY, partialTicks);

            if (rightButton != null) {
                rightButton.setX(startX + btnW + spacing);
                rightButton.setY(top);
                rightButton.render(gui, mouseX, mouseY, partialTicks);
            }
        }

        @Override
        public boolean mouseClicked(double x, double y, int btn) {
            if (leftButton.mouseClicked(x, y, btn))  return true;
            return rightButton != null
                    && rightButton.mouseClicked(x, y, btn);
        }

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }
    }

}
