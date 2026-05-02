package org.vmstudio.visor.core.client.render.decoration.hand;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.PropertyBool;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.PropertyFloat;
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
import org.vmstudio.visor.core.client.gui.overlays.builtin.VROverlayItemPoseTest;

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


    private PoseParams computeParams(ItemStack itemStack,
                                     AbstractClientPlayer player,
                                     InteractionHand mcHand,
                                     int handDir,
                                     float equipProgress,
                                     float partialTicks) {
        float gunAngle = ClientContext.rawPoseHandler.getGunAngle();
        HandType handType = HandType.fromMc(mcHand);
        var options = ClientContext.overlayManager.getOverlay(
                VROverlayItemPoseTest.ID,
                VROverlayItemPoseTest.class
        );
        var properties = options.getProperties();
        // defaults
        boolean active = properties.getProperty(
                "active",
                PropertyBool.class
        ).getValue();

        Quaternionf preRotation = new Quaternionf();

        Quaternionf rotation = new Quaternionf();

        float scale = 0.8f;

        float translateX = 0;
        float translateY = 0.005f;
        float translateZ = 0;

        float preYaw = 0;
        float prePitch = 0;
        float preRoll = 0;

        float yaw = -110 + gunAngle;
        float pitch = 0;
        float roll = 0;

        if(active){
            scale = properties.getProperty(
                    "scale",
                    PropertyFloat.class
            ).getValue();

            translateX = properties.getProperty(
                    "translate_x",
                    PropertyFloat.class
            ).getValue();
            translateY = properties.getProperty(
                    "translate_y",
                    PropertyFloat.class
            ).getValue();
            translateZ = properties.getProperty(
                    "translate_z",
                    PropertyFloat.class
            ).getValue();


            preYaw = properties.getProperty("pre_yaw", PropertyFloat.class).getValue();
            prePitch = properties.getProperty("pre_pitch", PropertyFloat.class).getValue();
            preRoll = properties.getProperty("pre_roll", PropertyFloat.class).getValue();

            yaw = properties.getProperty("yaw", PropertyFloat.class).getValue();
            pitch = properties.getProperty("pitch", PropertyFloat.class).getValue();
            roll = properties.getProperty("roll", PropertyFloat.class).getValue();

            preRotation.mul(Axis.ZP.rotationDegrees(preRoll));
            preRotation.mul(Axis.YP.rotationDegrees(prePitch));
            preRotation.mul(Axis.XP.rotationDegrees(preYaw));
            rotation.mul(Axis.ZP.rotationDegrees(roll));
            rotation.mul(Axis.YP.rotationDegrees(pitch));
            rotation.mul(Axis.XP.rotationDegrees(yaw));
            return new PoseParams(preRotation, rotation, translateX, translateY, translateZ, scale);
        }

        var transformType = getTransformType(itemStack, player, MC.getItemRenderer());
        switch (transformType) {
            case BLOCK_ITEM, DEFAULT -> {
                scale = 1.0f;
                if (itemStack.getItem() instanceof ArrowItem) {
                    preRoll = -180;
                    yaw = -gunAngle;
                } else if (itemStack.is(Items.STICK)) {
                    scale = 1.0f;
                    translateY = 0.0f;
                    yaw = 0;
                }else if(itemStack.getItem() instanceof BannerItem){
                    scale = 1.4f;
                    translateY = 0.0f;
                    yaw = 0;
                    pitch = 180;
                }else {
                    preYaw = -20;
                    yaw = 0;
                    pitch = 90;
                    translateX = -0.055f;
                    translateY = -0.1f;
                    translateZ = -0.2f;
                }
            }
            case BLOCK_3D -> {
                scale = 0.7f;
                translateZ -= 0.13f;
                translateY -= 0.05f;
                if(itemStack.getItem() instanceof BedItem){
                    yaw += 20;
                }else if(itemStack.getItem() instanceof BannerItem){
                    scale = 1.4f;
                    translateY = 0.0f;
                    yaw = 0;
                    pitch = 180;
                }else {
                    yaw += -40;
                }
            }
            case CONSUMABLE, COMPASS, BLOCK_STICK, HORN -> {
                long ticks = player.getUseItemRemainingTicks();
                roll = 180;
                yaw = -135;
                translateZ += 0.006f * Mth.sin(ticks) + 0.02f;

            }
            case TOOL ->{
                if(itemStack.getItem() instanceof BrushItem){
                    scale = 0.9f;
                    yaw = -90;
                    pitch = -40;
                    roll = 90;
                    translateY = 0;
                } else if (itemStack.getItem() instanceof FlintAndSteelItem) {
                    scale = 1;
                    translateX = 0.06f;
                    translateY = 0;
                    translateZ = -0.25f;
                    preYaw = -15f;
                    yaw = 0f;
                    pitch = -90f;
                } else {
                    scale = 1.45f;
                    yaw = -25;
                    translateZ -= 0.08F;
                    translateY -= 0.1F;
                }
            }
            case STICK -> {
                translateZ = -0.05f;
                yaw = -20;
            }
            case TORCH -> {
                scale = 1.8f;
                translateX = -0.11f;
                translateY = 0;
                translateZ = 0.08f;
                preYaw = -10;
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
                translateY = 0;
                yaw = -50;
            }
            case CROSSBOW -> {
                scale = 0.9f;
                translateX = handDir * -0.065f;
                translateY = 0;
                yaw = 0;
                pitch = handDir * 15;
            }
            case BOW -> {
                scale = 0.9f;
                translateX = handDir * 0.075F;
                translateY = 0.1f;
                translateZ = -0.1f;
                yaw = -7;
                roll = handDir * -8;
            }
            case SWORD -> {
                scale = 1.3f;
                yaw = -25;
                translateZ -= 0.08F;
                translateY -= 0.04f;
            }
            case SHIELD -> {

                if (player.isUsingItem() && player.getUsedItemHand() == mcHand) {
                    translateY -= 0.04f;;
                    translateX = handDir * -0.17f;
                    yaw = -45;
                    pitch = handDir * 45;

                }else{
                    translateY -= 0.04f;
                    translateZ += 0.1f;
                    translateX += handDir * 0.015f;
                    yaw += (handDir == 1 ? 105 : 115) - gunAngle;
                }

            }
            case SPEAR -> {
                scale = 1.3f;
                translateY = 0;
                yaw = 0;

                float progress = 0.0F;
                int riptideLevel = EnchantmentHelper.getRiptide(itemStack);

                if (player.isUsingItem()
                        && player.getUseItemRemainingTicks() > 0
                        && player.getUsedItemHand() == mcHand) {

                    if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                        progress =
                                itemStack.getUseDuration() - (player.getUseItemRemainingTicks() - partialTicks + 1.0F);

                        if (progress > TridentItem.THROW_THRESHOLD_TIME) {
                            float rotationProgress = progress - TridentItem.THROW_THRESHOLD_TIME;
                            progress = TridentItem.THROW_THRESHOLD_TIME;

                            if (riptideLevel > 0 && player.isInWaterOrRain()) {
                                pitch = -rotationProgress * 10.0F * riptideLevel;
                            }

                            if (VisorState.TICK_COUNT % 2 == 0) {
                                ClientContext.inputManager.triggerHapticPulseMicroSec(
                                        handType, 200
                                );
                            }

                            translateX += 0.005f * Mth.sin(Util.getMillis());
                        }
                    }

                    translateX += handDir * 0.01f;
                    translateY += -0.55F + progress / 10.0F * 0.25F;

                    preYaw = 90;

                } else if (player.isAutoSpinAttack() && riptideLevel > 0) {
                    translateX = handDir * -0.02f;
                    preYaw = -90;
                    translateY += 0.75F;
                    pitch = (-VisorState.TICK_COUNT * 50) % 360 - partialTicks * 10.0F * riptideLevel;
                } else{
                    translateX = handDir * -0.02f;
                    translateY = 0.2f;
                    translateZ = -0.05f;
                    preYaw = -30;
                    pitch = handDir * 30;
                }
            }
        }
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
                                                 ItemRenderer itemRenderer) {
        TransformType transformType = TransformType.DEFAULT;
        Item item = itemStack.getItem();

        if (itemStack.getUseAnimation() == UseAnim.EAT
                || itemStack.getUseAnimation() == UseAnim.DRINK) {
            return TransformType.CONSUMABLE;
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
        return item instanceof ArrowItem
                || item instanceof DebugStickItem
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
        BOW_DRAWING,
        SPEAR,
        MAP,
        CONSUMABLE,
        CROSSBOW,
        TELESCOPE,
        COMPASS,
        HORN,
        STICK,
        TORCH
    }
}
