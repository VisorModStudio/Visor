package me.phoenixra.visor.core.client.render.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayerData;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class HeadsetModel extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {


    public HeadsetModel(RenderLayerParent<AbstractClientPlayer,
            PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack,
                       MultiBufferSource multiBufferSource,
                       int i,
                       AbstractClientPlayer entity,
                       float f, float g, float h, float j,
                       float k, float l) {
        //nothing for now
    }
}
