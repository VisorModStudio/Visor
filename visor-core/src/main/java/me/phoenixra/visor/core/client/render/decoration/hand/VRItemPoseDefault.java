package me.phoenixra.visor.core.client.render.decoration.hand;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.render.decoration.annotations.RegisterVRItemPose;
import me.phoenixra.visor.api.client.render.decoration.hand.VRHandItemPose;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.compatibility.ItemClassifier;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import me.phoenixra.visor.core.client.ClientContext;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;


@RegisterVRItemPose
public class VRItemPoseDefault extends VRHandItemPose {
    private static final String ID = "default";

    public VRItemPoseDefault(@NotNull VisorAddon owner) {
        super(owner);
    }

    @Override
    public void applyPose(@NotNull PoseStack stack,
                          @NotNull AbstractClientPlayer player,
                          @NotNull ControllerHand hand,
                          @NotNull ItemStack item,
                          float equipProgress,
                          float partialTicks) {

        InteractionHand mcHand = hand == ControllerHand.MAIN ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        int handDir = hand == ControllerHand.MAIN ? 1 : -1;
        double gunAngle = ClientContext.rawPoseHandler.getGunAngle();


        PoseParams params = computeParams(item, player, mcHand, handDir, (float) gunAngle, equipProgress, partialTicks);


        stack.mulPose(params.preRotation);
        stack.translate(params.offsetX, params.offsetY, params.offsetZ);
        stack.mulPose(params.rotation);
        stack.scale(params.scale, params.scale, params.scale);
    }

    //@TODO rework legacy code
    private PoseParams computeParams(ItemStack item,
                                     AbstractClientPlayer player,
                                     InteractionHand mcHand,
                                     int handDir,
                                     float gunAngle,
                                     float equipProgress,
                                     float partialTicks) {
        // defaults
        float scale = 0.7f;
        float translateX = -0.05f, translateY = 0.005f, translateZ = 0f;
        Quaternionf preRotation = Axis.YP.rotationDegrees(0);
        Quaternionf rotation = Axis.YP.rotationDegrees(0);
        rotation.mul(Axis.XP.rotationDegrees(-110 + gunAngle));

        var transformType = getTransformType(item, player, MC.getItemRenderer());
        switch (transformType) {
            case BLOCK_ITEM, DEFAULT:
                if (item.getItem() instanceof ArrowItem) {
                    preRotation = Axis.ZP.rotationDegrees(-180);
                    rotation = Axis.XP.rotationDegrees(-gunAngle);
                } else {
                    rotation = Axis.ZP.rotationDegrees(180);
                    rotation.mul(Axis.XP.rotationDegrees(-135));
                    scale = 0.4f;
                    translateX += 0.08f;
                    translateZ -= 0.08f;
                }
                break;
            case BLOCK_3D:
                scale = 0.3f;
                translateX += 0.05f;
                translateZ -= 0.1f;
                break;
            case BLOCK_STICK:
                translateY += -0.105f + 0.06f * gunAngle / 40;
                translateZ -= 0.1f;
                rotation = Axis.XP.rotationDegrees(-45);
                rotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
                break;
            case CONSUMABLE:
                long ticks = player.getUseItemRemainingTicks();
                rotation = Axis.ZP.rotationDegrees(180);
                rotation.mul(Axis.XP.rotationDegrees(-135));
                translateZ += 0.006f * Mth.sin(ticks) + 0.02f;
                translateX += 0.08f;
                scale = 0.4f;
                break;
            case MAP:
                preRotation = Axis.YP.rotationDegrees(0);
                rotation = Axis.XP.rotationDegrees(-45);
                translateX = 0;
                translateY = 0.16f;
                translateZ = -0.075f;
                scale = 0.75f;
                break;
            case COMPASS:
                rotation = Axis.YP.rotationDegrees(90);
                rotation.mul(Axis.XP.rotationDegrees(25));
                scale = 0.4f;
                break;
            case FISHING_ROD:
                translateX += 0.05f;
                translateY += -0.02f + gunAngle / 40 * 0.1f;
                translateZ -= 0.15f;
                rotation.mul(Axis.XP.rotationDegrees(40));
                scale = 0.8f;
                break;
            case BOW:
                rotation.mul(Axis.XP.rotationDegrees(90.0F - gunAngle));
                translateY += -0.1F;
                translateZ += 0.1F;
                break;
            case SHIELD:
                if (VRClientSettings.isLeftHanded()) handDir *= -1;
                scale = 0.4f;
                translateY += 0.18f;
                translateZ += 0.1f;
                rotation.mul(Axis.XP.rotationDegrees((float) ((handDir == 1 ? 105 : 115) - gunAngle)));
                translateX += handDir == 1 ? 0.11f : -0.015f;
                if (player.isUsingItem() && player.getUsedItemHand() == mcHand) {
                    rotation.mul(Axis.XP.rotationDegrees(handDir * 5));
                    rotation.mul(Axis.ZP.rotationDegrees(-5));
                    translateY -= 0.12f;
                    translateZ -= handDir == 1 ? 0.1f : 0.11f;
                    translateX += handDir == 1 ? 0.04f : 0.19f;
                    rotation.mul(Axis.YP.rotationDegrees(handDir * (player.isBlocking() ? 90 : (1 - equipProgress) * 90)));
                }
                rotation.mul(Axis.YP.rotationDegrees((float) handDir * -90));
                break;
            case SPEAR:
                rotation = Axis.XP.rotationDegrees(-65);
                scale = 0.6f;
                translateX -= 0.135f;
                translateZ += 0.575f;
                if (player.isUsingItem() && player.getUsedItemHand() == mcHand) {
                    float duration = item.getUseDuration() - (MC.player.getUseItemRemainingTicks() - partialTicks + 1);
                    if (duration > 10) duration = 10;
                    if (VisorState.FRAME_COUNT % 4 == 0 && duration >= 10) {
                        ClientContext.inputManager
                                .triggerHapticPulse(
                                        ControllerHand.fromInt(mcHand == InteractionHand.MAIN_HAND ? 0 : 1),
                                        0.0002f
                                );
                    }
                    translateX += 0.003f * Mth.sin(Util.getMillis());
                } else {
                    translateY += 0.2f * gunAngle / 40;
                    rotation.mul(Axis.XP.rotationDegrees((float) gunAngle));
                }
                break;
            default:
                break;
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
    public boolean canApplyPose(@NotNull AbstractClientPlayer player, @NotNull ControllerHand hand, @NotNull ItemStack itemStack) {
        return true;
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.LOWEST;
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
