package org.vmstudio.visor.mixin.client.vanillafix.itemmodel;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TextureAtlasSprite.class)
public abstract class TextureAtlasSpriteMixin {
    @Shadow
    protected abstract float atlasSize();

    @Shadow
    public abstract ResourceLocation atlasLocation();

    @Inject(method = "uvShrinkRatio", at = @At("RETURN"), cancellable = true)
    public void visor$fixOutlineTransparency(CallbackInfoReturnable<Float> cir) {
        float expectedValue = 4.0F / this.atlasSize();

        boolean blockAtlas = this.atlasLocation()
                .equals(InventoryMenu.BLOCK_ATLAS);

        if (blockAtlas
                && expectedValue == cir.getReturnValueF()) {
            cir.setReturnValue(
                    0f
            );
        }
    }
}
