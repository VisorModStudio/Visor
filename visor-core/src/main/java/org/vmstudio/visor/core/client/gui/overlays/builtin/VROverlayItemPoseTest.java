package org.vmstudio.visor.core.client.gui.overlays.builtin;

import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionTextures;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.types.OverlayOptionsGeneral;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.PropertyBool;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.PropertyFloat;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoButtonImaged;
import org.vmstudio.visor.api.client.gui.widgets.info.WidgetInfoEditBox;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.core.client.gui.overlays.builtin.settings.VROverlaySettings;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VROverlayItemPoseTest extends VROverlayScreen {
    public static final String ID = "item_pose_test";

    @Getter
    private OverlayOptionsGeneral properties;

    public VROverlayItemPoseTest(@NotNull VisorAddon owner,
                                 @NotNull String id) {
        super(owner, id);
        properties = getOption(OverlayOptionsGeneral.ID, OverlayOptionsGeneral.class);
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {
    }

    @Override
    public void onUpdatePose(float partialTicks) {

    }


    @Override
    public boolean supportsCursor() {
        return false;
    }

    @Override
    public boolean isHudLayer() {
        return false;
    }


    @Override
    protected boolean updateVisibility() {
        if(MC.screen != null){
            return false;
        }
        return MC.player != null;
    }

    @Override
    public boolean isInViewDistance() {
        return true;
    }

    @Override
    public @NotNull Component getName() {
        return Component.literal("ItemPosesTest");
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.literal("Tool to test item poses options");
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createOptions() {
        return List.of(
                new OverlayOptionsGeneral(
                        this,
                        List.of(
                                new PropertyBool(
                                        "active",
                                        false,
                                        Component.literal("Active"),
                                        Component.literal("Inactive"),
                                        new WidgetInfoButtonImaged()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setHighlightHovered(OptionTextures.HOVERED_HIGHLIGHT)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("Is overriding active")))

                                ),
                                new PropertyFloat(
                                        "scale",
                                        1.0f,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("Scale")))

                                ),
                                new PropertyFloat(
                                        "translate_x",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("translate_x")))

                                ),
                                new PropertyFloat(
                                        "translate_y",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("translate_y")))

                                ),
                                new PropertyFloat(
                                        "translate_z",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("translate_z")))

                                ),
                                new PropertyFloat(
                                        "pre_yaw",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("pre_yaw")))

                                ),
                                new PropertyFloat(
                                        "pre_pitch",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("pre_pitch")))

                                ),
                                new PropertyFloat(
                                        "pre_roll",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("pre_roll")))

                                ),
                                new PropertyFloat(
                                        "yaw",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("yaw")))

                                ),
                                new PropertyFloat(
                                        "pitch",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("pitch")))

                                ),
                                new PropertyFloat(
                                        "roll",
                                        0,
                                        Integer.MIN_VALUE,
                                        Integer.MAX_VALUE,
                                        new WidgetInfoEditBox()
                                                .setTexture(OptionTextures.GRAY_TEXTURE)
                                                .setTextColor(VROverlaySettings.TEXT_COLOR)
                                                .setTooltip(Tooltip.create(Component.literal("roll")))

                                )
                        )

                )
        );
    }

}
