package org.vmstudio.visor.mixin.client.renderer.entity;

import org.vmstudio.visor.extensions.client.render.RenderLayerExtension;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(RenderLayer.class)
public class RenderLayerMixin<T extends Entity, M extends EntityModel<T>> implements Cloneable, RenderLayerExtension {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
