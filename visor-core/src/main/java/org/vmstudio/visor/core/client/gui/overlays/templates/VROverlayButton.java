package org.vmstudio.visor.core.client.gui.overlays.templates;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsVisibility;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreen;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsButtonTemplate;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

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
        if (heldKeyCode != -1
                && (ClientContext.cursorHandler.getFocusedOverlayScreen() != this
                 || !button.isHovered())) {
            button.forceRelease();
        }

        int x = (width - optionsButtonTemplate.getWidth()) / 2;
        int y = (height - optionsButtonTemplate.getHeight()) / 2;
        int bWidth = optionsButtonTemplate.getWidth();
        int bHeight = optionsButtonTemplate.getHeight();


        button.getWidgetInfo()
                .setTexture(optionsButtonTemplate.getTexture())
                .setFillColor(optionsButtonTemplate.getFillColor())
                .setDynamicTextScale(true)
                .setDynamicTextMaxScale(20)
                .setTextColor(optionsButtonTemplate.getTextColor());
        button.setMessage(Component.translatable(optionsButtonTemplate.getText()));
        button.setPosition(x,y);
        button.setWidth(bWidth);
        button.height = bHeight;
    }

    private void buttonPressed(){
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

    private void buttonReleased(){
        if(heldKeyCode == -1) return;
        InputHelper.releaseKey(heldKeyCode);
        heldKeyCode = -1;
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