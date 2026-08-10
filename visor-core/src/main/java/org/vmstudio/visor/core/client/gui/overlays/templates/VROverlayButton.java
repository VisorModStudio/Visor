package org.vmstudio.visor.core.client.gui.overlays.templates;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsVisibility;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.input.action.VRAction;
import org.vmstudio.visor.api.client.input.action.VRActionSet;
import org.vmstudio.visor.api.client.input.action.framework.VRActionButton;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreen;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate.ActionType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVROverlayTemplate(
        id = VROverlayButton.ID,
        name = VROverlayButton.NAME,
        description = VROverlayButton.DESCRIPTION
)
public class VROverlayButton extends VROverlayTemplateScreen {

    public static final String ID = "button";
    public static final String NAME = "visor.overlay.template." + ID + ".name";
    public static final String DESCRIPTION = "visor.overlay.template." + ID + ".description";

    private final OverlayOptionsButtonTemplate optionsButtonTemplate;

    private ButtonImaged button;

    private int heldKeyCode = -1;

    private VRActionButton heldVrAction;
    private boolean vrActionReleasePending;

    public VROverlayButton(@NotNull VisorAddon owner,
                           @NotNull String id) {
        super(owner, id);
        optionsButtonTemplate = getOption(
                OverlayOptionsButtonTemplate.ID,
                OverlayOptionsButtonTemplate.class
        );
        setEnabled(true);

    }

    @Override
    protected void init() {
        super.init();
        buttonReleased();
        button = new ButtonImaged(
                new WidgetInfoButtonImaged(),
                (it)-> buttonPressed(),
                (it)-> buttonReleased()
        );
        addRenderableWidget(button);
    }

    @Override
    protected void onTick() {
        boolean focused = ClientContext.cursorHandler.getFocusedOverlayScreen() == this;

        if (vrActionReleasePending) {
            vrActionReleased();
        }

        if ((heldKeyCode != -1 || heldVrAction != null)
                && (!focused || !button.isHovered())) {
            button.forceRelease();
        }

        int x = (width - optionsButtonTemplate.getWidth()) / 2;
        int y = (height - optionsButtonTemplate.getHeight()) / 2;
        int bWidth = optionsButtonTemplate.getWidth();
        int bHeight = optionsButtonTemplate.getHeight();


        button.getWidgetInfo()
                .setTexture(optionsButtonTemplate.getTexture())
                .setTextureHovered(focused ? optionsButtonTemplate.getHoverTexture() : null)
                .setFillColor(optionsButtonTemplate.getFillColor())
                .setFillColorHovered(focused ? optionsButtonTemplate.getHoverFillColor() : null)
                .setDynamicTextScale(true)
                .setDynamicTextMaxScale(20)
                .setTextColor(optionsButtonTemplate.getTextColor());
        button.setMessage(Component.translatable(optionsButtonTemplate.getText()));
        button.setPosition(x,y);
        button.setWidth(bWidth);
        button.height = bHeight;
    }

    private void buttonPressed(){
        switch (optionsButtonTemplate.getActionType()){
            case KEY -> keyPressed();
            case COMMAND -> executeCommand();
            case OVERLAY_VISIBILITY -> applyOverlayVisibility();
            case VR_ACTION -> vrActionPressed();
        }
    }

    private void buttonReleased(){
        keyReleased();
        vrActionReleased();
    }

    private void keyPressed(){
        String key = optionsButtonTemplate.getKey();
        if(key.length() == 1
                && InputHelper.sendChar(key.charAt(0), 0)){
            return;
        }

        int keyCode = optionsButtonTemplate.getKeyCode();
        if(keyCode == -1) return;

        heldKeyCode = keyCode;
        InputHelper.pressKey(keyCode);
    }

    private void keyReleased(){
        if(heldKeyCode == -1) return;
        InputHelper.releaseKey(heldKeyCode);
        heldKeyCode = -1;
    }

    private void executeCommand(){
        String command = optionsButtonTemplate.getCommand();
        if(command == null) return;
        command = command.trim();
        if(command.startsWith("/")){
            command = command.substring(1).trim();
        }
        if(command.isEmpty()) return;

        var player = MC.player;
        if(player == null || player.connection == null) return;
        player.connection.sendCommand(command);
    }

