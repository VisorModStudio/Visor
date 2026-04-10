package org.vmstudio.visor.core.client.utils;

import net.minecraft.world.item.enchantment.Enchantment;

@FunctionalInterface
public interface EnchantmentVisitor {
    void accept(Enchantment enchantment, int i);
}