package me.phoenixra.visor.core.client.render.gameview.hand;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.gameview.annotations.RegisterVRItemPose;
import me.phoenixra.visor.api.client.render.gameview.hand.VRHandItemPoseBase;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.compatibility.ItemClassifier;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.data.VRClientPose;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
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

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClient.MC;

@RegisterVRItemPose
public class VRItemPoseDefault extends VRHandItemPoseBase {
    private static final String ID = "default";

    public VRItemPoseDefault(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public boolean applyPose(@NotNull AbstractClientPlayer player,
                             @NotNull ControllerHand hand,
                             @NotNull ItemStack itemStack,
                             @NotNull PoseStack poseStack,
                             float equippedProgress,
                             float partialTick
    ) {
        VRClientPose renderPose = ClientContext.player
                .getPose(PoseType.RENDER);

        InteractionHand interactionHand = hand == ControllerHand.MAIN ?
                InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int handDir = hand == ControllerHand.MAIN ? 1 : -1;
        double itemScale = 0.7;
        double offsetX = -0.05;
        double offsetY = 0.005;
        double offsetZ = 0.0;

        double gunAngle = ClientContext.rawPlayerPose.getGunAngle();
        Quaternionf itemRotation = Axis.YP.rotationDegrees(0.0F);
        Quaternionf preRotation = Axis.YP.rotationDegrees(0.0F);
        itemRotation.mul(Axis.XP.rotationDegrees((float) (-110.0D + gunAngle)));

        VRItemPoseType transformType = getTransformType(
                itemStack, player, MC.getItemRenderer()
        );
        switch (transformType){
            default -> {
                if (itemStack.getItem() instanceof ArrowItem) {
                    preRotation = Axis.ZP.rotationDegrees(-180.0F);
                    itemRotation.mul(Axis.XP.rotationDegrees((float) (-gunAngle)));
                }
            }

            case DEFAULT, BLOCK_ITEM -> {
                itemRotation = Axis.ZP.rotationDegrees(180.0F);
                itemRotation.mul(Axis.XP.rotationDegrees(-135.0F));
                itemScale = 0.4F;
                offsetX += 0.08F;
                offsetZ += -0.08F;
            }
            case BLOCK_3D -> {
                itemScale = 0.3F;
                offsetZ += -0.1F;
                offsetX += 0.05F;
            }
            case BLOCK_STICK -> {
                itemRotation = Axis.XP.rotationDegrees(0.0F);
                offsetY += -0.105D + 0.06D * gunAngle / 40.0D;
                offsetZ += -0.1F;
                itemRotation.mul(Axis.XP.rotationDegrees(-45.0F));
                itemRotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
            }

            case CONSUMABLE -> {
                long l = MC.player.getUseItemRemainingTicks();
                itemRotation = Axis.ZP.rotationDegrees(180.0F);
                itemRotation.mul(Axis.XP.rotationDegrees(-135.0F));
                offsetZ = offsetZ + 0.006D * Mth.sin(l);
                offsetZ = offsetZ + (double) 0.02F;
                offsetX += 0.08F;
                itemScale = 0.4F;
            }
            case HORN -> {
                itemScale = 0.3F;
                itemRotation = Axis.XP.rotationDegrees(0.0F);
                offsetY += -0.105D + 0.06D * gunAngle / 40.0D;
                offsetZ += -0.1F;
                itemRotation.mul(Axis.XP.rotationDegrees(-45.0F));
                itemRotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
            }
            case FISHING_ROD -> {
                offsetZ += -0.15F;
                offsetY += -0.02D + gunAngle / 40.0D * 0.1D;
                offsetX += 0.05F;
                itemRotation.mul(Axis.XP.rotationDegrees(40.0F));
                itemScale = 0.8F;
            }

            case MAP -> {
                itemRotation = Axis.XP.rotationDegrees(-45.0F);
                offsetX = 0.0D;
                offsetY = 0.16D;
                offsetZ = -0.075D;
                itemScale = 0.75D;
            }
            case COMPASS -> {
                itemRotation = Axis.YP.rotationDegrees(90.0F);
                itemRotation.mul(Axis.XP.rotationDegrees(25.0F));
                itemScale = 0.4F;
            }
            case TELESCOPE -> {
                preRotation = Axis.XP.rotationDegrees(0.0F);
                itemRotation = Axis.XP.rotationDegrees(0.0F);
                offsetZ = 0.0D;
                offsetY = 0.0D;
                offsetX = 0.0D;
            }

            case CROSSBOW -> {
                offsetX += 0.01F;
                offsetZ += -0.02F;
                offsetY += -0.02F;
                itemScale = 0.5D;
                itemRotation = Axis.XP.rotationDegrees(0.0F);
                itemRotation.mul(Axis.YP.rotationDegrees(10.0F));
            }
            case BOW -> {
                itemRotation = Axis.XP.rotationDegrees(0.0F);
                poseStack.mulPose(Axis.XP.rotationDegrees((float) (-90.0D + gunAngle)));
                offsetY -= 0.25D;
                offsetZ += (double) 0.025F + 0.03D * gunAngle / 40.0D;
                offsetX += -0.0225D;
                itemScale = 1.0D;
            }
            case SHIELD -> {
                boolean reverse = VRClientSettings.isLeftHanded();
                if (reverse) {
                    handDir *= -1;
                }
                itemScale = 0.4F;

                offsetY += 0.18F;
                //
                if (handDir == 1) {
                    itemRotation.mul(Axis.XP.rotationDegrees((float) (105.0D - gunAngle)));
                    offsetX += 0.11F;
                } else {
                    itemRotation.mul(Axis.XP.rotationDegrees((float) (115.0D - gunAngle)));
                    offsetX += -0.015D;
                }
                ////
                offsetZ += 0.1F;

                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == interactionHand) {
                    itemRotation.mul(Axis.XP.rotationDegrees(handDir * 5F));
                    itemRotation.mul(Axis.ZP.rotationDegrees(-5F));

                    if (handDir == 1) {
                        offsetY += -0.12F;
                        offsetZ += -.1F;
                        offsetX += .04F;
                    } else {
                        offsetY += -0.12F;
                        offsetZ += -.11F;
                        offsetX += 0.19F;
                    }


                    ////
                    if (player.isBlocking()) {
                        itemRotation.mul(Axis.YP.rotationDegrees((float) handDir * 90.0F));
                    } else {
                        itemRotation.mul(Axis.YP.rotationDegrees((1.0F - equippedProgress) * (float) handDir * 90.0F));
                    }
                    ////
                }
                ////
                itemRotation.mul(Axis.YP.rotationDegrees((float) handDir * -90.0F));
            }
            case SPEAR -> {
                itemRotation = Axis.XP.rotationDegrees(0.0F);
                offsetX += -0.135F;
                offsetZ = offsetZ + (double) 0.575F;
                itemScale = 0.6F;
                float state = 0.0F;
                boolean flag5 = false;
                int riptide = 0;

                if (player.isUsingItem() && player.getUseItemRemainingTicks() > 0 && player.getUsedItemHand() == interactionHand) {
                    flag5 = true;
                    riptide = EnchantmentHelper.getRiptide(itemStack);

                    if (riptide <= 0 || player.isInWaterOrRain()) {
                        state = (float) itemStack.getUseDuration()
                                - ((float) MC.player.getUseItemRemainingTicks()
                                - partialTick + 1.0F);

                        if (state > 10.0F) {
                            state = 10.0F;

                            if (riptide > 0 && player.isInWaterOrRain()) {
                                poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-VisorState.TICK_COUNT * 10 * riptide % 360) - partialTick * 10.0F * (float) riptide));
                            }

                            if (VisorState.FRAME_COUNT % 4L == 0L) {
                                /*ClientContext.vrApp.getInputManager()
                                        .triggerHapticPulse(
                                                ControllerHand.fromInt(hand == ControllerHand.MAIN ? 0 : 1),
                                                200
                                        );*/
                            }
                            offsetX += 0.003D * Mth.sin(Util.getMillis());
                        }
                    }
                }

