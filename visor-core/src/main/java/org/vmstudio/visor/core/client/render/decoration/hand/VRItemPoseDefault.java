package org.vmstudio.visor.core.client.render.decoration.hand;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.compatibility.ItemClassifier;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayItemPoseTest;
import org.vmstudio.visor.core.client.player.VRClientPlayers;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


/**
 * Default items positioning in VR.
 * <p>
 *     Made with {@link VROverlayItemPoseTest} tool,
 *     and based on Meta Quest 3s controllers
 *     (should be compatible with others, because gunAngle is used)
 * </p>
 */
@RegisterVRItemPose
public class VRItemPoseDefault extends VRHandItemPose {
    private static final String ID = "default";

    // 1.21.4: ItemRenderer no longer resolves a BakedModel - item models are baked into an
    // ItemStackRenderState. Reused scratch instance, updateForTopItem clears it before filling.
    private static final ItemStackRenderState ITEM_RENDER_STATE = new ItemStackRenderState();

    public VRItemPoseDefault(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void applyPose(@NotNull PoseStack stack,
                          @NotNull AbstractClientPlayer player,
                          @NotNull HandType hand,
                          @NotNull ItemStack item,
                          float equipProgress,
                          float partialTicks) {
        InteractionHand mcHand = hand == HandType.MAIN ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int handDir = hand == HandType.MAIN ? 1 : -1;

        var vrPlayer = VRClientPlayers.getPlayer(player);
        if(vrPlayer == null) return;

        PoseParams params = computeParams(item, player,vrPlayer, mcHand, handDir, equipProgress, partialTicks);

        stack.mulPose(params.preRotation);
        stack.translate(params.offsetX, params.offsetY, params.offsetZ);
        stack.mulPose(params.rotation);
        stack.scale(params.scale, params.scale, params.scale);
    }


    private PoseParams computeParams(ItemStack itemStack,
                                     AbstractClientPlayer player,
                                     VRClientPlayer vrPlayer,
                                     InteractionHand mcHand,
                                     int handDir,
                                     float equipProgress,
                                     float partialTicks) {
        boolean isSelf = player instanceof LocalPlayer;

        float gunAngle = vrPlayer.getGunAngle();
        HandType handType = HandType.fromMc(mcHand);

        Quaternionf preRotation = new Quaternionf();

        Quaternionf rotation = new Quaternionf();

        float scale = 0.8f;

        float preYaw = 0;
        float prePitch = 0;
        float preRoll = 0;

        float translateX = 0;
        float translateY = 0;
        float translateZ = 0;

        float yaw = 0;
        float pitch = 0;
        float roll = 0;


        var transformType = getTransformType(itemStack, player, MC.getItemModelResolver());
        switch (transformType) {
            case BLOCK_ITEM, DEFAULT -> {
                scale = 1.0f;
                preYaw = -20;
                translateX = -0.055f;
                translateY = -0.1f;
                translateZ = -0.2f;
                yaw = 0;
                pitch = 90;
            }
            case BLOCK_3D -> {
                scale = 0.7f;
                translateY = 0.005f-0.05f;
                translateZ -= 0.13f;
                if(itemStack.getItem() instanceof BedItem){
                    yaw = -50 + 20;
                }else if(itemStack.getItem() instanceof BannerItem){
                    scale = 1.4f;
                    translateY = 0.0f;
                    yaw = 0;
                    pitch = 180;
                }else {
                    yaw = -50 - 40;
                }
            }
            case CONSUMABLE, COMPASS, BLOCK_STICK, HORN -> {
                long ticks = player.getUseItemRemainingTicks();
                translateY = 0.005f;
                translateZ += 0.006f * Mth.sin(ticks) + 0.02f;
                roll = 180;
                yaw = -135;


            }
            case TOOL ->{
                if(itemStack.getItem() instanceof BrushItem){
                    scale = 0.9f;
                    yaw = -90;
                    pitch = -40;
                    roll = 90;
                } else if (itemStack.getItem() instanceof FlintAndSteelItem) {
                    scale = 1;
                    preYaw = -15f;
                    translateX = 0.06f;
                    translateZ = -0.25f;
                    yaw = 0f;
                    pitch = -90f;
                } else {
                    scale = 1.45f;
                    translateY = 0.005f-0.1F;
                    translateZ -= 0.08F;
                    yaw = -25;
                }
            }
            case STICK -> {
                translateY = 0.005f;
                translateZ = -0.05f;
                yaw = -20;
            }
            case TORCH -> {
                scale = 1.8f;
                preYaw = -10;
                translateX = -0.11f;
                translateZ = 0.08f;
                yaw = 0;
                pitch = 90;
            }
            case MAP -> {
                scale = 1.0f;
                translateX = 0;
                translateY = 0.16f;
                translateZ = -0.075f;
                yaw = -45;
            }
            case FISHING_ROD -> {
                scale = 1.45f;
                yaw = -50;
            }
            case CROSSBOW -> {
                scale = 0.9f;
                translateX = handDir * -0.065f;
                yaw = 0;
                pitch = handDir * 15;
            }
            case BOW -> {
                scale = 0.9f;
                translateX = handDir * 0.075F;
                translateY = 0.1f;
                translateZ = -0.1f;
                yaw = -7;
                roll = handDir * -10;
            }
            case SWORD -> {
                scale = 1.3f;
                translateZ -= 0.08F;
                translateY = 0.005f-0.04f;
                yaw = -25;
            }
            case SHIELD -> {
                scale = 1.0f;
                if (player.isUsingItem() && player.getUsedItemHand() == mcHand) {
                    translateX = handDir == 1 ? -0.25f : 0.17f;
                    translateY = handDir == 1 ? -0.04f : 0f;
                    yaw = -45;
                    pitch = handDir * 45;
                }

            }
            case SPEAR -> {
                scale = 1.3f;
                preYaw = 90;

                float progress = 0.0F;
                // riptide levels became a spin-attack strength (level * 1.5) in 1.21
                float riptideLevel = EnchantmentHelper.getTridentSpinAttackStrength(itemStack, player) / 1.5F;

                if (player.isUsingItem()
                        && player.getUseItemRemainingTicks() > 0
                        && player.getUsedItemHand() == mcHand) {

                    if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                        progress =
                                itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() - partialTicks + 1.0F);

                        if (progress > TridentItem.THROW_THRESHOLD_TIME) {
                            float rotationProgress = progress - TridentItem.THROW_THRESHOLD_TIME;
                            progress = TridentItem.THROW_THRESHOLD_TIME;

                            if (riptideLevel > 0 && player.isInWaterOrRain()) {
                                pitch = -rotationProgress * 10.0F * riptideLevel;
                            }

                            if (isSelf && VisorState.TICK_COUNT % 2 == 0) {
                                ClientContext.inputManager.triggerHapticPulseMicroSec(
                                        handType, 200
                                );
                            }

                            translateX += 0.005f * Mth.sin(Util.getMillis());
                        }
                    }

                    translateX += handDir * 0.01f;
                    translateY = 0.005f-0.55F + progress / 10.0F * 0.25F;


                } else if (player.isAutoSpinAttack() && riptideLevel > 0) {
                    preYaw = -90;
                    translateX = handDir * -0.02f;
                    translateY = 0.005f+0.75F;
                    pitch = (-VisorState.TICK_COUNT * 50) % 360 - partialTicks * 10.0F * riptideLevel;
                } else{
                    preYaw = -30;
                    translateX = handDir * -0.02f;
                    translateY = 0.2f;
                    translateZ = -0.05f;
                    pitch = handDir * 30;
                }
            }
        }

