package org.vmstudio.visor.compatibility;

import lombok.Getter;
import net.minecraft.world.item.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public enum ItemClassifier {
    FARMING_TOOL((itemStack) -> itemStack.getItem() instanceof HoeItem),
    SHIELD((itemStack) -> itemStack.getItem() instanceof ShieldItem),
    SWORD((itemStack) -> itemStack.getItem() instanceof SwordItem),
    SPEAR((itemStack) -> itemStack.getItem() instanceof TridentItem),
    FOOD_STICK((itemStack) -> itemStack.getItem() instanceof FoodOnAStickItem),
    THROWABLE((itemStack) -> {
        Item item = itemStack.getItem();
        return item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof SplashPotionItem
                || item instanceof LingeringPotionItem
                || item instanceof FireChargeItem;
    });


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
}