                if (player.isAutoSpinAttack()) {
                    riptide = 5;
                    offsetZ += -0.15F;
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float) (-VisorState.TICK_COUNT * 10 * riptide % 360) - partialTick * 10.0F * (float) riptide));
                    flag5 = true;
                }

                if (!flag5) {
                    offsetY += 0.0D + 0.2D * gunAngle / 40.0D;
                    itemRotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
                }

                itemRotation.mul(Axis.XP.rotationDegrees(-65.0F));
                offsetZ = offsetZ + (double) (-0.75F + state / 10.0F * 0.25F);
            }
        }

        poseStack.mulPose(preRotation);
        poseStack.translate(offsetX, offsetY, offsetZ);
        poseStack.mulPose(itemRotation);
        poseStack.scale((float) itemScale, (float) itemScale, (float) itemScale);
        return true;
    }

    public static VRItemPoseType getTransformType(ItemStack itemStack,
                                                  AbstractClientPlayer player,
                                                  ItemRenderer itemRenderer) {
        VRItemPoseType transformType = VRItemPoseType.DEFAULT;
        Item item = itemStack.getItem();

        if (itemStack.getUseAnimation() == UseAnim.EAT
                || itemStack.getUseAnimation() == UseAnim.DRINK) {
            return VRItemPoseType.CONSUMABLE;
        }
        if (item instanceof BlockItem) {
            Block block = ((BlockItem) item).getBlock();

            if (block instanceof TorchBlock) {
                transformType = VRItemPoseType.BLOCK_STICK;
            } else {
                BakedModel model = itemRenderer.getModel(
                        itemStack, MC.level, MC.player, 0
                );

                if (model.isGui3d()) {
                    transformType = VRItemPoseType.BLOCK_3D;
                } else {
                    transformType = VRItemPoseType.BLOCK_ITEM;
                }
            }
        } else if (item instanceof MapItem) {
            transformType = VRItemPoseType.MAP;
        } else if (item instanceof BowItem) {
            transformType = VRItemPoseType.BOW;

        } else if (itemStack.getUseAnimation() == UseAnim.TOOT_HORN) {
            transformType = VRItemPoseType.HORN;
        } else if (ItemClassifier.SWORD.is(item)) {
            transformType = VRItemPoseType.SWORD;
        } else if (ItemClassifier.SHIELD.is(item)) {
            transformType = VRItemPoseType.SHIELD;
        } else if (ItemClassifier.SPEAR.is(item)) {
            transformType = VRItemPoseType.SPEAR;
        } else if (item instanceof CrossbowItem) {
            transformType = VRItemPoseType.CROSSBOW;
        } else if (item instanceof CompassItem || item == Items.CLOCK) {
            transformType = VRItemPoseType.COMPASS;
        } else {
            if (isTool(item)) {
                transformType = VRItemPoseType.TOOL;

                if (item instanceof FoodOnAStickItem
                        || item instanceof FishingRodItem) {
                    transformType = VRItemPoseType.FISHING_ROD;
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
    public int getPriority() {
        return 0;
    }
    @Override
    public @NotNull String getId() {
        return ID;
    }

    public enum VRItemPoseType {
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
