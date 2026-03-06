package org.vmstudio.visor.mixin.client.renderer.entity.player;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.vmstudio.visor.api.common.player.VRBodyType;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.player.RenderLayerType;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.extensions.client.entity.EntityRenderDispatcherPlayerExtension;
import org.vmstudio.visor.core.client.render.player.VRPlayerRenderer;
import org.vmstudio.visor.core.common.CommonUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vmstudio.visor.extensions.client.render.RenderLayerExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PlayerRenderMixins {
    @Mixin(EntityRenderDispatcher.class)
    public abstract static class EntityRenderDispatcherMixin implements ResourceManagerReloadListener, EntityRenderDispatcherPlayerExtension {

        @Unique
        private final Map<String, VRPlayerRenderer> visor$skinMapVRVanilla = new HashMap<>();

        @Unique
        private final Map<String, VRPlayerRenderer> visor$skinMapVRArms = new HashMap<>();

        @Unique
        private final Map<String, VRPlayerRenderer> visor$skinMapVRLegs = new HashMap<>();

        @Unique
        private VRPlayerRenderer visor$playerRendererVRVanilla;

        @Unique
        private VRPlayerRenderer visor$playerRendererVRArms;

        @Unique
        private VRPlayerRenderer visor$playerRendererVRLegs;

        @Override
        public Map<String, VRPlayerRenderer> visor$getSkinMapVRVanilla() {
            return this.visor$skinMapVRVanilla;
        }

        @Override
        public Map<String, VRPlayerRenderer> visor$getSkinMapVRArms() {
            return this.visor$skinMapVRArms;
        }

        @Override
        public Map<String, VRPlayerRenderer> visor$getSkinMapVRLegs() {
            return this.visor$skinMapVRLegs;
        }


        @Inject(method = "getRenderer", at = @At("HEAD"), cancellable = true)
        private void visor$getVRPlayerRenderer(
                Entity entity, CallbackInfoReturnable<EntityRenderer<AbstractClientPlayer>> cir)
        {
            // don't do any animations for dummy players
            if (entity instanceof AbstractClientPlayer player &&
                    (player.getClass() == LocalPlayer.class || player.getClass() == RemotePlayer.class))
            {
                String skinType = player.getModelName();
                if (VRClientPlayers.isTracked(player)) {
                    cir.setReturnValue(
                            visor$getVRRenderer(skinType));
                }
            }
        }

        @Unique
        private VRPlayerRenderer visor$getVRRenderer(String skinType) {
            VRClientSettings.PlayerModelType type = VRClientSettings.getPlayerModelType();
            if (type == VRClientSettings.PlayerModelType.VANILLA) {
                return this.visor$skinMapVRVanilla.getOrDefault(skinType, this.visor$playerRendererVRVanilla);
            } else if (type == VRClientSettings.PlayerModelType.SPLIT_ARMS) {
                return this.visor$skinMapVRArms.getOrDefault(skinType, this.visor$playerRendererVRArms);
            } else {
                return this.visor$skinMapVRLegs.getOrDefault(skinType, this.visor$playerRendererVRLegs);
            }
        }

        @Inject(method = "onResourceManagerReload", at = @At(value = "HEAD"))
        private void visor$clearVRPlayerRenderer(CallbackInfo ci) {
            this.visor$skinMapVRVanilla.clear();
            this.visor$skinMapVRArms.clear();
            this.visor$skinMapVRLegs.clear();
        }

        @Inject(method = "onResourceManagerReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderers;createPlayerRenderers(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;)Ljava/util/Map;"))
        private void visor$reloadVRPlayerRenderer(CallbackInfo ci, @Local EntityRendererProvider.Context context) {
            this.visor$playerRendererVRVanilla = new VRPlayerRenderer(context, false,
                    VRClientSettings.PlayerModelType.VANILLA);
            this.visor$skinMapVRVanilla.put("default", this.visor$playerRendererVRVanilla);
            this.visor$skinMapVRVanilla.put("slim", new VRPlayerRenderer(context, true,
                    VRClientSettings.PlayerModelType.VANILLA)
            );

            this.visor$playerRendererVRArms = new VRPlayerRenderer(context, false,
                    VRClientSettings.PlayerModelType.SPLIT_ARMS);
            this.visor$skinMapVRArms.put("default", this.visor$playerRendererVRArms);
            this.visor$skinMapVRArms.put("slim", new VRPlayerRenderer(context, true,
                    VRClientSettings.PlayerModelType.SPLIT_ARMS));

            this.visor$playerRendererVRLegs = new VRPlayerRenderer(context, false,
                    VRClientSettings.PlayerModelType.SPLIT_ARMS_LEGS);
            this.visor$skinMapVRLegs.put("default", this.visor$playerRendererVRLegs);
            this.visor$skinMapVRLegs.put("slim", new VRPlayerRenderer(context, true,
                    VRClientSettings.PlayerModelType.SPLIT_ARMS_LEGS)
            );
        }
    }

    @Mixin(LivingEntityRenderer.class)
    public abstract static class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> {

        @Shadow
        protected M model;

        protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
            super(context);
        }

        @Inject(method = "addLayer", at = @At("HEAD"))
        protected void visor$onAddLayer(RenderLayer<T, M> renderLayer, CallbackInfoReturnable<Boolean> cir) {}
    }

    @Mixin(PlayerRenderer.class)
    public abstract static class PlayerRendererMixin extends LivingEntityRendererMixin<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

        protected PlayerRendererMixin(EntityRendererProvider.Context context) {
            super(context);
        }

        @ModifyArg(method = "setupRotations(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;FFF)V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;acos(D)D"))
        private double visor$fixFlicker(double acos) {
            // because of imprecision issues this can cause nans
            // is fixed in 1.21.4
            return Math.min(1.0, acos);
        }

        /**
         * A hacky way of copying regular PlayerRenderer layers to the VRPlayerRenderers
         * an alternative would be to add the VRPlayerRenderers to the skin model list,
         * so mods could add it manually, but some mods hardcode only the slim/default model,
         * and that would mean the VRPlayerRenderers would be missing those layers completely
         */
        @Override
        @SuppressWarnings("unchecked")
        protected void visor$onAddLayer(
                RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer,
                CallbackInfoReturnable<Boolean> cir)
        {
            // check if the layer gets added from the PlayerRenderer, we don't want to copy, if we add it to the VRPlayerRenderer
            // also check that the VRPlayerRenderers were created, this method also gets called in the constructor,
            // those default Layers already are added to the VRPlayerRenderer there
            EntityRenderDispatcherPlayerExtension renderExtension = (EntityRenderDispatcherPlayerExtension) this.entityRenderDispatcher;
            if ((Object) this.getClass() == PlayerRenderer.class &&
                    !renderExtension.visor$getSkinMapVRVanilla().isEmpty())
            {

                // try to find a suitable constructor, so we can create a new Object without issues
                Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> constructor = null;
                RenderLayerType type = RenderLayerType.OTHER;
                for (Constructor<?> c : renderLayer.getClass().getConstructors()) {
                    if (c.getParameterCount() == 1 && RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0])) {
                        constructor = (Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) c;
                        type = RenderLayerType.PARENT_ONLY;
                        break;
                    } else if (c.getParameterCount() == 2 &&
                            RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0]) &&
                            EntityModelSet.class.isAssignableFrom(c.getParameterTypes()[1]))
                    {
                        constructor = (Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) c;
                        type = RenderLayerType.PARENT_MODELSET;
                    } else if (c.getParameterCount() == 3 &&
                            RenderLayerParent.class.isAssignableFrom(c.getParameterTypes()[0]) &&
                            HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[1]) &&
                            HumanoidModel.class.isAssignableFrom(c.getParameterTypes()[2]) &&
                            renderLayer instanceof HumanoidArmorLayer)
                    {
                        constructor = (Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>>) c;
                        type = RenderLayerType.PARENT_MODEL_MODEL;
                    }
                }

                String modelType = this.model.slim ? "slim" : "default";

                // if no suitable constructor was found, use do a basic Object.clone call, and replace the parent of the copy
                if (constructor == null) {
                    // do a hacky clone, and replace parent
                    visor$addLayerClone(renderLayer, renderExtension.visor$getSkinMapVRVanilla().get(modelType));
                    visor$addLayerClone(renderLayer, renderExtension.visor$getSkinMapVRArms().get(modelType));
                    visor$addLayerClone(renderLayer, renderExtension.visor$getSkinMapVRLegs().get(modelType));
                } else {
                    if (!constructor.canAccess(null)) {
                        // make sure the target class is accessible or this will error
                        ClientContext.visor.getLogger().warn("Visor: layer constructor of '{}' was private, making it accessible",
                                renderLayer.getClass());
                        constructor.setAccessible(true);
                    }
                    // make a new instance with the vr model as parent
                    visor$addLayerConstructor(renderLayer, constructor, type,
                            renderExtension.visor$getSkinMapVRVanilla().get(modelType));
                    visor$addLayerConstructor(renderLayer, constructor, type,
                            renderExtension.visor$getSkinMapVRArms().get(modelType));
                    visor$addLayerConstructor(renderLayer, constructor, type,
                            renderExtension.visor$getSkinMapVRLegs().get(modelType));
                }
            }
        }

        /**
         * does a basic Object.clone() copy
         */
        @SuppressWarnings("unchecked")
        @Unique
        private void visor$addLayerClone(
                RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer, VRPlayerRenderer target)
        {
            // only add layers once
            if (target.hasLayerType(renderLayer)) return;
            try {
                ClientContext.visor.getLogger().warn("Visor: Copying layer: {} with Object.copy, this could cause issues",
                        renderLayer.getClass());
                RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> newLayer = (RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) ((RenderLayerExtension) renderLayer).clone();
                newLayer.renderer = target;
                target.addLayer(newLayer);
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * uses the provided constructor, to create a new RenderLayer Instance
         */
        @Unique
        private void visor$addLayerConstructor(
                RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayer,
                Constructor<RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>> constructor,
                RenderLayerType type, VRPlayerRenderer target)
        {
            // only add layers once
            if (target.hasLayerType(renderLayer)) return;

            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();

            try {
                switch (type) {
                    case PARENT_ONLY -> target.addLayer(constructor.newInstance(target));
                    case PARENT_MODELSET -> target.addLayer(constructor.newInstance(target, modelSet));
                    case PARENT_MODEL_MODEL -> {
                        if (this.model.slim) {
                            target.addLayer(constructor.newInstance(target,
                                    new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)),
                                    new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR))));
                        } else {
                            target.addLayer(constructor.newInstance(target,
                                    new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                                    new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR))));
                        }
                    }
                }
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
