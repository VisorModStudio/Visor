package me.phoenixra.visor.core.client.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.mcmodified.WindowModified;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.helpers.MirrorHelper;
import me.phoenixra.visor.core.client.render.helpers.RenderShaderHelper;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRShaderMixedReality implements VRShader{
    @Getter
    private ShaderInstance handle;


    private AbstractUniform uHmdViewPosition;
    private AbstractUniform uHmdPlaneNormal;
    private AbstractUniform uInverseProjectionView;

    private AbstractUniform uAsGrid2x2;
    private AbstractUniform uKeyColor;
    private AbstractUniform uAlphaMode;


    @Override
    public void init() throws Exception {
        handle = new ShaderInstance(Minecraft.getInstance().getResourceManager(), "vr_mixed_reality", DefaultVertexFormat.POSITION_TEX);

        uAsGrid2x2 = handle.safeGetUniform("uAsGrid2x2");
        uAlphaMode = handle.safeGetUniform("uAlphaMode");

        uHmdViewPosition = handle.safeGetUniform("uHmdViewPosition");
        uHmdPlaneNormal = handle.safeGetUniform("uHmdPlaneNormal");
        uInverseProjectionView = handle.safeGetUniform("uInverseProjectionView");
        uKeyColor = handle.safeGetUniform("uKeyColor");

    }


    public void drawMirror(){
        var mcWindow = ((WindowModified) (Object) MC.getWindow());
        RenderSystem.viewport(0, 0,
                mcWindow.visor$getActualScreenWidth(),
                mcWindow.visor$getActualScreenHeight()
        );

        // --- Prepare ---
        boolean asGrid2x2 = VRClientSettings.isMixedRealityAsGrid2x2();
        boolean alphaMask = asGrid2x2
                && VRClientSettings.isMixedRealityAlphaMask();
        boolean withFirstPerson = VRClientSettings.isMixedRealityWithFirstPerson();


        var roomPose = ClientContext.player.getPoseData(PoseDataType.ROOM);
        var cameraElement = roomPose.getThirdPersonCamera();
        Vector3f cameraPos = roomPose.getHeadPivot()
                .sub(cameraElement.getPosition(), new Vector3f());


        var cameraRotation = cameraElement.getRotation().transpose(new Matrix4f());
        var cameraDir = cameraElement.getDirection();


        // --- Update Uniforms ---

        var proj = ((GameRendererModified) MC.gameRenderer).visor$getThirdPersonProjection();
        Matrix4f invProjView = new Matrix4f(proj)
                .mul(cameraRotation)
                .invert();
        uInverseProjectionView.set(invProjView);

        uAlphaMode.set(alphaMask ? 1 : 0);
        uAsGrid2x2.set(asGrid2x2 ? 1 : 0);

        uHmdViewPosition.set(cameraPos.x, cameraPos.y, cameraPos.z);
        uHmdPlaneNormal.set(-cameraDir.x(), 0.0F, -cameraDir.z());

        if (!alphaMask) {
            var color = VRClientSettings.getMixedRealityKeyColor();
            uKeyColor.set(
                    (float) color.getRed() / 255.0F,
                    (float) color.getGreen() / 255.0F,
                    (float) color.getBlue() / 255.0F
            );
        } else {
            uKeyColor.set(0F, 0F, 0F);
        }


        // --- Textures ---
        var target = ClientContext.renderer.thirdPersonTarget.getTarget();
        handle.setSampler("SamplerColor", target.getColorTextureId());
        handle.setSampler("SamplerDepth", target.getDepthTextureId());


        // --- Render ---
        handle.apply();
        RenderShaderHelper.renderFullscreenQuad(handle.getVertexFormat());
        handle.clear();

        if (asGrid2x2) {
            RenderTarget source;
            if (withFirstPerson) {
                source = ClientContext.renderer.firstPersonTarget.getTarget();
            } else {
                if (VRClientSettings.getMirrorEye() == EyeType.LEFT) {
                    source = ClientContext.renderer.getTextureLeftEye().getRenderTarget();
                } else {
                    source = ClientContext.renderer.getTextureRightEye().getRenderTarget();
                }
            }
            MirrorHelper.blit(source,
                    MC.mainRenderTarget.width / 2,
                    0,
                    MC.mainRenderTarget.width,
                    MC.mainRenderTarget.height / 2
            );
        }
    }
}
