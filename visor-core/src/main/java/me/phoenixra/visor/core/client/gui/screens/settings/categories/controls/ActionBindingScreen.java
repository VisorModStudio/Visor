package me.phoenixra.visor.core.client.gui.screens.settings.categories.controls;

import com.mojang.blaze3d.platform.InputConstants;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.visor.api.client.input.action.ActionBinding;
import me.phoenixra.visor.api.client.input.action.VisorAction;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.provider.openxr.XrProvider;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ActionBindingScreen extends Screen {
    private enum Mode { PICK_PATH, CAPTURE_INPUT }

    private final VRActionsScreen parent;
    private final VRInteractionProfileType profileType;
    private final VisorAction action;
    private final ActionBinding oldPath;
    private final boolean leftHanded;


    private PathList list;
    private Button confirmButton;
    private VRActionIdentifier selectedActionId;
    private Checkbox touchFilter;
    private Checkbox forceFilter;


    private List<VRActionIdentifier> availableActionIds;

    private Mode mode = Mode.PICK_PATH;

    private boolean capturing = false;
    private long captureStart = 0L;


    public ActionBindingScreen(VRActionsScreen parent,
                               VRInteractionProfileType profileType,
                               VisorAction action,
                               ActionBinding currentPath,
                               boolean leftHanded) {
        super(action.getName().copy()
                .append(AtumColor.COLOR_SYMBOL+"7 (").append(Component.translatable("visor.text."+(leftHanded?"leftHanded":"rightHanded"))
                        .append(AtumColor.COLOR_SYMBOL+"7)")));
        this.parent = parent;
        this.profileType = profileType;
        this.action = action;
        this.oldPath = currentPath;
        this.selectedActionId = currentPath.getActionId(leftHanded);
        this.leftHanded = leftHanded;
        this.availableActionIds = action.getSupportedBindingIds(profileType)
                .stream()
                .filter(it->!it.getValue().contains(".touch")
                        && !it.getValue().contains(".force")).toList();


    }

    @Override
    protected void init() {
        clearWidgets();

        var provider = (XrProvider) ClientContext.visor.getVrProvider();
        var inputHandler = provider.getInputHandler();
        inputHandler.setActionListener(null);

        // Layout constants
        int pad = 10;
        int titleY = 15;
        int btnH = 20;
        int modeBtnY = titleY + 10;            // 25
        int halfWidth = (this.width - pad * 3) / 2;
        int listTop = modeBtnY + btnH + pad;  // 55
        int listBottom = this.height - 50;

        // ─── Mode-switch buttons ─────────────────────
        Button pickBtn = Button.builder(Component.translatable("visor.action_binds.pick_path"), b -> {
                    mode = Mode.PICK_PATH;
                    init();
                })
                .bounds(pad, modeBtnY, halfWidth, btnH)
                .build();
        pickBtn.active = (mode != Mode.PICK_PATH);
        this.addRenderableWidget(pickBtn);

        Button capBtn = Button.builder(Component.translatable("visor.action_binds.capture_input"), b -> {
                    mode = Mode.CAPTURE_INPUT;
                    init();
                })
                .bounds(pad * 2 + halfWidth, modeBtnY, halfWidth, btnH)
                .build();
        capBtn.active = (mode != Mode.CAPTURE_INPUT);
        this.addRenderableWidget(capBtn);

        // ─── Center area: list or “Start” button ─────
        if (mode == Mode.PICK_PATH) {
            list = new PathList(this.width, this.height, listTop, listBottom, 20);
            availableActionIds.forEach(list::addEntry);
            list.setRenderTopAndBottom(false);
            list.setRenderBackground(false);

            this.addRenderableWidget(list);

        } else {
            // “Capture Input” mode
            int w  = 100;
            int x  = (this.width - w) / 2;
            int y  = listTop + (listBottom - listTop) / 2 - (btnH / 2);
            this.addRenderableWidget(
                    Button.builder(
                                    Component.translatable(capturing ? "visor.action_binds.capture_input.listening" : "visor.button.start"),
                                    b -> {
                                        capturing = true;
                                        captureStart = System.currentTimeMillis();
                                        init();
                                        inputHandler.setActionListener(
                                                (it)->{
                                                    if(inputHandler.getActionListener() == null){
                                                        return;
                                                    }
                                                    if(captureStart + 500L > System.currentTimeMillis()){
                                                        return;
                                                    }
                                                    if(!availableActionIds.contains(it)){
                                                        return;
                                                    }
                                                    selectedActionId = it;
                                                    capturing = false;
                                                    init();
                                                    ClientContext.inputManager.setPausedActionsTicks(2);
                                                }
                                        );
                                    }
                            )
                            .bounds(x, y, w, btnH)
                            .build()
            );
        }

        // ─── Filters ────────────────────────────
        int radioY = this.height - 25;
        int gap = 20;

        // measure text widths
        Component touchLabel = Component.translatable("visor.action_binds.touch_filter");
        Component forceLabel = Component.translatable("visor.action_binds.force_filter");
        int touchTextW = this.font.width(touchLabel);
        int forceTextW = this.font.width(forceLabel);

        // assume 12 px for the box + 4 px padding between box and text
        int touchW = touchTextW + 12 + 4;
        int forceW = forceTextW + 12 + 4;

        int totalW = touchW + forceW + gap;
        int startX = (this.width - totalW) / 2;

        touchFilter = new FilterCheckBox(
                startX, radioY,
                touchW, btnH,
                touchLabel,
                touchFilter != null && touchFilter.selected()
        );
        this.addRenderableWidget(touchFilter);

        forceFilter = new FilterCheckBox(
                startX + touchW + gap, radioY,
                forceW, btnH,
                forceLabel,
                forceFilter != null && forceFilter.selected()
        );
        this.addRenderableWidget(forceFilter);

        // ─── Footer: Confirm & Back ───────────────────
        int footerY = this.height - 27;
        int btnW    = 80;

        confirmButton = Button.builder(Component.translatable("visor.button.confirm"), b -> {
                    if (!selectedActionId.getValue().isEmpty()) {
                        var newBinding = new ActionBinding(
                                oldPath.getRightHandedId(),
                                oldPath.getLeftHandedId()
                        );
                        newBinding.setActionId(selectedActionId, leftHanded);
                        parent.getNewBinds().put(action, newBinding);

                        MC.setScreen(parent);

                    }
                })
                .bounds(pad, footerY, btnW, btnH)
                .build();
        confirmButton.active = !oldPath.getActionId(leftHanded)
                .equals(selectedActionId);
        this.addRenderableWidget(confirmButton);

        this.addRenderableWidget(
                Button.builder(Component.translatable("gui.back"), b ->
                                MC.setScreen(parent)
                        )
                        .bounds(this.width - pad - btnW, footerY, btnW, btnH)
                        .build()
        );
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE) {
            MC.setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gui);

        // Title
        gui.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        if (mode == Mode.CAPTURE_INPUT) {
            int pad = 10;
            int titleY = 15;
            int btnH = 20;
            int modeBtnY = titleY + 10;            // 25
            int listTop = modeBtnY + btnH + pad;  // 55
            int listBottom = this.height - 50;
            int y  = listTop + (listBottom - listTop) / 2 - (btnH / 2) - 40;
            gui.drawCenteredString(
                    this.font,
                    Component.translatable("visor.action_binds.capture_input.guide"),
                    this.width / 2,
                    y,
                    0xFFFFFF
            );
        }

        // “Selected path”
        gui.drawCenteredString(
                this.font,
                Component.translatable("visor.action_binds.selected_path", selectedActionId.getValue()),
                this.width / 2,
                this.height - 40, 0xAAAAAA
        );

        if(mode == Mode.PICK_PATH){
            list.renderBackground(gui);
        }
        super.render(gui, mouseX, mouseY, partialTicks);
    }

    private class FilterCheckBox extends Checkbox{

        public FilterCheckBox(int i, int j, int k, int l, Component component, boolean bl) {
            super(i, j, k, l, component, bl);
        }

        @Override
        public void onPress() {
            super.onPress();
            availableActionIds = action.getSupportedBindingIds(profileType)
                    .stream()
                    .filter(it-> (touchFilter.selected() || !it.getValue().contains(".touch"))
                            && (forceFilter.selected() || !it.getValue().contains(".force"))).toList();
            init();
        }
    }

    private class PathList extends ObjectSelectionList<PathList.Entry> {
        public PathList(int width, int height, int top, int bottom, int itemHeight) {
            super(MC, width, height, top, bottom, itemHeight);
        }

        @Override
        protected void renderBackground(GuiGraphics guiGraphics) {

            guiGraphics.fill(
                    this.x0, this.y0,
                    this.x1, this.y1,
                    AtumColor.BLACK.withAlpha(0.5f).asInt()
            );
        }

        @Override
        protected int getScrollbarPosition() {
            return this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 40;
        }

        public void addEntry(VRActionIdentifier path) {
            var entry = new Entry(path);
            this.addEntry(entry);
            if(selectedActionId.equals(path)){
                setFocused(entry);
            }
        }

        @Override
        public void setSelected(@Nullable ActionBindingScreen.PathList.Entry entry) {
            super.setSelected(entry);
            if(entry != null) {
                selectedActionId = entry.identifier;
                if(confirmButton != null) {
                    confirmButton.active = !oldPath.getActionId(leftHanded)
                            .equals(selectedActionId);
                }
            }
        }

        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private final VRActionIdentifier identifier;

            public Entry(VRActionIdentifier identifier) {
                this.identifier = identifier;
            }

            @Override
            public void render(GuiGraphics gui,
                               int index, int top, int left, int listWidth, int slotHeight,
                               int mouseX, int mouseY, boolean hovered, float partialTicks) {
                gui.drawString(
                        ActionBindingScreen.this.font,
                        identifier.getValue(),
                        left + 5,
                        top + 2,
                        hovered ? 0xFFFFA0 : 0xFFFFFF
                );
            }

            @Override
            public boolean mouseClicked(double d, double e, int i) {
                return isMouseOver(d,e);
            }

            @Override
            public @NotNull Component getNarration() {
                return Component.empty();
            }
        }
    }
}
