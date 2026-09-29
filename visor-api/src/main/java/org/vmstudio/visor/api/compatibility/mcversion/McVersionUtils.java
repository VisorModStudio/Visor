package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
//? if >=1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.CustomModelData;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
*///?}
//? if <1.21.2 {
/*import net.minecraft.world.item.Equipable;
*///?}
//? if >=1.21.2 {
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
//?} else {
/*import net.minecraft.world.entity.vehicle.boat.Boat;
*///?}
import net.minecraft.world.entity.Entity;

/**
 * Cross-mc-version Utils for common methods
 */
public class McVersionUtils {
    private McVersionUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    // ------- RESOURCES -------

    public static Identifier newResourceLoc(String namespace,
                                                  String path){
        //? if >=1.21 {
        return Identifier.fromNamespaceAndPath(namespace, path);
        //?} else {
        /*return new Identifier(namespace, path);
        *///?}
    }
    public static Identifier newResourceLoc(String location){
        //? if >=1.21 {
        return Identifier.parse(location);
        //?} else {
        /*return new Identifier(location);
        *///?}
    }


    // ------- ITEM USE -------

    public static int useDuration(ItemStack stack, LivingEntity user) {
        //? if >=1.21 {
        return stack.getUseDuration(user);
        //?} else {
        /*return stack.getUseDuration();
        *///?}
    }

    public static float riptideStrength(ItemStack stack, LivingEntity user) {
        //? if >=1.21 {
        return EnchantmentHelper.getTridentSpinAttackStrength(stack, user);
        //?} else {
        /*return EnchantmentHelper.getRiptide(stack);
        *///?}
    }

    public static McUseAnim useAnimation(ItemStack stack) {
        return switch (stack.getUseAnimation()) {
            case NONE -> McUseAnim.NONE;
            case EAT -> McUseAnim.EAT;
            case DRINK -> McUseAnim.DRINK;
            case BLOCK -> McUseAnim.BLOCK;
            case BOW -> McUseAnim.BOW;
            // 1.21.11 renamed the trident's SPEAR to TRIDENT and gave SPEAR to the new spears
            //? if >=1.21.11 {
            case TRIDENT -> McUseAnim.SPEAR;
            //?} else {
            /*case SPEAR -> McUseAnim.SPEAR;
            *///?}
            case CROSSBOW -> McUseAnim.CROSSBOW;
            case SPYGLASS -> McUseAnim.SPYGLASS;
            case TOOT_HORN -> McUseAnim.TOOT_HORN;
            case BRUSH -> McUseAnim.BRUSH;
            default -> McUseAnim.CUSTOM;
        };
    }

    public static boolean isOnCooldown(Player player, ItemStack stack) {
        //? if >=1.21.2 {
        return player.getCooldowns().isOnCooldown(stack);
        //?} else {
        /*return player.getCooldowns().isOnCooldown(stack.getItem());
        *///?}
    }

    public static boolean shouldSwing(InteractionResult result) {
        //? if >=1.21.2 {
        return result instanceof InteractionResult.Success success
                && success.swingSource() == InteractionResult.SwingSource.CLIENT;
        //?} else {
        /*return result.shouldSwing();
        *///?}
    }


    // ------- TEXT HELPERS -------
    public static String filterText(String text,
                                    boolean allowLineBreaks){
        //? if >=1.20.5 {
        return StringUtil.filterText(text, allowLineBreaks);
        //?} else {
        /*return SharedConstants.filterText(text, allowLineBreaks);
        *///?}
    }

    public static boolean isAllowedChatCharacter(char character){
        //? if >=1.20.5 {
        return StringUtil.isAllowedChatCharacter(character);
        //?} else {
        /*return SharedConstants.isAllowedChatCharacter(character);
        *///?}
    }


    // ------- ENTITY -------

    // 1.20.5 made EntityDimensions a record
    public static float dimensionsWidth(EntityDimensions dimensions){
        //? if >=1.20.5 {
        return dimensions.width();
        //?} else {
        /*return dimensions.width;
        *///?}
    }

