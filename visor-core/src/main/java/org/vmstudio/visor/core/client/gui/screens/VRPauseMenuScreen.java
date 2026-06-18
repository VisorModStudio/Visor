package org.vmstudio.visor.core.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;
import org.vmstudio.visor.core.client.utils.ClientUtils;

import java.util.ArrayList;
import java.util.List;

public class VRPauseMenuScreen extends Screen {
    private static final Component RETURN_TO_MENU = Component.translatable("menu.returnToMenu");
    private static final Component DISCONNECT = Component.translatable("menu.disconnect");


    private static final int COLUMN_W = 204;
    private static final int BTN_H = 20;
    private static final int BTN_HALF = (COLUMN_W - 4) / 2;
    private static final int GAP = 4;
    private static final int TAB_W = (COLUMN_W - 8) / 3;

    private static final int BTN_QUARTER = (COLUMN_W - 3 * GAP) / 4;
    private static final int BTN_THIRD = (COLUMN_W - 2 * GAP) / 3;

    private static final int LABEL_H = 11;
    private static final int SECTION_GAP = 6;

    private enum Tab{
        MAIN(Component.translatable("visor.screen.pause_menu.tab.main")),
        COMMANDS(Component.translatable("visor.screen.pause_menu.tab.commands")),
        TOOLS(Component.translatable("visor.screen.pause_menu.tab.tools"));

        Component label;

        Tab(Component l) {
            this.label = l;
        }
    }

    private Tab currentTab = Tab.MAIN;

    private final List<int[]> sectionHeaderPos = new ArrayList<>(); // {x, y}
    private final List<String> sectionHeaderTexts = new ArrayList<>();

    public VRPauseMenuScreen() {
        super(Component.translatable("visor.screen.pause_menu.title"));
    }


