package me.phoenixra.visor.api.client.gui.overlay.template.options;

import lombok.Getter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

public abstract class OverlayOptionsScreen<T extends OverlayOptions> extends Screen {
    @Getter
    protected final T optionCategory;

    protected final float mainMenuWidth;
    protected final float mainMenuHeight;

    @Getter
    protected int mouseEdgeX = -1;
    @Getter
    protected int mouseEdgeY = -1;
    @Getter
    protected int mouseEdgeWidth = -1;
    @Getter
    protected int mouseEdgeHeight = -1;
    protected OverlayOptionsScreen(@NotNull T optionCategory,
                                   float mainMenuWidth, float mainMenuHeight) {
        super(Component.empty());
        this.optionCategory = optionCategory;
        this.mainMenuWidth = mainMenuWidth;
        this.mainMenuHeight = mainMenuHeight;
    }

    public abstract Vector3fc getPositionOffset();
    public abstract Vector3fc getRotationOffset();

}
