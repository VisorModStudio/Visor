package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.settings.enums.ScalingFilter;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;
import org.vmstudio.visor.core.client.render.helpers.RenderShaderHelper;
import org.vmstudio.visor.core.client.utils.ClientUtils;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.util.Util;
import net.minecraft.util.Mth;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class VRShaderPostProcessEye implements VRShader{
    private static final float AXIS_NONE = 0.0F;
    private static final float AXIS_HORIZONTAL = 1.0F;
    private static final float AXIS_VERTICAL = 2.0F;

    @Getter
    private McShaderProgram handle;

    private float desaturateProgress;
    private long desaturateLastMillis;

    private float tintRed;
    private float tintBlue;
    private float tintBlack;
    private float desaturate;

    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_post_process_eye", DefaultVertexFormat.POSITION_TEX, false);
    }


    public void finishEye(EyeType eye,
                          RenderTarget source,
                          RenderTarget target,
                          @Nullable RenderTarget resampleTarget,
                          float partialTicks) {
        if (eye == EyeType.LEFT) {
            // update state only for the first rendered eye,
            // to have synchronized effects for both
            updateEffects(partialTicks);
        }

        if (resampleTarget == null) {
            McRenderTarget.bindWrite(target);
            draw(source, target, AXIS_NONE, true);
        } else {
            McRenderTarget.bindWrite(resampleTarget);
            draw(source, resampleTarget, AXIS_HORIZONTAL, false);
            McRenderTarget.bindWrite(target);
            draw(resampleTarget, target, AXIS_VERTICAL, true);
        }

        GLUtils.checkGLError("post process eye: "+ eye.name());
    }

    private void draw(RenderTarget source, RenderTarget target, float axis, boolean effects) {
        handle.setUniform("uTintRed", effects ? tintRed : 0.0F);
        handle.setUniform("uTintBlue", effects ? tintBlue : 0.0F);
        handle.setUniform("uTintBlack", effects ? tintBlack : 0.0F);
        handle.setUniform("uDesaturate", effects ? desaturate : 0.0F);

        handle.setUniform("uScalingAxis", axis);
        handle.setUniform("uScalingFilter", filterIndex(VRClientSettings.getScalingFilter()));
        handle.setUniform("uSourceWidth", (float) McRenderTarget.viewWidth(source));
        handle.setUniform("uSourceHeight", (float) McRenderTarget.viewHeight(source));
        handle.setUniform("uTargetWidth", (float) McRenderTarget.viewWidth(target));
        handle.setUniform("uTargetHeight", (float) McRenderTarget.viewHeight(target));

        RenderShaderHelper.renderFullscreenQuad(handle, source);
    }

    private static float filterIndex(ScalingFilter filter) {
        return switch (filter) {
            case LANCZOS -> 0.0F;
            case MITCHELL -> 1.0F;
            case BILINEAR -> 2.0F;
        };
    }


    private void updateEffects(float partialTicks){

        boolean canApplyEffects = MC.level != null
                && MC.player != null
                && !MC.player.isSpectator();


        float time = (float) Util.getMillis() / 1000.0F;

        float redTint = 0.0F;
        float blueTint = 0.0F;
        float blackTint = 0.0F;

        if (canApplyEffects) {

            // --- Damage & low health effects ---
            if (MC.player.isCreative()) {
                redTint = 0.0F;
            }else{
                float hurtTimer = (float) MC.player.hurtTime - partialTicks;
                float healthPercent = 1.0F - MC.player.getHealth() / MC.player.getMaxHealth();
                healthPercent = (healthPercent - 0.5F) * 0.75F;
                if (VRClientSettings.isHitIndicatorEnabled()
                        && hurtTimer > 0.0F) {
                    // red flash
                    hurtTimer = hurtTimer / (float) MC.player.hurtDuration;
                    hurtTimer = healthPercent +
                            Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * Mth.PI) * 0.5F;
                    redTint = hurtTimer;
                } else if(VRClientSettings.isLowHealthIndicatorEnabled()){
                    //low health red indicator
                    redTint = healthPercent * Mth.abs(Mth.sin((2.5F * time) / (1.0F - healthPercent + 0.1F)));
                }
            }


            // --- Freeze effect ---
            if(VRClientSettings.isFreezeEffectEnabled()) {
                float freeze = MC.player.getPercentFrozen();
                boolean hasFreezeEffect = freeze > 0;
                if (hasFreezeEffect) {
                    blueTint = redTint;
                    blueTint = Math.max(freeze / 2, blueTint);
                    redTint = 0;
                }
            }

            // --- Sleep effect ---
            if (MC.player.isSleeping()) {
                blackTint = 0.5F + 0.3F * MC.player.getSleepTimer() * 0.01F;
            }

        }

        // --- Finalize ---

        //tints
        tintRed = redTint;
        tintBlue = blueTint;
        tintBlack = blackTint;

        //drain the colors while the client in fullscreen
        desaturate = updateDesaturation();
    }

    private float updateDesaturation() {
        long now = Util.getMillis();
        float frameDelta = desaturateLastMillis == 0L
                ? 0.0f
                : Mth.clamp(
                        (now - desaturateLastMillis) / 1000.0f,
                        0.0f,
                        0.1f
                );

        desaturateLastMillis = now;
        desaturateProgress = Mth.approach(
                desaturateProgress,
                ClientUtils.isFullscreenInVr() ? 1.0f : 0.0f,
                frameDelta / 0.6f
        );
        return desaturateProgress * desaturateProgress * (3.0f - 2.0f * desaturateProgress);
    }
}
