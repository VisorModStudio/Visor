package org.vmstudio.visor.core.client.render.shaders;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import me.phoenixra.atumvr.api.enums.EyeType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.compatibility.mcversion.render.McGlState;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderTarget;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.extensions.client.WindowExtension;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.helpers.MirrorHelper;
import org.vmstudio.visor.core.client.render.helpers.RenderShaderHelper;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRShaderMixedReality implements VRShader{
    @Getter
    private McShaderProgram handle;


    @Override
    public void init() throws Exception {
        handle = McShaderProgram.core("vr_mixed_reality", DefaultVertexFormat.POSITION_TEX, true);
    }


    public void drawMirror(){
        var mcWindow = ((WindowExtension) (Object) MC.getWindow());
        McGlState.viewport(0, 0,
                mcWindow.visor$mcScreenWidth(),
                mcWindow.visor$mcScreenHeight()
        );

        // --- Prepare ---
        boolean asGrid2x2 = VRClientSettings.isMixedRealityAsGrid2x2();
        boolean alphaMask = asGrid2x2
                && VRClientSettings.isMixedRealityAlphaMask();
        boolean withFirstPerson = VRClientSettings.isMixedRealityWithFirstPerson();


        var relativePose = ClientContext.localPlayer.getPoseData(PlayerPoseType.ROOM);
        var cameraElement = relativePose.getThirdPersonCamera();
        Vector3f cameraPos = relativePose.getHeadPivot()
                .sub(cameraElement.getPosition(), new Vector3f());


        var cameraRotation = cameraElement.getRotation().transpose(new Matrix4f());
        var cameraDir = cameraElement.getDirection();


        // --- Update Uniforms ---

        var proj = ((GameRendererExtension) MC.gameRenderer).visor$getThirdPersonProjection();
        Matrix4f invProjView = new Matrix4f(proj)
                .mul(cameraRotation)
                .invert();
        handle.setUniform("uInverseProjectionView", invProjView);

        handle.setUniform("uAlphaMode", alphaMask ? 1 : 0);
        handle.setUniform("uAsGrid2x2", asGrid2x2 ? 1 : 0);

        handle.setUniform("uHmdViewPosition", cameraPos.x, cameraPos.y, cameraPos.z);
        handle.setUniform("uHmdPlaneNormal", -cameraDir.x(), 0.0F, -cameraDir.z());

        if (!alphaMask) {
            var color = VRClientSettings.getMixedRealityKeyColor();
            handle.setUniform("uKeyColor",
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue()
            );
        } else {
            handle.setUniform("uKeyColor", 0F, 0F, 0F);
        }


        // --- Textures ---
        var target = ClientContext.renderer.thirdPersonTarget.getTarget();
        handle.setSampler("SamplerColor", McRenderTarget.colorTextureId(target));
        handle.setSampler("SamplerDepth", McRenderTarget.depthTextureId(target));


        // --- Render ---
        handle.apply();
        RenderShaderHelper.renderFullscreenQuad(handle.vertexFormat());
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
            RenderTarget mainTarget = McRenderTarget.mainTarget();
            MirrorHelper.blit(source,
                    mainTarget.width / 2,
                    0,
                    mainTarget.width,
                    mainTarget.height / 2
            );
        }
    }
}
