package org.vmstudio.visor.compatibility;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

//@TODO use it in more cases with ItemClassifier
public class VisorItemTags {

    private VisorItemTags() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static final TagKey<Item> SHIELDS = tag("shields");

    private static TagKey<Item> tag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation("visor", name));
    }
}
