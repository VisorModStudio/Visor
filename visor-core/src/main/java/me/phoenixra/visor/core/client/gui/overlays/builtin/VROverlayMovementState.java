package me.phoenixra.visor.core.client.gui.overlays.builtin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.player.pose.PoseAnchor;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VROverlayMovementState extends VROverlayScreen {
    public static final String ID = "movement_state";

    protected final OverlayOptionsPose optionsPose;

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

        MobEffect mobEffect = null;

        if (player.isSprinting()) {
            mobEffect = MobEffects.MOVEMENT_SPEED;
        }
        else if (player.isShiftKeyDown()) {
            mobEffect = MobEffects.BLINDNESS;
        }
        else if (player.isVisuallySwimming()) {
            mobEffect = MobEffects.DOLPHINS_GRACE;
        }
        else if (player.isFallFlying()) {
            mobEffect = MobEffects.SLOW_FALLING;
        }

        if (mobEffect != null) {
            TextureAtlasSprite textureatlassprite = MC.getMobEffectTextures().get(mobEffect);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            guiGraphics.blit(0, 0, 0, 256, 256, textureatlassprite);
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
