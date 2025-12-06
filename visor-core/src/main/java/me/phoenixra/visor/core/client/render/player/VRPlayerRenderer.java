package me.phoenixra.visor.core.client.render.player;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.core.client.player.VRClientPlayers;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.render.player.model.HeadsetModel;
import me.phoenixra.visor.core.client.render.player.model.VRPlayerModelWithArms;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;


public class VRPlayerRenderer extends PlayerRenderer {
    static LayerDefinition vrLayerArms = LayerDefinition.create(
            VRPlayerModelWithArms.createMesh(
                    CubeDeformation.NONE, false
            ), 64, 64
    );
    static LayerDefinition vrLayerArmsSlim = LayerDefinition.create(
            VRPlayerModelWithArms.createMesh(
                    CubeDeformation.NONE, true
            ), 64, 64
    );

    public VRPlayerRenderer(EntityRendererProvider.Context context,
                            boolean slim
    ) {
        super(context, slim);
        model = slim ?
                new VRPlayerModelWithArms<>(
                        vrLayerArmsSlim.bakeRoot(),
                        true
                )
                :
                new VRPlayerModelWithArms<>(
                        vrLayerArms.bakeRoot(),
                        false
                );


        this.addLayer(new HeadsetModel(this));
    }

    @Override
    public void render(AbstractClientPlayer entityIn,
                       float pEntityYaw, float pPartialTicks,
                       PoseStack matrixStackIn,
                       MultiBufferSource pBuffer,
                       int pPackedLight
    ) {

        var vrPlayer = VRClientPlayers
                .getPlayer(entityIn.getUUID());

        if (vrPlayer != null) {
            float heightScale = vrPlayer.getFullHeightScale();
            matrixStackIn.scale(
                    heightScale,
                    heightScale,
                    heightScale
            );
            super.render(
                    entityIn, pEntityYaw, pPartialTicks,
                    matrixStackIn, pBuffer, pPackedLight
            );
            matrixStackIn.scale(
                    1.0F,
                    1.0F / heightScale,
                    1.0F
            );
        }
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer pEntity,
                                float pPartialTicks
    ) {
        //idk why we do this anymore
        return pEntity.isVisuallySwimming()
                ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
        // return pEntity.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(pEntity, pPartialTicks);
    }

    @Override
    public void setModelProperties(AbstractClientPlayer pClientPlayer) {
        super.setModelProperties(pClientPlayer);

        this.getModel().crouching &= !pClientPlayer.isVisuallySwimming();
    }

    @Override
    protected void setupRotations(AbstractClientPlayer pEntityLiving,
                                  PoseStack pMatrixStack,
                                  float pAgeInTicks, float pRotationYaw,
                                  float pPartialTicks
    ) {
        UUID uuid = pEntityLiving.getUUID();
        if (VRRenderState.getPhase().isNotVRGui()
                && VRClientPlayers.isTracked(uuid)) {
            var vrPlayer = VRClientPlayers.getPlayer(uuid);
            pRotationYaw = (float) Math.toDegrees(vrPlayer.getPoseData(PlayerPoseType.RENDER).getBodyYaw());
        }

        //vanilla below here
        super.setupRotations(
                pEntityLiving, pMatrixStack,
                pAgeInTicks, pRotationYaw,
                pPartialTicks
        );
    }
}