    private void applyOverlayVisibility(){
        for(var entry : optionsButtonTemplate.getOverlayActions().entrySet()){
            VROverlay target = ClientContext.overlayManager.getOverlay(entry.getKey());
            if(target == null) continue;

            OverlayOptionsVisibility visibility = target.getOption(
                    OverlayOptionsVisibility.ID,
                    OverlayOptionsVisibility.class
            );
            if(visibility == null) continue;

            boolean newVisible = switch (entry.getValue()){
                case SHOW -> true;
                case HIDE -> false;
                case TOGGLE -> !visibility.isVisible();
            };
            visibility.setVisibleRuntime(newVisible);
        }
    }

    private void vrActionPressed(){
        VRActionButton action = resolveVrAction();
        if(action == null) return;

        heldVrAction = action;
        vrActionReleasePending = false;
        action.forcePress();
    }

    private void vrActionReleased(){
        if(heldVrAction == null){
            vrActionReleasePending = false;
            return;
        }
        if(!heldVrAction.isPressed()){
            if(!isVrActionSetActive()){
                heldVrAction = null;
                vrActionReleasePending = false;
                return;
            }
            vrActionReleasePending = true;
            return;
        }
        heldVrAction.forceRelease();
        heldVrAction = null;
        vrActionReleasePending = false;
    }

    @Nullable
    private VRActionButton resolveVrAction(){
        String setId = optionsButtonTemplate.getVrActionSetId();
        if(setId.isEmpty()) return null;

        VRActionSet actionSet = ClientContext.inputManager
                .getActionSetRegistry().getComponent(setId);
        if(actionSet == null) return null;

        VRAction action = actionSet.getAction(optionsButtonTemplate.getVrActionId());
        if(!(action instanceof VRActionButton vrButton)
                || OverlayOptionsButtonTemplate.isMouseAction(action)){
            return null;
        }
        return vrButton;
    }

    private boolean isVrActionSetActive(){
        String setId = optionsButtonTemplate.getVrActionSetId();
        if(setId.isEmpty()) return false;

        VRActionSet active = ClientContext.inputManager.getActiveSet();
        return active != null && active.getId().equals(setId);
    }


    @Override
    protected void onDisable() {
        super.onDisable();
        buttonReleased();
    }

    @Override
    protected void onVisibilityChanged() {
        super.onVisibilityChanged();
        if(!isVisible()){
            buttonReleased();
        }
    }

    @Override
    public boolean updateVisibility() {
        if(optionsButtonTemplate.getActionType() == ActionType.VR_ACTION
                && !optionsButtonTemplate.getVrActionSetId().isEmpty()
                && !isVrActionSetActive()){
            return false;
        }
        return MC.screen == null || !optionsButtonTemplate.isWorldOnly();
    }

    @Override
    public boolean supportsCursor() {
        return true;
    }

    @Override
    public boolean isHudLayer() {
        return false;
    }

    @Override
    public int getRequestedWidth() {
        return 200;
    }

    @Override
    public int getRequestedHeight() {
        return 200;
    }

    @Override
    public int getCursorBoundsX() {
        return button.getX();
    }

    @Override
    public int getCursorBoundsY() {
        return button.getY();
    }

    @Override
    public int getCursorBoundsWidth() {
        return button.getWidth();
    }

    @Override
    public int getCursorBoundsHeight() {
        return button.height;
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createTemplateOptions() {
        return List.of(
                new OverlayOptionsVisibility(
                        this,
                        it -> it.setVisible(true)
                ),
                new OverlayOptionsPose(
                        this,
                        it->{
                            it.setTickPose(true);
                            it.setAimedRotation(false);
                            it.setPositionAnchor(PoseAnchor.HMD);
                            it.setPositionOffset(
                                    0,0f, -0.7f
                            );
                            it.setRotationAnchor(PoseAnchor.HMD);
                            it.setRotationOffset(
                                    0,0,0
                            );
                            it.setScale(0.15f);
                        }
                ),
                new OverlayOptionsButtonTemplate(
                        this,
                        it -> {
                            it.setWidth(200);
                            it.setHeight(200);
                            it.setKey("e");
                            it.setText("Key");
                            it.setCustomizationType(OverlayOptionsButtonTemplate.CustomizationType.COLOR);
                            it.setColor(AtumColor.DARK_GRAY);
                            it.setTextColor(AtumColor.WHITE);
                            it.setTexturePath(VisorAddon.MISSING_ICON.getResourceLocation().getPath());
                        }
                )
        );
    }
}