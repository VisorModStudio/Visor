package org.vmstudio.visor.mixin.client.renderer.entity;

import org.vmstudio.visor.extensions.client.render.RenderLayerExtension;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(RenderLayer.class)
public class RenderLayerMixin<S extends EntityRenderState, M extends EntityModel<? super S>> implements Cloneable, RenderLayerExtension {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
