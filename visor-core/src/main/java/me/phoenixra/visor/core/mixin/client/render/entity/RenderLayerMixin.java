package me.phoenixra.visor.core.mixin.client.render.entity;

import me.phoenixra.visor.core.client.mcmodified.render.RenderLayerModified;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(RenderLayer.class)
public class RenderLayerMixin<T extends Entity, M extends EntityModel<T>> implements Cloneable, RenderLayerModified {
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
