// #!MC-VERSION:: 1.20.1-1.21.8
package org.vmstudio.visor.mixin.client.model;

import net.minecraft.client.renderer.entity.layers.CapeLayer;
import org.spongepowered.asm.mixin.Mixin;

// [EMPTY SHELL before 1.21.9] CapeLayerMixin poses the cape part itself there, PlayerCapeModel exists from 1.21.2
@Mixin(CapeLayer.class)
public abstract class PlayerCapeModelMixin {
}
