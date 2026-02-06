package me.phoenixra.visor.core.client.gui.overlays.builtin;

import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.player.pose.PoseAnchor;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VROverlayMovementState extends VROverlayScreen {
    public static final String ID = "movement_state";

    protected final OverlayOptionsPose optionsPose;

    protected static final ResourceLocation RESOURCE = new ResourceLocation(
            "visor:textures/gui/overlays/movement_state.png"
    );
    protected static final int TEX_WIDTH = 162;
    protected static final int TEX_HEIGHT = 212;

    protected static final GuiTexture RUN_STATE = new GuiTexture(
            RESOURCE,
            0, 0,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture SNEAK_STATE = new GuiTexture(
            RESOURCE,
            56, 0,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture CRAWL_STATE = new GuiTexture(
            RESOURCE,
            112, 0,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture CLIMB_STATE = new GuiTexture(
            RESOURCE,
            0, 72,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture SWIM_STATE = new GuiTexture(
            RESOURCE,
            56, 72,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture FALL_FLYING_STATE = new GuiTexture(
            RESOURCE,
            112, 72,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture FLYING_STATE = new GuiTexture(
            RESOURCE,
            0, 144,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );
    protected static final GuiTexture FAST_FLYING_STATE = new GuiTexture(
            RESOURCE,
            56, 144,
            50, 50,
            TEX_WIDTH, TEX_HEIGHT
    );

    public VROverlayMovementState(@NotNull VisorAddon owner,
                                  @NotNull String id) {
        super(owner, id);
        optionsPose = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);
        setEnabled(true);
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics,
                            int mouseX, int mouseY,
                            float partialTicks) {

        var player = MC.player;

        GuiTexture stateTexture = null;

        if (player.isSprinting()) {
            stateTexture = RUN_STATE;
        }
        if (player.isShiftKeyDown()) {
            stateTexture = SNEAK_STATE;
        }
        if (player.isSwimming()) {
            stateTexture = SWIM_STATE;
        }
        if(ClientContext.localPlayer.isCrawling()){
            stateTexture = CRAWL_STATE;
        }

        if(player.getAbilities().flying){
            if (player.isSprinting()) {
                stateTexture = FAST_FLYING_STATE;
            }else {
                stateTexture = FLYING_STATE;
            }
        }
        if (player.isFallFlying()) {
            stateTexture = FALL_FLYING_STATE;
        }
        if(ClientContext.localPlayer.isClimbing()){
            stateTexture = CLIMB_STATE;
        }

        if (stateTexture != null) {
            stateTexture.blit(
                    guiGraphics,
                    0,0,
                    256,256
            );
        }

    }

    @Override
    public void onUpdatePose(float partialTicks) {
        VROverlayHelper.applyPose(
                this,
                optionsPose.getPositionAnchor(),
                optionsPose.getRotationAnchor(),
                optionsPose.getScale(),
                optionsPose.isAimedRotation(),
                optionsPose.getPositionOffset(),
                optionsPose.getRotationOffset()
        );
    }


    @Override
    public boolean supportsCursor() {
        return false;
    }

    @Override
    public boolean supportsDepth() {
        return true;
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
    public int getRequestedWidth() {
        return 400;
    }

    @Override
    public int getRequestedHeight() {
        return 400;
    }

    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
    }

    @Override
    protected @NotNull List<OverlayOptionGroup<?>> createOptions() {
        return List.of(
                new OverlayOptionsPose(
                        this,
                        it-> {
                            it.setTickPose(true);
                            it.setAimedRotation(false);
                            it.setPositionAnchor(PoseAnchor.OFFHAND);
                            it.setPositionOffset(
                                    0.06f,
                                    -0.0178f,
                                    0.2628f
                            );
                            it.setRotationAnchor(PoseAnchor.OFFHAND);
                            it.setRotationOffset(
                                    0f,
                                    (float) Math.PI/2,
                                    0f
                            );
                            it.setScale(0.05f);
                        }

                )
        );
    }

}
