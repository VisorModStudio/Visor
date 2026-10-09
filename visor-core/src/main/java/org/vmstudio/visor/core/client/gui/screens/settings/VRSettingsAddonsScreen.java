package org.vmstudio.visor.core.client.gui.screens.settings;

import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import com.mojang.blaze3d.platform.InputConstants;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McScreen;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRSettingsAddonsScreen extends McScreen {

    private final Screen previousScreen;

    private AddonList list;

    public VRSettingsAddonsScreen(Screen previous) {
        super(Component.translatable("visor.options.main.addons"));
        this.previousScreen = previous;
    }

    @Override
    protected void init() {
        super.init();

        List<VisorAddon> addons = ClientContext.addonManager.getAddons().stream().toList();

        this.list = new AddonList(
                this.width, this.height - 64,
                32, 24
        );

        int rowWidth = this.list.getRowWidth();
        VisorAddon leftAddon = null;
        Screen leftScreen = null;
        VisorAddon rightAddon;
        Screen rightScreen;
        for (int i = 0; i < addons.size(); i += 2) {
            if(leftAddon == null) {
                leftAddon = addons.get(i);
                leftScreen = leftAddon.createAddonSettingsScreen(VRSettingsAddonsScreen.this);
                rightAddon = (i + 1 < addons.size() ? addons.get(i + 1) : null);
                if(rightAddon == null){
                    continue;
                }
                rightScreen = rightAddon.createAddonSettingsScreen(VRSettingsAddonsScreen.this);
            }else{
                rightAddon = addons.get(i);
                rightScreen = rightAddon.createAddonSettingsScreen(VRSettingsAddonsScreen.this);
            }

            if(leftScreen == null && rightScreen != null){
                leftScreen = rightScreen;
                leftAddon = rightAddon;
                continue;
            } else if(leftScreen == null){
                leftAddon = null;
                continue;
            } else if (rightScreen == null) {
                continue;
            }
            this.list.addRow(
                    new AddonEntry(
                            leftAddon, leftScreen,
                            rightAddon, rightScreen,
                            rowWidth
                    )
            );
            leftAddon = null;
            leftScreen = null;
        }
        if(leftAddon != null && leftScreen != null){
            this.list.addRow(
                    new AddonEntry(
                            leftAddon, leftScreen,
                            null, null,
                            rowWidth
                    )
            );
        }
        list.setRenderBackground(false);
        McGuiUtils.setRenderTopAndBottom(list, false);
        this.addWidget(this.list);

        //Back button
        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.back"), btn -> {
                            McVersionClientUtils.setScreen(this.previousScreen);
                        })
                        .bounds(this.width / 2 - 100, this.height - 27, 200, 20)
                        .build()
        );
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            ClientContext.settingsManager.saveOptions();
            McVersionClientUtils.setScreen(this.previousScreen);
            return true;
        }
        return super.onKeyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        McGuiUtils.render(this.list, guiGraphics, mouseX, mouseY, partialTicks);

        McGuiUtils.drawCenteredString(guiGraphics, this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        super.renderContents(guiGraphics, mouseX, mouseY, partialTicks);
    }


    private static class AddonList extends McObjectSelectionList<AddonEntry> {
        public AddonList(int width, int height, int top, int itemHeight) {
            super(MC, width, height, 0, top, itemHeight);
        }

        private void addRow(AddonEntry entry) {
            addEntry(entry);
        }

        @Override
        protected int scrollbarX() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return Math.min(300, this.width - 50);
        }

        @Override
        protected void renderContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
            guiGraphics.fill(
                    listLeft(), listTop(),
                    listRight(), listBottom(),
                    AtumColor.BLACK.withAlpha(0.5f).asInt()
            );
            renderDefault(guiGraphics, mouseX, mouseY, partialTick);
        }
    }


    private class AddonEntry extends ObjectSelectionList.Entry<AddonEntry> {
        private final Button leftButton, rightButton;

        public AddonEntry(@NotNull VisorAddon left,
                          @NotNull Screen leftScreen,
                          @Nullable VisorAddon right,
                          @Nullable Screen rightScreen,
                          int rowWidth) {
            int spacing = 5;
            int buttonWidth = (rowWidth - spacing) / 2;
            int buttonH = 20;



            this.leftButton = Button.builder(
                            left.getAddonName().copy().append("..."),
                            b -> McVersionClientUtils.setScreen(leftScreen)
                    )
                    .bounds(0, 0, buttonWidth, buttonH)
                    .build();

            if (right != null) {
                this.rightButton = Button.builder(
                                right.getAddonName().copy().append("..."),
                                b -> McVersionClientUtils.setScreen(rightScreen)
                        )
                        .bounds(0, 0, buttonWidth, buttonH)
                        .build();
            } else {
                this.rightButton = null;
            }
        }

        //? if >=26.1 {
        @Override
        public void extractContent(GuiGraphicsExtractor gui, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            visor$renderRow(gui, getY(), getX(), getWidth(), mouseX, mouseY, partialTicks);
        }

        private void visor$renderRow(GuiGraphicsExtractor gui, int top, int left, int listWidth,
                                     int mouseX, int mouseY, float partialTicks) {
        //?} elif >=1.21.9 {
        /*@Override
        public void renderContent(GuiGraphics gui, int mouseX, int mouseY, boolean hovered, float partialTicks) {
            visor$renderRow(gui, getY(), getX(), getWidth(), mouseX, mouseY, partialTicks);
        }

        private void visor$renderRow(GuiGraphics gui, int top, int left, int listWidth,
                                     int mouseX, int mouseY, float partialTicks) {
        *///?} else {
        /*@Override
        public void render(GuiGraphics gui, int index, int top, int left, int listWidth, int slotHeight,
                           int mouseX, int mouseY, boolean hovered, float partialTicks) {
        *///?}
            int spacing = 5;
            int btnW = leftButton.getWidth();
            int totalW = btnW + (rightButton != null ? btnW + spacing : 0);
            int startX = left + (listWidth - totalW) / 2;

            leftButton.setX(startX);
            leftButton.setY(top);
            McGuiUtils.render(leftButton, gui, mouseX, mouseY, partialTicks);

            if (rightButton != null) {
                rightButton.setX(startX + btnW + spacing);
                rightButton.setY(top);
                McGuiUtils.render(rightButton, gui, mouseX, mouseY, partialTicks);
            }
        }

        //? if >=1.21.9 {
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (leftButton.mouseClicked(event, doubleClick)) return true;
            return rightButton != null
                    && rightButton.mouseClicked(event, doubleClick);
        }
        //?} else {
        /*@Override
        public boolean mouseClicked(double x, double y, int btn) {
            if (leftButton.mouseClicked(x, y, btn))  return true;
            return rightButton != null
                    && rightButton.mouseClicked(x, y, btn);
        }
        *///?}

        @Override
        public @NotNull Component getNarration() {
            return Component.empty();
        }
    }

}
