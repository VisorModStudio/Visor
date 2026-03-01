package org.vmstudio.visor.api.client.gui.overlays.options.types;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class OverlayOptionsScreenRegion extends OverlayOptionGroup<OverlayOptionsScreenRegion> {

    public static final String ID = "screen_region";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);


    private final Supplier<RenderTarget> targetSupplier;

    private final int screenWidth;
    private final int screenHeight;


    private int regionX;
    private int regionY;
    private int regionWidth;
    private int regionHeight;

    public OverlayOptionsScreenRegion(@NotNull VROverlay owner,
                                      int screenWidth, int screenHeight,
                                      @NotNull Supplier<RenderTarget> targetSupplier,
                                      @NotNull Consumer<OverlayOptionsScreenRegion> defaultSettings){
        super(owner, defaultSettings);
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.targetSupplier = targetSupplier;
    }


    @Override
    public void update(boolean force) {

    }

    @Override
    protected void onLoad(@NotNull Config config){

        setRegionX(
                config.getIntOrDefault(
                        "region_x",
                        0
                )
        );
        setRegionY(
                config.getIntOrDefault(
                        "region_y",
                        0
                )
        );

        setRegionWidth(
                config.getIntOrDefault(
                        "region_width",
                        screenWidth
                )
        );
        setRegionHeight(
                config.getIntOrDefault(
                        "region_height",
                        screenHeight
                )
        );
        changesNotSaved = false;

    }
    @Override
    public void onSave(@NotNull Config config){
        config.set("region_x", regionX);
        config.set("region_y", regionY);
        config.set("region_width", regionWidth);
        config.set("region_height", regionHeight);
    }


    public void setRegionX(int value){
        int oldValue = regionX;
        regionX = Mth.clamp(value, 0, screenWidth);
        if(oldValue != regionX){
            regionWidth = Mth.clamp(regionWidth, 1, screenWidth - regionX);
            changesNotSaved = true;
        }
    }

    public void setRegionY(int value) {
        int oldValue = regionY;
        regionY = Mth.clamp(value, 0, screenHeight);
        if(oldValue != regionY){
            regionHeight = Mth.clamp(regionHeight,1, screenHeight - regionY);
            changesNotSaved = true;
        }
    }

    public void setRegionWidth(int value) {
        int oldValue = regionWidth;
        regionWidth = Mth.clamp(value, 1, screenWidth - regionX);
        if(oldValue != regionWidth){
            changesNotSaved = true;
        }
    }

    public void setRegionHeight(int value) {
        int oldValue = regionHeight;
        regionHeight = Mth.clamp(value,1, screenHeight - regionY);
        if(oldValue != regionHeight){
            changesNotSaved = true;
        }
    }

    @Override
    public boolean supportsCopying() {
        return true;
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return VisorAPI.client().getGuiManager().getOverlayManager().getOptionsScreenFor(
                this
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;

    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}
