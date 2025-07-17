package me.phoenixra.visor.core.mixin.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.mcmodified.entity.EntityRenderDispatcherPlayerModified;
import me.phoenixra.visor.core.client.mcmodified.render.RenderLayerModified;
import me.phoenixra.visor.core.client.render.player.RenderLayerType;
import me.phoenixra.visor.core.client.render.player.VRPlayerRenderer;
import me.phoenixra.visor.core.common.CommonUtils;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayerData;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EntityPlayerRenderMixins {
    @Mixin(EntityRenderDispatcher.class)
    public abstract static class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherPlayerModified {

        @Unique
        private final Map<String, VRPlayerRenderer> visor$skinMapVR = new HashMap<>();

        @Unique
        private VRPlayerRenderer visor$playerRendererVR;
        @Override
        public Map<String, VRPlayerRenderer> visor$getSkinMapVR() {
            return visor$skinMapVR;
        }

        @Inject(at = @At("HEAD"), method = "renderHitbox")
        private static void visor$headHitbox(PoseStack poseStack,
                                            VertexConsumer vertexConsumer,
                                            Entity entity,
                                            float f,
                                            CallbackInfo ci) {
            AABB headBox;
            if ((headBox = CommonUtils.getEntityHeadHitBox(entity, 0.0)) != null) {
                // raw head box
                LevelRenderer.renderLineBox(
                        poseStack, vertexConsumer,
                        headBox.move(
                                -entity.getX(),
                                -entity.getY(),
                                -entity.getZ()
                        ),
                        1.0f, 1.0f, 0.0f, 1.0f
                );
                // inflated head box for arrows
                AABB headBoxArrow = CommonUtils.getEntityHeadHitBox(
                        entity, 0.3
                );
                LevelRenderer.renderLineBox(
                        poseStack, vertexConsumer,
                        headBoxArrow.move(
                                -entity.getX(),
                                -entity.getY(),
                                -entity.getZ()
                        ),
                        1.0f, 0.0f, 0.0f, 1.0f
                );
            }
        }

        @Inject(at = @At("HEAD"), method = "getRenderer(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;", cancellable = true)
        public void visor$renderer(Entity pEntity,
                                  CallbackInfoReturnable<EntityRenderer<AbstractClientPlayer>> info) {
            if (pEntity instanceof AbstractClientPlayer) {
                String skinId = ((AbstractClientPlayer) pEntity).getModelName();
                VRRemotePlayerData vrPlayerNetworkData = VRRemotePlayers.getInstance()
                        .getPlayer(
                                pEntity.getUUID()
                        );
                if (vrPlayerNetworkData != null) {
                    VRPlayerRenderer playerRenderer;
                    playerRenderer = this.visor$skinMapVR.get(skinId);
                    if (playerRenderer == null) {
                        playerRenderer = this.visor$playerRendererVR;
                    }

                    info.setReturnValue(playerRenderer);
                }
            }
        }

        @Inject(at = @At(value = "HEAD"), method = "onResourceManagerReload")
        public void visor$reloadClear(ResourceManager resourceManager,
                                     CallbackInfo ci) {
            visor$skinMapVR.clear();
        }

        @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;", shift = At.Shift.AFTER),
                method = "onResourceManagerReload", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
        public void visor$reload(ResourceManager resourceManager,
                                CallbackInfo info,
                                EntityRendererProvider.Context context) {

            this.visor$playerRendererVR = new VRPlayerRenderer(context, false);
            this.visor$skinMapVR.put("default", visor$playerRendererVR);
            this.visor$skinMapVR.put("slim", new VRPlayerRenderer(context, true));
        }
    }


    @Mixin(LivingEntityRenderer.class)
    public abstract static class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

        @Shadow
        protected M model;

        protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
            super(context);
        }

        @Inject(at = @At("HEAD"), method = "addLayer") @SuppressWarnings("unchecked")
        public void visor$copyLayer(RenderLayer<T, M> renderLayer,
                                   CallbackInfoReturnable<Boolean> cir
        ) {
            // check if the layer gets added from the PlayerRenderer, we don't want to copy, if we add it to the VRPlayerRenderer
            // also check that the VRPlayerRenderers were created, this method also gets called in the constructor,
            // those default Layers already are added to the VRPlayerRenderer there
            if ((Object) this.getClass() == PlayerRenderer.class
                    && !((EntityRenderDispatcherPlayerModified) Minecraft.getInstance()
                    .getEntityRenderDispatcher()).visor$getSkinMapVR().isEmpty()) {

                // try to find a suitable constructor, so we can create a new Object without issues
                Constructor<?> constructor = null;
                RenderLayerType type = RenderLayerType.OTHER;
                for (Constructor<?> c : renderLayer.getClass().getConstructors()) {
                    if (c.getParameterCount() == 1
                            && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])) {
                        constructor = c;
                        type = RenderLayerType.PARENT_ONLY;
                        break;
                    } else if (c.getParameterCount() == 2
                            && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])
                            && EntityModelSet.class.isAssignableFrom(c.getParameterTypes()[1])) {
                        constructor = c;
                        type = RenderLayerType.PARENT_MODELSET;
                    } else if (c.getParameterCount() == 3
                            && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])
                            && HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[1])
                            && HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[2])
                            && renderLayer instanceof HumanoidArmorLayer) {
                        constructor = c;
                        type = RenderLayerType.PARENT_MODEL_MODEL;
                    }
                }

                // if no suitable constructor was found, use do a basic Object.clone call, and replace the parent of the copy
                if (constructor == null) {
                    // do a hacky clone, and replace parent
                    if (((PlayerModel<?>) model).slim) {
                        visor$addLayerClone(
                                renderLayer,
                                (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherPlayerModified) entityRenderDispatcher)
                                        .visor$getSkinMapVR().get("slim")
                        );
                    } else {
                        visor$addLayerClone(
                                renderLayer,
                                (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherPlayerModified) entityRenderDispatcher)
                                        .visor$getSkinMapVR().get("default")
                        );
                    }
                } else {
                    // make a new instance with the vr model as parent
                    if (((PlayerModel<?>) model).slim) {
                        visor$addLayerConstructor(
                                constructor, type,
                                (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherPlayerModified) entityRenderDispatcher)
                                        .visor$getSkinMapVR().get("slim")
                        );
                    } else {
                        visor$addLayerConstructor(
                                constructor, type,
                                (LivingEntityRenderer<T, M>) ((EntityRenderDispatcherPlayerModified) entityRenderDispatcher)
                                        .visor$getSkinMapVR().get("default")
                        );
                    }
                }
            }
        }

        /**
         * does a basic Object.clone() copy
         */
        @Unique
        private void visor$addLayerClone(RenderLayer<T, M> renderLayer, LivingEntityRenderer<T, M> target) {
            try {
                VisorClientImpl.LOGGER.warn(
                        "Copying layer: {} with Object.copy, " +
                                "this could cause issues",
                        renderLayer.getClass()
                );
                RenderLayer<T, M> newLayer = (RenderLayer<T, M>) ((RenderLayerModified) renderLayer).clone();
                newLayer.renderer = target;
                target.addLayer(newLayer);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * uses the provided constructor, to create a new RenderLayer Instance
         */
        @Unique @SuppressWarnings("unchecked")
        private void visor$addLayerConstructor(Constructor<?> constructor,
                                              RenderLayerType type,
                                              LivingEntityRenderer<T, M> target
        ) {
            try {
                switch (type) {
                    case PARENT_ONLY -> target.addLayer(
                            (RenderLayer<T, M>) constructor.newInstance(target)
                    );
                    case PARENT_MODELSET ->
                            target.addLayer(
                                    (RenderLayer<T, M>) constructor.newInstance(
                                            target,
                                            Minecraft.getInstance()
                                                    .getEntityModels()
                                    )
                            );
                    case PARENT_MODEL_MODEL -> {
                        if (((PlayerModel<?>) model).slim) {
                            target.addLayer(
                                    (RenderLayer<T, M>) constructor.newInstance(
                                            target,
                                            new HumanoidModel<>(
                                                    Minecraft.getInstance().getEntityModels()
                                                            .bakeLayer(
                                                                    ModelLayers.PLAYER_SLIM_INNER_ARMOR
                                                            )
                                            ),
                                            new HumanoidModel<>(
                                                    Minecraft.getInstance().getEntityModels()
                                                            .bakeLayer(
                                                                    ModelLayers.PLAYER_SLIM_OUTER_ARMOR
                                                            )
                                            )
                                    )
                            );
                        } else {
                            target.addLayer(
                                    (RenderLayer<T, M>) constructor.newInstance(
                                            target,
                                            new HumanoidModel<>(
                                                    Minecraft.getInstance().getEntityModels()
                                                            .bakeLayer(
                                                                    ModelLayers.PLAYER_INNER_ARMOR
                                                            )
                                            ),
                                            new HumanoidModel<>(
                                                    Minecraft.getInstance().getEntityModels()
                                                            .bakeLayer(
                                                                    ModelLayers.PLAYER_OUTER_ARMOR
                                                            )
                                            )
                                    )
                            );
                        }
                    }
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
