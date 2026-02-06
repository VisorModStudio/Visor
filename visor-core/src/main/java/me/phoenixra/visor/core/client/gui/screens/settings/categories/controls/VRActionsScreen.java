package me.phoenixra.visor.core.client.gui.screens.settings.categories.controls;

import lombok.Getter;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.api.client.input.action.VisorActionSet;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRActionsScreen extends Screen {


    private static final int MARGIN = 20;
    private static final int BUTTON_HEIGHT = 20;
    private static final int HEADER_H = 20;
    private static final int FOOTER_H = 45;
    private static final int ENTRY_H = 30;
    private static final int GAP  = 5;
    private static final float ACTION_RATIO = 0.30f;
    private static final float BIND_RATIO = 0.35f;

    private final Screen previousScreen;
    private final VisorActionSet actionSet;
    private final VRInteractionProfileType activeProfileType;
    private VRInteractionProfileType currentProfileType;


    private final Map<VRInteractionProfileType, Map<VisorAction, ActionBinding>> newBindings;


    private Button applyButton;
    @Getter
    private ActionList bindingList;

    private double scrollSaved = -1;
    protected VRActionsScreen(VisorActionSet actionSet, Screen previous) {
        super(actionSet.getName());
        this.previousScreen = previous;
        this.actionSet = actionSet;
        this.activeProfileType = ClientContext.inputManager.getActiveProfile();
        this.currentProfileType = activeProfileType != null
                ? activeProfileType
                : VRInteractionProfileType.VALVE_INDEX;

        newBindings = new EnumMap<>(VRInteractionProfileType.class);

        resetNewBinds();
    }

    @Override
    protected void init() {
        clearWidgets();


        // Bindings list
        int selY = MARGIN + this.font.lineHeight + GAP;
        int headerY = selY + BUTTON_HEIGHT + GAP;
        int listTop = headerY + HEADER_H + GAP;
        int listBottom = height - MARGIN - FOOTER_H;

        bindingList = new ActionList(
                MC, width, listBottom - listTop, listTop, listBottom, ENTRY_H
        );
        bindingList.setRenderBackground(false);
        bindingList.setRenderTopAndBottom(false);
        bindingList.refreshEntries();

        addRenderableWidget(bindingList);

        // Profile selector button
        addRenderableWidget(
                CycleButton.<VRInteractionProfileType>builder(p ->
                                 Component.literal(AtumColor.COLOR_SYMBOL+(activeProfileType == currentProfileType
                                         ? "a"+p.name() : "f"+ p.name())))
                        .withValues(getProfiles())
                        .withInitialValue(currentProfileType)
                        .create(
                                MARGIN - GAP, headerY - GAP - BUTTON_HEIGHT,
                                150, BUTTON_HEIGHT,
                                Component.translatable("visor.button.input_profile"),
                                (btn, prof) -> {
                                    currentProfileType = prof;
                                    init();
                                }
                        )
        );

        // Load defaults button
        int buttonY = listBottom + GAP;
        int totalWidth = BUTTON_HEIGHT * 6 * 2 + 20; // two wide buttons
        int startX = (width - totalWidth) / 2;
        addRenderableWidget(
                Button.builder(
                        Component.translatable("visor.button.load_defaults"),
                        b -> {
                            actionSet.loadDefaults(currentProfileType);
                            resetNewBinds();
                            init();
                        }
                ).size(BUTTON_HEIGHT * 6, BUTTON_HEIGHT).pos(startX, buttonY).build()
        );

        // Apply changes button
        applyButton = addRenderableWidget(
                Button.builder(
                        Component.translatable("visor.button.apply_changes"),
                        b -> {
                            getNewBinds().forEach((a, p) -> a.setBinding(currentProfileType, p));
                            actionSet.saveBindings();
                            init();
                        }
                ).size(BUTTON_HEIGHT * 6, BUTTON_HEIGHT).pos(startX + BUTTON_HEIGHT * 6 + 20, buttonY).build()
        );
        updateApplyButton();

        // Back button
        addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.back"),
                                b -> {
                                    ClientContext.settingsHandler.saveOptions();
                                    MC.setScreen(previousScreen);
                                }
                        ).size(80, BUTTON_HEIGHT)
                        .pos(width/2 - 40, height - BUTTON_HEIGHT)
                        .build()
        );
    }

    private List<VRInteractionProfileType> getProfiles() {
        return ClientContext.inputProvider.getSupportedProfileTypes();
    }

    private void updateApplyButton() {
        boolean hasChanges = getNewBinds().keySet().stream()
                .anyMatch(a -> !Objects.equals(
                        a.getBindingOrEmpty(currentProfileType),
                        getNewBinds().get(a)
                ));
        boolean collision = hasBindingCollision();
        applyButton.active = hasChanges && !collision;
    }

    private boolean hasCollision(VRActionIdentifier actionId, boolean leftHanded) {
        var counts = getNewBinds().values().stream()
                .map(b -> b.getActionId(leftHanded))
                .filter(p -> p.equals(actionId))
                .filter(p->!p.getValue().startsWith("vec2")) //ignore this for now, since some actions may use only 1 dimension
                .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
        return counts.values().stream().anyMatch(c -> c > 1);
    }


    private boolean hasBindingCollision() {
        for (boolean left : new boolean[]{false,true}) {
            var counts = getNewBinds().values().stream()
                    .map(b -> b.getActionId(left))
                    .filter(p -> !p.equals(ActionBinding.EMPTY_ID))
                    .filter(p->!p.getValue().startsWith("vec2")) //ignore this for now, since some actions may use only 1 dimension
                    .collect(Collectors.groupingBy(p -> p, Collectors.counting()));
            if (counts.values().stream().anyMatch(c -> c > 1)) return true;
        }
        return false;
    }

    private void resetNewBinds(){
        newBindings.clear();
        for(var profile : VRInteractionProfileType.values()){
            var map = new HashMap<VisorAction, ActionBinding>();
            actionSet.getActions().forEach(a ->
                    map.put(a, a.getBindingOrEmpty(profile))
            );
            newBindings.put(profile, map);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(scrollSaved != -1){
            bindingList.setScrollAmount(scrollSaved);
            scrollSaved = -1;
        }
        // 1) background
        this.renderBackground(guiGraphics);


        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, MARGIN - 10, 0xFFFFFF);

        // 3) panel behind list + headers
        int selY = MARGIN + font.lineHeight + GAP;
        int headerY = selY + BUTTON_HEIGHT + GAP;
        int listBottom = height - MARGIN - FOOTER_H;
        int listLeft = MARGIN;
        int listWidth = width - MARGIN * 2;
        guiGraphics.fill(
                listLeft - GAP, headerY - GAP,
                listLeft + listWidth + GAP, listBottom + GAP,
                0x80000000
        );

        // 4) column headers
        AtumColor activeColor = AtumColor.GREEN.blend(AtumColor.BLACK,0.2f);
        boolean leftHanded = ClientContext.inputManager.isLeftHanded();
        int actionColumn = (int)(listWidth * ACTION_RATIO);
        int bindColumn = (int)(listWidth * BIND_RATIO);
        int headerTextY = headerY + (HEADER_H - font.lineHeight) / 2;
        guiGraphics.drawString(font, Component.translatable("visor.options.controls.action"),
                listLeft + GAP, headerTextY, AtumColor.GRAY.asInt());
        guiGraphics.drawString(font, Component.translatable("visor.options.controls.left_handed"),
                listLeft + actionColumn + GAP, headerTextY,  !leftHanded ? AtumColor.GRAY.asInt() : activeColor.asInt());
        guiGraphics.drawString(font, Component.translatable("visor.options.controls.right_handed"),
                listLeft + actionColumn + bindColumn + GAP, headerTextY, !leftHanded ? activeColor.asInt() : AtumColor.GRAY.asInt());

        // 5) all other widgets
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int key, int sc, int mods) {
        if (key == 256) {
            MC.setScreen(previousScreen);
            return true;
        }
        return super.keyPressed(key, sc, mods);
    }

    public Map<VisorAction, ActionBinding> getNewBinds(){
        return newBindings.get(currentProfileType);
    }
    public class ActionList extends ContainerObjectSelectionList<BindingEntry> {
        public ActionList(Minecraft mc, int w, int h, int top, int bottom, int itemH) {
            super(mc, w, h, top, bottom, itemH);
        }
        public void refreshEntries() {
            clearEntries();
            actionSet.getActions().forEach(a -> addEntry(new BindingEntry(a)));
        }


        @Override
        protected int getScrollbarPosition() {
            return this.width - MARGIN - 3;
        }

        @Override
        public int getRowWidth() {
            return this.width - MARGIN * 2;
        }
    }

    private class BindingEntry extends ContainerObjectSelectionList.Entry<BindingEntry> {
        private final VisorAction action;
        private final Button leftButton, rightButton;

        public BindingEntry(VisorAction action) {
            this.action = action;
            this.leftButton = Button.builder(Component.literal(""),
                    b -> {
                        scrollSaved = bindingList.getScrollAmount();
                        MC.setScreen(new ActionBindingScreen(
                                VRActionsScreen.this,
                                currentProfileType,
                                action,
                                getNewBinds().get(action),
                                true
                        ));
                    })
                    .size(0, BUTTON_HEIGHT).build();
            this.rightButton = Button.builder(Component.literal(""),
                    b -> {
                        scrollSaved = bindingList.getScrollAmount();
                        MC.setScreen(new ActionBindingScreen(
                                VRActionsScreen.this,
                                currentProfileType,
                                action,
                                getNewBinds().get(action),
                                false
                        ));
                    })
                    .size(0, BUTTON_HEIGHT).build();
        }


        @Override
        public void render(GuiGraphics guiGraphics,
                           int idx, int y, int x, int w, int h,
                           int mouseX, int mouseY, boolean hover, float pt) {
            if (hover) {
                guiGraphics.fill(x, y, x + w, y + h, 0x30FFFFFF);
            }

            int actionColumn = (int)(w * ACTION_RATIO) - 3;
            int bindColumn   = (int)(w * BIND_RATIO);

            VRActionIdentifier leftActionId = getNewBinds().get(action).getActionId(true);
            VRActionIdentifier rightActionId = getNewBinds().get(action).getActionId(false);

            // action label
            guiGraphics.drawString(font,
                    action.getName(),
                    x + GAP - 3,
                    y + (h - font.lineHeight) / 2,
                    0xFFFFFF,
                    false);

            // left button
            if(hasCollision(leftActionId, true)){
                leftButton.setMessage(
                        Component.literal(
                                AtumColor.COLOR_SYMBOL+"c"+leftActionId.getValue()
                        )
                );
            }else{
                leftButton.setMessage(Component.literal(leftActionId.getValue()));
            }
            leftButton.setWidth(bindColumn - GAP * 2);
            leftButton.setX(x + actionColumn + GAP);
            leftButton.setY(y + (h - BUTTON_HEIGHT) / 2);
            leftButton.render(guiGraphics, mouseX, mouseY, pt);

            // right button
            if(hasCollision(rightActionId, false)){
                rightButton.setMessage(
                        Component.literal(
                                AtumColor.COLOR_SYMBOL+"c"+rightActionId.getValue()
                        )
                );
            }else{
                rightButton.setMessage(Component.literal(rightActionId.getValue()));
            }
            rightButton.setWidth(bindColumn - GAP * 2);
            rightButton.setX(x + actionColumn + bindColumn + GAP);
            rightButton.setY(leftButton.getY());
            rightButton.render(guiGraphics, mouseX, mouseY, pt);
        }

        @Override public @NotNull List<? extends GuiEventListener> children() {
            return List.of(leftButton, rightButton);
        }
        @Override public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }

}