    public static boolean canInteractWithEntity(Player player,
                                                AABB boundingBox){
        //? if >=1.21.11 {
        return player.isWithinEntityInteractionRange(boundingBox, 1.0);
        //?} elif >=1.20.5 {
        /*return player.canInteractWithEntity(boundingBox, 1.0);
        *///?} else {
        /*return boundingBox.distanceToSqr(player.getEyePosition())
                < ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
        *///?}
    }

    public static boolean canInteractWithBlock(Player player,
                                               BlockPos blockPos){
        //? if >=1.21.11 {
        return player.isWithinBlockInteractionRange(blockPos, 1.0);
        //?} elif >=1.20.5 {
        /*return player.canInteractWithBlock(blockPos, 1.0);
        *///?} else {
        /*return blockPos.distToCenterSqr(player.getEyePosition())
                < ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;
        *///?}
    }


    public static void setStepHeight(LivingEntity entity, float stepHeight){
        //? if >=1.20.5 {
        AttributeInstance instance = entity.getAttribute(Attributes.STEP_HEIGHT);
        if (instance != null) {
            instance.setBaseValue(stepHeight);
        }
        //?} else {
        /*entity.setMaxUpStep(stepHeight);
        *///?}
    }

    public static boolean isBoat(Entity entity) {
        //? if >=1.21.2 {
        return entity instanceof AbstractBoat;
        //?} else {
        /*return entity instanceof Boat;
        *///?}
    }


    // ------- BLOCKS -------

    public static boolean isSolidRender(BlockState state, BlockGetter level, BlockPos pos) {
        //? if >=1.21.2 {
        return state.isSolidRender();
        //?} else {
        /*return state.isSolidRender(level, pos);
        *///?}
    }

    // exclusive top like the old getMaxBuildHeight, 1.21.2 replaced it with the inclusive getMaxY
    public static int maxBuildHeight(LevelHeightAccessor level) {
        //? if >=1.21.2 {
        return level.getMaxY() + 1;
        //?} else {
        /*return level.getMaxBuildHeight();
        *///?}
    }


    // ------- ITEMS -------

    public static ChatFormatting rarityColor(Rarity rarity){
        //? if >=1.20.5 {
        return rarity.color();
        //?} else {
        /*return rarity.color;
        *///?}
    }

    public static boolean hasCustomHoverName(ItemStack itemStack){
        //? if >=1.20.5 {
        return itemStack.has(DataComponents.CUSTOM_NAME);
        //?} else {
        /*return itemStack.hasCustomHoverName();
        *///?}
    }

    // 1.21.2 replaced the Equipable interface with the EQUIPPABLE component
    public static boolean isEquippable(ItemStack itemStack){
        //? if >=1.21.2 {
        return itemStack.has(DataComponents.EQUIPPABLE);
        //?} else {
        /*return Equipable.get(itemStack) != null;
        *///?}
    }

    public static void addItemAttributeModifiers(LivingEntity entity,
                                                 ItemStack itemStack,
                                                 EquipmentSlot slot){
        //? if >=1.20.5 {
        itemStack.forEachModifier(slot, (attribute, modifier) -> {
            AttributeInstance instance = entity.getAttributes().getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifier);
                instance.addTransientModifier(modifier);
            }
        });
        //?} else {
        /*entity.getAttributes().addTransientAttributeModifiers(
                itemStack.getAttributeModifiers(slot));
        *///?}
    }

    public static void removeItemAttributeModifiers(LivingEntity entity,
                                                    ItemStack itemStack,
                                                    EquipmentSlot slot){
        //? if >=1.20.5 {
        itemStack.forEachModifier(slot, (attribute, modifier) -> {
            AttributeInstance instance = entity.getAttributes().getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifier);
            }
        });
        //?} else {
        /*entity.getAttributes().removeAttributeModifiers(
                itemStack.getAttributeModifiers(slot));
        *///?}
    }

    public static int customModelData(ItemStack itemStack){
        //? if >=1.21.4 {
        CustomModelData data = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        Float value = data == null ? null : data.getFloat(0);
        return value == null ? 0 : value.intValue();
        //?} elif >=1.20.5 {
        /*CustomModelData data = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        return data == null ? 0 : data.value();
        *///?} else {
        /*CompoundTag tag = itemStack.getTag();
        return tag == null ? 0 : tag.getInt("CustomModelData");
        *///?}
    }

}