        yaw += gunAngle - 60;

        preRotation.mul(Axis.ZP.rotationDegrees(preRoll));
        preRotation.mul(Axis.YP.rotationDegrees(prePitch));
        preRotation.mul(Axis.XP.rotationDegrees(preYaw));
        rotation.mul(Axis.ZP.rotationDegrees(roll));
        rotation.mul(Axis.YP.rotationDegrees(pitch));
        rotation.mul(Axis.XP.rotationDegrees(yaw));
        return new PoseParams(preRotation, rotation, translateX, translateY, translateZ, scale);
    }
    public static TransformType getTransformType(ItemStack itemStack,
                                                 AbstractClientPlayer player,
                                                 ItemModelResolver itemModelResolver) {
        TransformType transformType = TransformType.DEFAULT;
        Item item = itemStack.getItem();

        if (itemStack.getUseAnimation() == ItemUseAnimation.EAT
                || itemStack.getUseAnimation() == ItemUseAnimation.DRINK) {
            return TransformType.CONSUMABLE;
        }

        //tagged modded shields may also be tools or block items
        if (ItemClassifier.SHIELD.is(itemStack)) {
            return TransformType.SHIELD;
        }

        if (isTool(item)) {
            transformType = TransformType.TOOL;

            if (item instanceof FoodOnAStickItem
                    || item instanceof FishingRodItem) {
                transformType = TransformType.FISHING_ROD;
            }
        }
        else if(isStick(item)){
            transformType = TransformType.STICK;
        }else if(isTorch(item)){
            transformType = TransformType.TORCH;
        }
        else if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();

            if (block instanceof TorchBlock) {
                transformType = TransformType.BLOCK_STICK;
            } else {
                itemModelResolver.updateForTopItem(
                        ITEM_RENDER_STATE, itemStack, ItemDisplayContext.GROUND,
                        false, MC.level, MC.player, 0
                );

                if (ITEM_RENDER_STATE.isGui3d()) {
                    transformType = TransformType.BLOCK_3D;
                } else {
                    transformType = TransformType.BLOCK_ITEM;
                }
            }
        } else if (item instanceof MapItem) {
            transformType = TransformType.MAP;
        } else if (item instanceof BowItem) {
            transformType = TransformType.BOW;

        } else if (itemStack.getUseAnimation() == ItemUseAnimation.TOOT_HORN) {
            transformType = TransformType.HORN;
        } else if (ItemClassifier.SWORD.is(item)) {
            transformType = TransformType.SWORD;
        } else if (ItemClassifier.SHIELD.is(item)) {
            transformType = TransformType.SHIELD;
        } else if (ItemClassifier.SPEAR.is(item)) {
            transformType = TransformType.SPEAR;
        } else if (item instanceof CrossbowItem) {
            transformType = TransformType.CROSSBOW;
        } else if (item instanceof CompassItem || item == Items.CLOCK) {
            transformType = TransformType.COMPASS;
        }
        return transformType;
    }

    public static boolean isTool(final Item item) {
        return item instanceof DiggerItem
                || item instanceof FishingRodItem
                || item instanceof FoodOnAStickItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem
                || item instanceof HoeItem
                || item instanceof AxeItem
                || item instanceof PickaxeItem
                || item instanceof ShovelItem;
    }
    public static boolean isStick(final Item item){
        return item instanceof DebugStickItem
                || item == Items.BONE
                || item == Items.BLAZE_ROD
                || item == Items.BAMBOO
                || item == Items.STICK;
    }
    public static boolean isTorch(final Item item){
        return item == Items.TORCH
                || item == Items.SOUL_TORCH
                || item == Items.REDSTONE_TORCH;
    }

    @Override
    public boolean canApplyPose(@NotNull AbstractClientPlayer player,
                                @NotNull HandType hand,
                                @NotNull ItemStack itemStack) {
        return true;
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.LOWEST;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    private record PoseParams(Quaternionf preRotation,
                              Quaternionf rotation,
                              float offsetX,
                              float offsetY,
                              float offsetZ,
                              float scale) {}
    public enum TransformType {
        DEFAULT,
        BLOCK_3D,
        BLOCK_STICK,
        BLOCK_ITEM,
        SHIELD,
        SWORD,
        TOOL,
        FISHING_ROD,
        BOW,
        SPEAR,
        MAP,
        CONSUMABLE,
        CROSSBOW,
        COMPASS,
        HORN,
        STICK,
        TORCH
    }
}
