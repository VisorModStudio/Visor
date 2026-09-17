package org.vmstudio.visor.api.compatibility;

import lombok.Getter;
//? if >=1.20.5 {
import net.minecraft.tags.ItemTags;
//?}
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public enum ItemClassifier {
    FARMING_TOOL((itemStack) -> itemStack.getItem() instanceof HoeItem),
    SHIELD((itemStack) -> itemStack.getItem() instanceof ShieldItem
            || itemStack.is(VisorItemTags.SHIELDS)),
    SWORD(ItemClassifier::isSword),
    MACE(ItemClassifier::isMace),
    SPEAR(ItemClassifier::isSpear),
    FOOD_STICK((itemStack) -> itemStack.getItem() instanceof FoodOnAStickItem),
    THROWABLE(ItemClassifier::isThrowable);


    @Getter
    private final List<Predicate<ItemStack>> recognizers;
    @Getter
    private final List<Predicate<ItemStack>> filters;

    ItemClassifier(Predicate<ItemStack> defRecognizer) {
        recognizers = new ArrayList<>();
        filters = new ArrayList<>();
        recognizers.add(defRecognizer);
    }


    public boolean is(ItemStack itemStack) {
        boolean recognized = false;
        for (Predicate<ItemStack> entry : recognizers) {
            if (entry.test(itemStack)) {
                recognized = true;
                break;
            }
        }
        for (Predicate<ItemStack> entry : filters) {
            if (!entry.test(itemStack)) {
                recognized = false;
                break;
            }
        }

        return recognized;
    }

    public boolean is(Item item) {
        return is(item.getDefaultInstance());
    }


    public static boolean isDiggerTool(Item item) {
        //? if >=1.21.5 {
        var holder = item.builtInRegistryHolder();
        return holder.is(ItemTags.PICKAXES) || holder.is(ItemTags.AXES)
                || holder.is(ItemTags.SHOVELS) || holder.is(ItemTags.HOES);
        //?} else {
        /*return item instanceof DiggerItem;
        *///?}
    }

    public static boolean isPickaxe(Item item) {
        //? if >=1.21.5 {
        return item.builtInRegistryHolder().is(ItemTags.PICKAXES);
        //?} else {
        /*return item instanceof PickaxeItem;
        *///?}
    }

    private static boolean isSword(ItemStack itemStack) {
        //? if >=1.20.5 {
        if (itemStack.is(ItemTags.SWORD_ENCHANTABLE)) {
            return true;
        }
        //?}
        //? if >=1.21.5 {
        return itemStack.is(ItemTags.SWORDS);
        //?} else {
        /*return itemStack.getItem() instanceof SwordItem;
        *///?}
    }

    private static boolean isMace(ItemStack itemStack) {
        //? if >=1.20.5 {
        return itemStack.getItem() instanceof MaceItem
                || itemStack.is(ItemTags.MACE_ENCHANTABLE);
        //?} else {
        /*return false;
        *///?}
    }

    private static boolean isThrowable(ItemStack itemStack) {
        Item item = itemStack.getItem();
        //? if >=1.20.5 {
        if (item instanceof WindChargeItem) {
            return true;
        }
        //?}
        return item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof SplashPotionItem
                || item instanceof LingeringPotionItem
                || item instanceof FireChargeItem;
    }

    private static boolean isSpear(ItemStack itemStack) {
        //? if >=1.20.5 {
        if (itemStack.is(ItemTags.TRIDENT_ENCHANTABLE)) {
            return true;
        }
        //?}
        return itemStack.getItem() instanceof TridentItem;
    }
}
