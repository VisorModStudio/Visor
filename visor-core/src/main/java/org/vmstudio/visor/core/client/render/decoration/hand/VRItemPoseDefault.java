package org.vmstudio.visor.core.client.render.decoration.hand;


import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.compatibility.mcversion.McUseAnim;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionUtils;
import org.vmstudio.visor.api.client.input.HapticFeedback;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Util;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
//? if >=1.21.4 {
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
//?} else {
/*import net.minecraft.client.resources.model.BakedModel;
*///?}
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
//? if >=26.3 {
import net.minecraft.tags.ItemTags;
//?}
//? if >=1.21.11 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Ease;
import net.minecraft.world.item.component.KineticWeapon;
//?}
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.common.player.VRPlayer;
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
 *     and based on Meta Quest 3s controllers. The values are authored
 *     in the aim frame, {@link VRHandItemPose#getAimToGripRotation}
 *     re-anchors them onto the grip frame so other controllers match.
 * </p>
 */
@RegisterVRItemPose
public class VRItemPoseDefault extends VRHandItemPose {
    private static final String ID = "default";
    private static final Vector3fc SPEAR_GRIP = new Vector3f(0.0f, -0.2743f, 0.121f);

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

        McRenderUtils.rotate(stack, params.preRotation);
        stack.translate(params.offsetX, params.offsetY, params.offsetZ);
        McRenderUtils.rotate(stack, params.rotation);
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

        HandType handType = HandType.fromMc(mcHand);
        Quaternionfc aimToGrip = getAimToGripRotation(vrPlayer, handType);

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


        var transformType = getTransformType(itemStack, player);
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
                //? if >=26.3 {
                if(itemStack.is(ItemTags.BEDS)){
                //?} else {
                /*if(itemStack.getItem() instanceof BedItem){
                *///?}
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
            case MACE -> {
                scale = 1.1f;
                translateZ -= 0.1F;
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
            case TRIDENT-> {
                scale = 1.3f;
                preYaw = 90;

                float progress = 0.0F;
                float riptideLevel = McVersionUtils.riptideStrength(itemStack, player);

                if (player.isUsingItem()
                        && player.getUseItemRemainingTicks() > 0
                        && player.getUsedItemHand() == mcHand) {

                    if (riptideLevel <= 0 || player.isInWaterOrRain()) {
                        progress =
                                McVersionUtils.useDuration(itemStack, player) - (player.getUseItemRemainingTicks() - partialTicks + 1.0F);

                        if (progress > TridentItem.THROW_THRESHOLD_TIME) {
                            float rotationProgress = progress - TridentItem.THROW_THRESHOLD_TIME;
                            progress = TridentItem.THROW_THRESHOLD_TIME;

                            if (riptideLevel > 0 && player.isInWaterOrRain()) {
                                pitch = -rotationProgress * 10.0F * riptideLevel;
                            }

                            if (isSelf && VisorState.TICK_COUNT % 2 == 0) {
                                ClientContext.inputManager.triggerHapticPulseMicroSec(
                                        handType, HapticFeedback.RIPTIDE_SPIN
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
            case SPEAR-> {
                scale = 1.3f;
                yaw = -90;
                roll = -90;
                SpearMotion motion = spearMotion(itemStack, player, mcHand, partialTicks);
                preYaw = motion.dip();
                prePitch = motion.sway();
                Vector3f pivot = new Vector3f(SPEAR_GRIP)
                        .rotate(new Quaternionf()
                                .rotationY(prePitch * Mth.DEG_TO_RAD)
                                .rotateX(preYaw * Mth.DEG_TO_RAD)
                                .conjugate())
                        .sub(SPEAR_GRIP);
                translateX = -0.1625f + pivot.x;
                translateY = -0.2743f + pivot.y;
                translateZ = -0.0773f + pivot.z - motion.slide();
            }
        }

        boolean aimLocked = transformType == TransformType.SPEAR;
        if (!aimLocked) {
            yaw -= VRPlayer.DEFAULT_GUN_ANGLE;
        }

        preRotation.mul(Axis.ZP.rotationDegrees(preRoll));
        preRotation.mul(Axis.YP.rotationDegrees(prePitch));
        preRotation.mul(Axis.XP.rotationDegrees(preYaw));
        rotation.mul(Axis.ZP.rotationDegrees(roll));
        rotation.mul(Axis.YP.rotationDegrees(pitch));
        rotation.mul(Axis.XP.rotationDegrees(yaw));
        if (!aimLocked) {
            rotation.mul(aimToGrip);
        }
        return new PoseParams(preRotation, rotation, translateX, translateY, translateZ, scale);
    }

    private static SpearMotion spearMotion(ItemStack itemStack,
                                           AbstractClientPlayer player,
                                           InteractionHand mcHand,
                                           float partialTicks) {
        float slide = 0;
        float dip = 0;
        float sway = 0;
        //? if >=1.21.11 {
        KineticWeapon weapon = itemStack.get(DataComponents.KINETIC_WEAPON);
        float reach = weapon != null ? weapon.forwardMovement() : 0.38f;

        if (McVersionUtils.swingingArm(player) == mcHand && McVersionUtils.isStabSwing(player)) {
            float attack = McVersionUtils.attackAnim(player, partialTicks);
            float windUp = Ease.inOutSine(progress(attack, 0.0f, 0.05f));
            float thrust = Ease.outBack(progress(attack, 0.05f, 0.2f));
            float retract = Ease.inOutExpo(progress(attack, 0.4f, 1.0f));
            slide += reach * (thrust - retract) - 0.08f * (windUp - thrust);
        }

        if (weapon != null && player.isUsingItem() && player.getUsedItemHand() == mcHand) {
            float time = player.getTicksUsingItem() + partialTicks;
            int delay = weapon.delayTicks();
            int dismountEnd = delay + weapon.dismountConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0);
            int knockbackEnd = delay + weapon.knockbackConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0);
            int damageEnd = delay + weapon.damageConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0);

            float spent = progress(time, damageEnd - 5, damageEnd);
            float swayAmount = Ease.outCirc(progress(time, dismountEnd - 20, dismountEnd)) * (1.0f - Ease.inCirc(spent));
            float hit = player.getTicksSinceLastKineticHitFeedback(partialTicks);
            float recoil = Ease.outQuart(progress(hit, 1, 3)) - Ease.inOutSine(progress(hit, 3, 10));

            slide += reach * Ease.inOutBack(progress(time, 0, delay)) * (1.0f - spent) - 0.25f * recoil;
            dip = -15.0f * Ease.inOutSine(progress(time, knockbackEnd - 20, knockbackEnd + 20)) * (1.0f - spent)
                    + swayAmount * Mth.sin(time * 30.0f * Mth.DEG_TO_RAD);
            sway = 2.5f * swayAmount * Mth.sin(time * 19.0f * Mth.DEG_TO_RAD);
        }
        //?}
        return new SpearMotion(slide, dip, sway);
    }

    private static float progress(float time, float start, float end) {
        if (end <= start) {
            return time >= end ? 1.0f : 0.0f;
        }
        return Mth.clamp((time - start) / (end - start), 0.0f, 1.0f);
    }

    public static boolean isSpearStab(AbstractClientPlayer player, InteractionHand hand) {
        return McVersionUtils.isStabSwing(player) && ItemClassifier.SPEAR.is(player.getItemInHand(hand));
    }
    //? if >=1.21.4 {
    private static final ItemStackRenderState ITEM_RENDER_STATE = new ItemStackRenderState();
    //?}

    public static TransformType getTransformType(ItemStack itemStack,
                                                 AbstractClientPlayer player) {
        TransformType transformType = TransformType.DEFAULT;
        Item item = itemStack.getItem();

        if (McVersionUtils.useAnimation(itemStack) == McUseAnim.EAT
                || McVersionUtils.useAnimation(itemStack) == McUseAnim.DRINK) {
            return TransformType.CONSUMABLE;
        }

        //tagged modded shields may also be tools or block items
        if (ItemClassifier.SHIELD.is(itemStack)) {
            return TransformType.SHIELD;
        }

        if (ItemClassifier.SPEAR.is(itemStack)) {
            return TransformType.SPEAR;
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
                //? if >=1.21.5 {
                // updateForTopItem clears the reused state before filling it
                ItemModelResolver resolver = MC.getItemModelResolver();
                resolver.updateForTopItem(ITEM_RENDER_STATE, itemStack, ItemDisplayContext.GROUND,
                        MC.level, MC.player, 0);
                boolean gui3d = ITEM_RENDER_STATE.usesBlockLight();
                //?} elif >=1.21.4 {
                /*ItemModelResolver resolver = MC.getItemModelResolver();
                resolver.updateForTopItem(ITEM_RENDER_STATE, itemStack, ItemDisplayContext.GROUND,
                        false, MC.level, MC.player, 0);
                boolean gui3d = ITEM_RENDER_STATE.isGui3d();
                *///?} else {
                /*BakedModel model = MC.getItemRenderer().getModel(
                        itemStack, MC.level, MC.player, 0
                );
                boolean gui3d = model.isGui3d();
                *///?}

                if (gui3d) {
                    transformType = TransformType.BLOCK_3D;
                } else {
                    transformType = TransformType.BLOCK_ITEM;
                }
            }
        } else if (item instanceof MapItem) {
            transformType = TransformType.MAP;
        } else if (item instanceof BowItem) {
            transformType = TransformType.BOW;

        } else if (McVersionUtils.useAnimation(itemStack) == McUseAnim.TOOT_HORN) {
            transformType = TransformType.HORN;
        } else if (ItemClassifier.MACE.is(item)) {
            transformType = TransformType.MACE;
        } else if (ItemClassifier.SWORD.is(item)) {
            transformType = TransformType.SWORD;
        } else if (ItemClassifier.SHIELD.is(item)) {
            transformType = TransformType.SHIELD;
        } else if (ItemClassifier.TRIDENT.is(item)) {
            transformType = TransformType.TRIDENT;
        } else if (item instanceof CrossbowItem) {
            transformType = TransformType.CROSSBOW;
        } else if (item instanceof CompassItem || item == Items.CLOCK) {
            transformType = TransformType.COMPASS;
        }
        return transformType;
    }

    public static boolean isTool(final Item item) {
        //? if >=26.3 {
        return ItemClassifier.isDiggerTool(item)
                || item instanceof FishingRodItem
                || item instanceof FoodOnAStickItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem;
        //?} else {
        /*return ItemClassifier.isDiggerTool(item)
                || item instanceof FishingRodItem
                || item instanceof FoodOnAStickItem
                || item instanceof FlintAndSteelItem
                || item instanceof BrushItem
                || item instanceof HoeItem
                || item instanceof AxeItem
                || ItemClassifier.isPickaxe(item)
                || item instanceof ShovelItem;
        *///?}
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
    private record SpearMotion(float slide, float dip, float sway) {}
    public enum TransformType {
        DEFAULT,
        BLOCK_3D,
        BLOCK_STICK,
        BLOCK_ITEM,
        SHIELD,
        SWORD,
        MACE,
        TOOL,
        FISHING_ROD,
        BOW,
        TRIDENT,
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