    @Override
    protected void init() {
        TaskHotBar.setResetData(true);
        boolean hasPerms = this.minecraft.player != null && this.minecraft.player.hasPermissions(2);

        if (this.currentTab == Tab.COMMANDS && !hasPerms) {
            this.currentTab = Tab.MAIN;
        }

        sectionHeaderPos.clear();
        sectionHeaderTexts.clear();

        int cx = this.width / 2;
        int startY = this.height / 2 - totalColumnHeight() / 2;
        int y = startY + 9 + 16;

        int tx = cx - COLUMN_W / 2;
        for (Tab tab : Tab.values()) {
            final Tab t = tab;
            Button btn = Button.builder(tab.label, b -> {
                this.currentTab = t;
                this.rebuildWidgets();
            }).pos(tx + tab.ordinal() * (TAB_W + 4), y).width(TAB_W).build();

            if (t == Tab.COMMANDS && !hasPerms) {
                btn.visible = false;
            } else {
                btn.visible = true;
                btn.active = (this.currentTab != tab);
            }
            addRenderableWidget(btn);
        }

        // dont touch this please 🤞
        y += BTN_H + GAP + 2;
        y = buildContent(cx, y);
        y += 4;

        Component component = this.minecraft.isLocalServer() ? RETURN_TO_MENU : DISCONNECT;
        addRenderableWidget(
                Button.builder(component, b -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, () -> ClientUtils.disconnect(""), true))
                        .pos(cx - COLUMN_W / 2, y).width(COLUMN_W).build()
        );
    }

    private int buildContent(int cx, int y) {
        int left = cx - COLUMN_W / 2;
        int right = left + BTN_HALF + GAP;

        switch (this.currentTab) {
            case MAIN -> {
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.inventory").getString(), left, y,
                        b -> this.minecraft.setScreen(new InventoryScreen(this.minecraft.player))));
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.calibrate_height").getString(), right, y, b -> {
                    VRClientSettings.calibrateHeight();
                    ClientContext.settingsManager.saveOptions();
                }));
                y += BTN_H + GAP;

                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.keyboard").getString(), left, y, b ->{
                                    var accessor = ClientContext.overlayManager.getKeyboardAccessor();
                                    accessor.setVisible(true);
                                    accessor.resetPose();
                                })
                );
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.chat").getString(), right, y,
                        b -> this.minecraft.setScreen(new ChatScreen(""))));
                y += BTN_H + GAP;

                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.pause_menu").getString(), left, y,
                        b -> this.minecraft.setScreen(new PauseScreen(true))));
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.vr_settings").getString(), right, y,
                        b -> this.minecraft.setScreen(new VRSettingsScreen(this))));
                y += BTN_H + GAP;
            }

            case COMMANDS -> {
                registerSection(left, y, Component.translatable("visor.screen.pause_menu.section.game_mode").getString());
                y += LABEL_H;

                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.survival").getString(), left, y, b -> sendCommand("gamemode survival")));
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.creative").getString(), right, y, b -> sendCommand("gamemode creative")));
                y += BTN_H + GAP;

                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.spectator").getString(), left, y, b -> sendCommand("gamemode spectator")));
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.adventure").getString(), right, y, b -> sendCommand("gamemode adventure")));
                y += BTN_H + GAP;

                y += SECTION_GAP;

                registerSection(left, y, Component.translatable("visor.screen.pause_menu.section.time").getString());
                y += LABEL_H;

                int[] timeTicks = {0, 6000, 12000, 18000};
                String[] timeLabels = {
                        Component.translatable("visor.screen.pause_menu.button.dawn").getString(), //"Dawn",
                        Component.translatable("visor.screen.pause_menu.button.noon").getString(), //"Noon",
                        Component.translatable("visor.screen.pause_menu.button.dusk").getString(), //"Dusk",
                        Component.translatable("visor.screen.pause_menu.button.night").getString() //"Night"};
                };
                for (int i = 0; i < 4; i++) {
                    final int tick = timeTicks[i];
                    addRenderableWidget(Button.builder(Component.literal(timeLabels[i]),
                                    b -> sendCommand("time set " + tick))
                            .pos(left + i * (BTN_QUARTER + GAP), y)
                            .width(BTN_QUARTER)
                            .build());
                }
                y += BTN_H + GAP;

                y += SECTION_GAP;

                registerSection(left, y, Component.translatable("visor.screen.pause_menu.section.weather").getString());
                y += LABEL_H;

                String[] weatherLabels = {
                        Component.translatable("visor.screen.pause_menu.button.clear").getString(), //"Clear",
                        Component.translatable("visor.screen.pause_menu.button.rain").getString(), //"Rain",
                        Component.translatable("visor.screen.pause_menu.button.thunder").getString(), //"Thunder"
                };
                String[] weatherCmds = {"weather clear", "weather rain", "weather thunder"};
                for (int i = 0; i < 3; i++) {
                    final String cmd = weatherCmds[i];
                    addRenderableWidget(Button.builder(Component.literal(weatherLabels[i]), b -> sendCommand(cmd))
                            .pos(left + i * (BTN_THIRD + GAP), y)
                            .width(BTN_THIRD)
                            .build());
                }
                y += BTN_H + GAP;
            }

            case TOOLS -> {
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.hitboxes").getString(), left, y, b -> {
                    boolean cur = this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                    this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(!cur);
                }));
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.screen.pause_menu.button.chunk_borders").getString(), right, y,
                        b -> this.minecraft.debugRenderer.switchRenderChunkborder()));
                y += BTN_H + GAP;

                addRenderableWidget(makeHalfBtn(Component.translatable("visor.menu.game_menu.button.reload_chunks").getString(), left, y,
                        b -> this.minecraft.levelRenderer.allChanged()));
                addRenderableWidget(makeHalfBtn(Component.translatable("visor.menu.game_menu.button.clear_chat").getString(), right, y,
                        b -> this.minecraft.gui.getChat().clearMessages(false)));
                y += BTN_H + GAP;
            }
        }

        return y;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        super.renderBackground(gfx);

        int cx = this.width / 2;
        int startY = this.height / 2 - totalColumnHeight() / 2;

        gfx.drawCenteredString(this.font, Component.translatable("visor.menu.game_menu.title"), cx, startY, 0xFFFFFFFF);

        int dividerColor = 0xFF555555;

        int divY = startY + 9 + 10;
        gfx.fill(cx - COLUMN_W / 2, divY, cx + COLUMN_W / 2, divY + 1, dividerColor);

        int tabStripY = startY + 9 + 16;
        int accentX = cx - COLUMN_W / 2 + currentTab.ordinal() * (TAB_W + 4);
        gfx.fill(accentX, tabStripY - 2, accentX + TAB_W, tabStripY - 1, 0xFF55FF55);

        if (this.currentTab == Tab.COMMANDS) {
            for (int i = 0; i < sectionHeaderPos.size(); i++) {
                int sx = sectionHeaderPos.get(i)[0];
                int sy = sectionHeaderPos.get(i)[1];

                int lblW = this.font.width(sectionHeaderTexts.get(i));
                int lineY = sy + 4;

                gfx.fill(sx, lineY, sx + 18, lineY + 1, dividerColor);
                gfx.fill(sx + 22 + lblW, lineY, sx + COLUMN_W, lineY + 1, dividerColor);

                gfx.drawString(this.font, sectionHeaderTexts.get(i), sx + 20, sy, 0xFF88FF88, false);
            }
        }

        super.render(gfx, mouseX, mouseY, delta);
    }

    private int totalColumnHeight() {
        int contentH = switch (this.currentTab) {
            case MAIN -> 3 * (BTN_H + GAP);
            case TOOLS -> 2 * (BTN_H + GAP);
            case COMMANDS -> 3 * LABEL_H + 4 * (BTN_H + GAP) + 2 * SECTION_GAP;
        };
        return 9 + 16 + BTN_H + 6 + contentH + 6 + BTN_H;
    }

    private void registerSection(int x, int y, String text) {
        sectionHeaderPos.add(new int[]{x, y});
        sectionHeaderTexts.add(text);
    }

    private Button makeHalfBtn(String label, int x, int y, Button.OnPress action) {
        return Button.builder(Component.literal(label), action)
                .pos(x, y).width(BTN_HALF).build();
    }

    private Button makeFullBtn(String label, int x, int y, Button.OnPress action) {
        return Button.builder(Component.literal(label), action)
                .pos(x, y).width(COLUMN_W).build();
    }

    private void sendCommand(String command) {
        if (this.minecraft.player != null) {
            this.minecraft.player.connection.sendCommand(command);
        }
    }
}
