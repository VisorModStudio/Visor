// #!MC-VERSION:: 26.3+
package org.vmstudio.visor.mixin.client.renderer.entity.player;

import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.extensions.client.render.ItemInHandRendererExtension;
import com.mojang.math.Axis;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;


@Mixin(value = FirstPersonHandsAndItemsRenderer.class, priority = 999)
public abstract class ItemInHandRendererMixin implements ItemInHandRendererExtension {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Unique
    private static final RenderType visor$MAP_BACKGROUND =
            RenderTypes.text(Identifier.withDefaultNamespace("textures/map/map_background.png"));
    @Unique
    private static final RenderType visor$MAP_BACKGROUND_CHECKERBOARD =
            RenderTypes.text(Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
    @Unique
    private final MapRenderState visor$mapRenderState = new MapRenderState();


    // CallbackInfo only: 1.21.9 swapped the buffer source for a SubmitNodeCollector
    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void visor$noFirstPersonHandsInVR(CallbackInfo ci) {
        if (VRRenderState.getPhase().isNotVanilla()) {
            ci.cancel();
        }
    }

    @Override
    public void visor$renderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext,
                                 PoseStack poseStack, SubmitNodeCollector collector, int lightCoords) {
        if (!itemStack.isEmpty()) {
            ItemStackRenderState renderState = new ItemStackRenderState();
            minecraft.getItemModelResolver().updateForTopItem(renderState, itemStack, displayContext,
                    entity.level(), entity, entity.getId() + displayContext.ordinal());
            renderState.submit(poseStack, collector, lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    @Override
    public void visor$renderMap(PoseStack poseStack,
                                SubmitNodeCollector bufferSource,
                                int pCombinedLight,
                                ItemStack itemStack) {
        McRenderUtils.rotate(poseStack, Axis.YP.rotationDegrees(180.0F));
        McRenderUtils.rotate(poseStack, Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        MapId id = itemStack.get(DataComponents.MAP_ID);
        MapItemSavedData data = id == null ? null : MapItem.getSavedData(id, minecraft.level);
        RenderType renderType = data == null ? visor$MAP_BACKGROUND : visor$MAP_BACKGROUND_CHECKERBOARD;
        bufferSource.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            buffer.addVertex(pose, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(pCombinedLight);
            buffer.addVertex(pose, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(pCombinedLight);
            buffer.addVertex(pose, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(pCombinedLight);
            buffer.addVertex(pose, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(pCombinedLight);
        });
        if (data != null) {
            MapRenderer mapRenderer = minecraft.getMapRenderer();
            mapRenderer.extractRenderState(id, data, visor$mapRenderState);
            mapRenderer.render(visor$mapRenderState, poseStack, bufferSource, false, pCombinedLight);
        }
    }

    @Override
    public float visor$getEquipProgress(InteractionHand hand, float partialTicks) {
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return 0.0F;
        }
        FirstPersonHandsAndItems hands = player.firstPersonHandsAndItems();
        return hand == InteractionHand.MAIN_HAND
                ? 1.0F - (hands.oMainHandHeight + (hands.mainHandHeight - hands.oMainHandHeight) * partialTicks)
                : 1.0F - (hands.oOffHandHeight + (hands.offHandHeight - hands.oOffHandHeight) * partialTicks);
    }

}
