// #!MC-VERSION:: 1.21.11+
package org.vmstudio.visor.mixin.client.vanillafix.itemmodel;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;

// [EMPTY SHELL since 1.21.11] DO NOT TOUCH
// sprites are padded instead of UV-shrunk, uvShrinkRatio is gone
@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {
}
