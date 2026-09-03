package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.utils.GLUtils;
import org.vmstudio.visor.core.client.render.helpers.RenderShaderHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class VRShaderPostProcessEye implements VRShader{

    @Getter
    private ShaderInstance handle;

    private AbstractUniform uTintRed;
    private AbstractUniform uTintBlue;
    private AbstractUniform uTintBlack;



    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "vr_post_process_eye", DefaultVertexFormat.POSITION_TEX);

        uTintRed = handle.safeGetUniform("uTintRed");
        uTintBlue = handle.safeGetUniform("uTintBlue");
        uTintBlack = handle.safeGetUniform("uTintBlack");

    }


    public void finishEye(EyeType eye,
                          RenderTarget source,
                          float partialTicks) {
        if (eye == EyeType.LEFT) {
            // update state only for the first rendered eye,
            // to have synchronized effects for both
            updateUniforms(partialTicks);
        }

        RenderShaderHelper.renderFullscreenQuad(handle, source);

        GLUtils.checkGLError("post process eye: "+ eye.name());
    }


    private void updateUniforms(float partialTicks){

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
        uTintRed.set(redTint);
        uTintBlue.set(blueTint);
        uTintBlack.set(blackTint);
    }

}
