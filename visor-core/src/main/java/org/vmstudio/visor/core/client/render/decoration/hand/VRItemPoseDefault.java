package org.vmstudio.visor.core.client.render.decoration.hand;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import org.vmstudio.visor.api.client.render.decoration.hand.VRHandItemPose;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.compatibility.ItemClassifier;
import org.vmstudio.visor.core.client.VisorState;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import org.vmstudio.visor.core.client.ClientContext;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


@RegisterVRItemPose
public class VRItemPoseDefault extends VRHandItemPose {
    private static final String ID = "default";

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


        PoseParams params = computeParams(item, player, mcHand, handDir, equipProgress, partialTicks);

        stack.mulPose(params.preRotation);
        stack.translate(params.offsetX, params.offsetY, params.offsetZ);
        stack.mulPose(params.rotation);
        stack.scale(params.scale, params.scale, params.scale);
    }


    private PoseParams computeParams(ItemStack item,
                                     AbstractClientPlayer player,
                                     InteractionHand mcHand,
                                     int handDir,
                                     float equipProgress,
                                     float partialTicks) {
        float gunAngle = ClientContext.rawPoseHandler.getGunAngle();
        HandType handType = HandType.fromMc(mcHand);
        // defaults
        float scale = 0.7f;
        float translateX = 0.0f, translateY = 0.005f, translateZ = 0.0f;
        Quaternionf preRotation = Axis.YP.rotationDegrees(0);
        Quaternionf rotation = Axis.XP.rotationDegrees(-110 + gunAngle);

        var transformType = getTransformType(item, player, MC.getItemRenderer());
        switch (transformType) {
            case BLOCK_ITEM, DEFAULT -> {
                if (item.getItem() instanceof ArrowItem) {
                    preRotation = Axis.ZP.rotationDegrees(-180);
                    rotation = Axis.XP.rotationDegrees(-gunAngle);
                } else if (item.is(Items.STICK)) {
                    scale = 1.0f;
                    translateY = 0.0f;
                    rotation = Axis.XP.rotationDegrees(0);
                } else {
                    rotation = Axis.ZP.rotationDegrees(180);
                    rotation.mul(Axis.XP.rotationDegrees(-135));
                    rotation.mul(Axis.YP.rotationDegrees(90));
                    translateX += 0.04f;
                    translateZ -= 0.12f;
                }
            }
            case BLOCK_3D -> {
                translateZ -= 0.1f;
            }
            case CONSUMABLE, COMPASS, BLOCK_STICK, HORN, TOOL -> {
                long ticks = player.getUseItemRemainingTicks();
                rotation = Axis.ZP.rotationDegrees(180);
                rotation.mul(Axis.XP.rotationDegrees(-135));
                translateZ += 0.006f * Mth.sin(ticks) + 0.02f;

            }
            case MAP -> {
                preRotation = Axis.YP.rotationDegrees(0);
                rotation = Axis.XP.rotationDegrees(-45);
                translateX = 0;
                translateY = 0.16f;
                translateZ = -0.075f;
                scale = 0.75f;
            }
            case FISHING_ROD -> {
                translateY += -0.18f + gunAngle / 40 * 0.1f;
                translateZ -= 0.10f;
                rotation.mul(Axis.XP.rotationDegrees(40));
                scale = 0.8f;
            }
            // FIXME(!!): crossbow have shadow-issue with XP rotation
            case CROSSBOW -> {
                rotation = Axis.YP.rotationDegrees(-10.0F);
                translateX += 0.04f;
                translateZ += 0.08f;
                translateY += 0.04f;
            }
            case BOW -> {
                rotation.mul(Axis.XP.rotationDegrees(90.0F - gunAngle));
                rotation.mul(Axis.ZP.rotationDegrees(handDir == 1 ? -10.0F : 10.0F));
                translateZ -= 0.06F;
                translateX += 0.04F;
            }
            case SWORD -> {
                rotation.mul(Axis.XP.rotationDegrees(45.0f));
                translateZ -= 0.08F;
                translateY -= 0.01f;
            }
            case SHIELD -> {
                if (ClientContext.localPlayer.isLeftHanded()) handDir *= -1;
                translateY -= 0.04f;
                translateZ += 0.1f;
                rotation.mul(Axis.XP.rotationDegrees((handDir == 1 ? 105 : 115) - gunAngle));
                translateX += handDir == 1 ? 0.015f : -0.015f;

                // FIXME(!!): shield doesn't have interaction animation
                if (player.isUsingItem() && player.getUsedItemHand() == mcHand) {
                    rotation.mul(Axis.XP.rotationDegrees(handDir * 5));
                    rotation.mul(Axis.ZP.rotationDegrees(-5));
                    translateY -= 0.12f;
                    translateZ -= handDir == 1 ? 0.1f : 0.11f;
                    translateX += handDir == 1 ? 0.04f : 0.19f;
                    rotation.mul(Axis.YP.rotationDegrees(handDir * (player.isBlocking() ? 90 : (1 - equipProgress) * 90)));
                }
                rotation.mul(Axis.YP.rotationDegrees((float) handDir * 0));
            }
            case SPEAR -> {
                rotation.identity();
                translateZ += 0.645F;

                float progress = 0.0F;
                boolean charging = false;
                int riptideLevel = 0;

                // FIXME(!!): spear have BAD interaction animation
                if (player.isUsingItem()
                        && player.getUseItemRemainingTicks() > 0
                        && player.getUsedItemHand() == mcHand) {
                    charging = true;
                    riptideLevel = EnchantmentHelper.getRiptide(item);

                    if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                        progress =
                                item.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0F);

                        if (progress > TridentItem.THROW_THRESHOLD_TIME) {
                            float rotationProgress = progress - TridentItem.THROW_THRESHOLD_TIME;
                            progress = TridentItem.THROW_THRESHOLD_TIME;

                            if (riptideLevel > 0 && player.isInWaterOrRain()) {
                                preRotation = Axis.ZP.rotationDegrees(-rotationProgress * 10.0F * riptideLevel);
                            }

                            if (VisorState.TICK_COUNT % 2 == 0) {
                                ClientContext.inputManager.triggerHapticPulseMicroSec(
                                        handType, 200
                                );
                            }

                            translateX += 0.003F * (float) Math.sin(Util.getMillis());
                        }
                    }
                }

                if (player.isAutoSpinAttack()) {
                    riptideLevel = 5;
                    translateZ -= 0.15F;
                    preRotation = Axis.ZP.rotationDegrees(
                            (-VisorState.TICK_COUNT * 10 * riptideLevel) % 360 - partialTicks * 10.0F * riptideLevel);
                    charging = true;
                }

                if (!charging) {
                    translateY += 0.2F * gunAngle / 40.0F;
                    rotation.mul(Axis.XP.rotationDegrees(gunAngle));
                }

                rotation.mul(Axis.XP.rotationDegrees(-65.0F));
                translateZ += -0.75F + progress / 10.0F * 0.25F;
            }
        }
        return new PoseParams(preRotation, rotation, translateX, translateY, translateZ, scale);
    }
    public static TransformType getTransformType(ItemStack itemStack,
                                                 AbstractClientPlayer player,
                                                 ItemRenderer itemRenderer) {
        TransformType transformType = TransformType.DEFAULT;
        Item item = itemStack.getItem();

        if (itemStack.getUseAnimation() == UseAnim.EAT
                || itemStack.getUseAnimation() == UseAnim.DRINK) {
            return TransformType.CONSUMABLE;
        }
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();

            if (block instanceof TorchBlock) {
                transformType = TransformType.BLOCK_STICK;
            } else {
                BakedModel model = itemRenderer.getModel(
                        itemStack, MC.level, MC.player, 0
                );

                if (model.isGui3d()) {
                    transformType = TransformType.BLOCK_3D;
                } else {
                    transformType = TransformType.BLOCK_ITEM;
                }
            }
        } else if (item instanceof MapItem) {
            transformType = TransformType.MAP;
        } else if (item instanceof BowItem) {
            transformType = TransformType.BOW;

        } else if (itemStack.getUseAnimation() == UseAnim.TOOT_HORN) {
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
        } else {
            if (isTool(item)) {
                transformType = TransformType.TOOL;

                if (item instanceof FoodOnAStickItem
                        || item instanceof FishingRodItem) {
                    transformType = TransformType.FISHING_ROD;
                }
            }
        }
        return transformType;
    }

    public static boolean isTool(final Item item) {
        return item instanceof DiggerItem
                || item instanceof ArrowItem
                || item instanceof FishingRodItem
                || item instanceof FoodOnAStickItem
                || item instanceof ShearsItem
                || item == Items.BONE
                || item == Items.BLAZE_ROD
                || item == Items.BAMBOO
                || item == Items.TORCH
                || item == Items.REDSTONE_TORCH
                || item == Items.STICK
                || item instanceof DebugStickItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem
                || item instanceof HoeItem
                || item instanceof AxeItem
                || item instanceof PickaxeItem
                || item instanceof ShovelItem;
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
        BOW_DRAWING,
        SPEAR,
        MAP,
        CONSUMABLE,
        CROSSBOW,
        TELESCOPE,
        COMPASS,
        HORN
    }
}
