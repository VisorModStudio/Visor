package org.vmstudio.visor.core.client.gui.overlays.templates;

import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.client.gui.widgets.ButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.input.InputHelper;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import org.vmstudio.visor.api.client.gui.overlays.framework.template.VROverlayTemplateScreen;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.gui.overlays.options.OverlayOptionsKeyButton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVROverlayTemplate(
        id = VROverlayKeyButton.ID,
        name = VROverlayKeyButton.NAME,
        description = VROverlayKeyButton.DESCRIPTION
)
public class VROverlayKeyButton extends VROverlayTemplateScreen {

    public static final String ID = "key_button";
    public static final String NAME = "visor.overlay.template." + ID + ".name";
    public static final String DESCRIPTION = "visor.overlay.template." + ID + ".description";

    private final OverlayOptionsKeyButton optionsKeyButton;

    private ButtonImaged button;

    private int heldKeyCode = -1;

    public VROverlayKeyButton(@NotNull VisorAddon owner,
                              @NotNull String id) {
        super(owner, id);
        optionsKeyButton = getOption(
                OverlayOptionsKeyButton.ID,
                OverlayOptionsKeyButton.class
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

        int x = (width - optionsKeyButton.getWidth()) / 2;
        int y = (height - optionsKeyButton.getHeight()) / 2;
        int bWidth = optionsKeyButton.getWidth();
        int bHeight = optionsKeyButton.getHeight();


        button.getWidgetInfo()
                .setTexture(optionsKeyButton.getTexture())
                .setFillColor(optionsKeyButton.getFillColor())
                .setDynamicTextScale(true)
                .setDynamicTextMaxScale(20)
                .setTextColor(optionsKeyButton.getTextColor());
        button.setMessage(Component.translatable(optionsKeyButton.getText()));
        button.setPosition(x,y);
        button.setWidth(bWidth);
        button.height = bHeight;
    }

    private void buttonPressed(){
        String key = optionsKeyButton.getKey();
        if(key.length() == 1
                && InputHelper.sendChar(key.charAt(0), 0)){
            return;
        }

        int keyCode = optionsKeyButton.getKeyCode();
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
        return MC.screen == null || !optionsKeyButton.isWorldOnly();
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
                new OverlayOptionsKeyButton(
                        this,
                        it -> {
                            it.setWidth(200);
                            it.setHeight(200);
                            it.setKey("e");
                            it.setText("Key");
                            it.setCustomizationType(OverlayOptionsKeyButton.CustomizationType.COLOR);
                            it.setColor(AtumColor.DARK_GRAY);
                            it.setTextColor(AtumColor.WHITE);
                            it.setTexturePath(VisorAddon.MISSING_ICON.getResourceLocation().getPath());
                        }
                )
        );
    }
}