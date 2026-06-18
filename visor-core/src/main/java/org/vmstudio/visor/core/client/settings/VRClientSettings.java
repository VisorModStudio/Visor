package org.vmstudio.visor.core.client.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.SupportedMovement;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardLayout;
import org.vmstudio.visor.core.client.gui.overlays.builtin.keyboard.KeyboardLayouts;
import org.vmstudio.visor.core.client.player.body.VRBodyTypeHandsOnly;
import org.vmstudio.visor.core.client.settings.options.VROptionField;
import org.vmstudio.visor.core.client.settings.options.enums.*;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.core.client.utils.LangHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.Collection;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRClientSettings {


    @Getter
    @VROptionField(excludeForcedChange = true)
    protected static VRPlayMode vrPlayMode = VRPlayMode.ENABLED;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.LEFT_HANDED,
            key = "left_handed",
            excludeForcedChange = true)
    protected static boolean leftHanded = false;


    //----Keyboard
    @Getter
    @VROptionField(key = "keyboard.layouts", category = VROptionCategory.GUI)
    protected static String keyboardLayoutsRaw = KeyboardLayouts.serialize(
            List.of(KeyboardLayout.ENGLISH)
    );
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.KEYBOARD_AUTO_LAYOUT, key = "keyboard.auto_layout")
    protected static boolean keyboardAutoLayout = true;
    //---



    //----Movement

    @VROptionField(widgetType = VROptionWidgetType.MOVEMENT_MODE,
            key = "mode")
    protected static MovementMode movementMode = MovementMode.CONTROLLER;

    @VROptionField(widgetType = VROptionWidgetType.ROTATION_MODE,
            key = "rotation_mode")
    protected static RotationMode rotationMode = RotationMode.HMD;

    @VROptionField(widgetType = VROptionWidgetType.ROTATION_FLY_MODE,
            key = "rotation_fly_mode")
    protected static RotationMode rotationFlyMode = RotationMode.OFFHAND;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.WORLD_ROTATION_STEP,
            key = "world_rotation.step")
    protected static float worldRotationStep = 45f;
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.WORLD_ROTATION_SMOOTH,
            key = "world_rotation.smooth_sensitivity")
    protected static float worldRotationSmoothSensitivity = 0.06f;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.WALK_UP, key = "walk_up")
    protected static boolean walkUpEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.COMPATIBLE_LOOK_DIRECTION,
            key = "compatible_look_direction")
    protected static boolean compatibleLookDirection = false;



    @Getter
    @VROptionField(key = "movement.sprintThreshold")
    protected static float sprintThreshold = 0.9f;


    //----Rendering
    @Getter
    @VROptionField(key = "world_scale", category = VROptionCategory.RENDERING)
    protected static float worldScale = 1.0f;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.MIRROR_MODE, key = "mirror_mode")
    protected static MirrorMode mirrorMode = MirrorMode.CROPPED;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.MIRROR_EYE, key = "mirror_eye")
    protected static EyeType mirrorEye = EyeType.LEFT;

    @Getter
    protected static float eyesFovScale = 1f;

    //FOV changes detection, to apply properly
    @Getter
    private static float eyeFovScaleCurrent = 1.0f;
    @Getter @Setter
    private static boolean eyeFovChanged = false;

    @Getter
    protected static final float renderScaleFactor = 1.0f;
    @Getter
    protected static final float mirrorSmooth = 0.0F;
    @Getter
    protected static final float mirrorCrop = 0.15F;
    //

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.DH_MIRROR_PASSES, key = "dh_mirror_passes")
    protected static boolean dhMirrorPasses = false;

    //----Shaders
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.SHADER_PER_EYE_PIPELINES, key = "per_eye_pipelines")
    protected static boolean shaderPerEyePipelines = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.SHADER_SHARED_SHADOWS, key = "shared_shadows")
    protected static boolean shaderSharedShadows = false;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.SHADER_SHARED_SSBO, key = "shared_ssbo")
    protected static boolean shaderSharedSsbo = true;

    //----Eye Effects
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.LOW_HEALTH_INDICATOR, key = "low_health_indicator")
    protected static boolean lowHealthIndicatorEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.HIT_INDICATOR, key = "hit_indicator")
    protected static boolean hitIndicatorEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.FREEZE_EFFECT, key = "freeze")
    protected static boolean freezeEffectEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.PUMPKIN_EFFECT, key = "pumpkin")
    protected static boolean pumpkinEffectEnabled = true;

    // ---- VR Body rendering
    @Getter @Setter
    @VROptionField
    protected static String defaultVrBody = VRBodyTypeHandsOnly.ID;

    @Getter
    @VROptionField
    protected static float playerModelArmsScale = 0.5F;
    @Getter
    @VROptionField
    protected static float playerModelBodyScale = 1.0F;
    @Getter
    @VROptionField
    protected static float playerModelLegScale = 1.0F;



    //----Main menu
    @Getter
    @VROptionField(key = "main_menu.scene")
    protected static MainMenuSceneMode mainMenuScene = MainMenuSceneMode.SKY;

    @Getter
    @VROptionField(key = "main_menu.panorama.front")
    protected static String panoramaFront = "visor:textures/mainmenu/panorama_front.png";
    @Getter
    @VROptionField(key = "main_menu.panorama.back")
    protected static String panoramaBack = "visor:textures/mainmenu/panorama_back.png";
    @Getter
    @VROptionField(key = "main_menu.panorama.right")
    protected static String panoramaRight = "visor:textures/mainmenu/panorama_right.png";
    @Getter
    @VROptionField(key = "main_menu.panorama.left")
    protected static String panoramaLeft = "visor:textures/mainmenu/panorama_left.png";
    @Getter
    @VROptionField(key = "main_menu.panorama.up")
    protected static String panoramaUp = "visor:textures/mainmenu/panorama_up.png";
    @Getter
    @VROptionField(key = "main_menu.panorama.below")
    protected static String panoramaBelow = "visor:textures/mainmenu/panorama_below.png";

    @Getter
    @VROptionField(key = "main_menu.floor")
    protected static String mainMenuFloor = "minecraft:textures/block/moss_block.png";


    //----GUI && HUD
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.SHADER_GUI_RENDER, key = "shader_gui_render")
    protected static ShaderGUIRenderMode shaderGUIRender = ShaderGUIRenderMode.AFTER_SHADER;


    @Getter
    @VROptionField(widgetType = VROptionWidgetType.SETTINGS_TEXT_SCALE,
            key = "settings_text_scale")
    protected static float settingsTextScale = 0.9f;

    @Getter
    @VROptionField(key = "gui.scale")
    protected static float guiScale = 0;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.HUD_DISABLED_HOTBAR, key = "hud_disabled_hotbar")
    protected static boolean hudDisableHotBar = true;


    //----


    //----Third Person Mirror
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.THIRD_PERSON_FOV, key = "fov")
    protected static float thirdPersonFov = 40;
    @Getter
    @VROptionField(key = "camera.blockPos.x", category = VROptionCategory.RENDERING_THIRD_PERSON)
    protected static float thirdPersonCameraPosX = -1.0f;
    @Getter
    @VROptionField(key = "camera.blockPos.y", category = VROptionCategory.RENDERING_THIRD_PERSON)
    protected static float thirdPersonCameraPosY = 2.4f;
    @Getter
    @VROptionField(key = "camera.blockPos.z", category = VROptionCategory.RENDERING_THIRD_PERSON)
    protected static float thirdPersonCameraPosZ = 2.75f;

    @Getter
    @VROptionField(key = "camera.rotation", category = VROptionCategory.RENDERING_THIRD_PERSON)
    protected static Quaternionfc thirdPersonCameraRotation
            = new Quaternionf(0.2246, 0.1873, 0.0440, -0.9552);


    //----Mixed Reality Mirror
    @Getter
    @VROptionField(
            widgetType = VROptionWidgetType.MIXED_REALITY_RENDER_HANDS,
            key = "render_hands"
    )
    protected static boolean mixedRealityRenderHands = false;

    @Getter
    @VROptionField(
            widgetType = VROptionWidgetType.MIXED_REALITY_AS_GRID_2_X_2,
            key = "as_grid_2_x_2"
    )
    protected static boolean mixedRealityAsGrid2x2 = true;

    @Getter
    @VROptionField(
            widgetType = VROptionWidgetType.MIXED_REALITY_WITH_FIRST_PERSON,
            key = "with_first_person"
    )
    protected static boolean mixedRealityWithFirstPerson = true;

    @Getter
    @VROptionField(
            widgetType = VROptionWidgetType.MIXED_REALITY_ALPHA_MASK,
            key = "alpha_mask"
    )
    protected static boolean mixedRealityAlphaMask = false;

    @Getter
    @VROptionField(
            widgetType = VROptionWidgetType.MIXED_REALITY_FOV,
            key = "fov"
    )
    protected static float mixedRealityFov = 40;

    @Getter
    @VROptionField(key = "keyColor", category = VROptionCategory.RENDERING_MIXED_REALITY)
    protected static AtumColor mixedRealityKeyColor = AtumColor.immutable(0, 0, 0, 255);

    @Getter
    @VROptionField(key = "aspectRatio", category = VROptionCategory.RENDERING_MIXED_REALITY)
    protected static float mixedRealityAspectRatio = 16F / 9F;

    //

    // ---- IMMERSION

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_SNEAK,
            key = "room_sneak")
    protected static boolean roomSneakEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_CRAWL,
            key = "room_crawl")
    protected static boolean roomCrawlEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_CLIMB,
            key = "room_climb")
    protected static boolean roomClimbEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_JUMP,
            key = "room_jump")
    protected static boolean roomJumpEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_SWIM,
            key = "room_swim")
    protected static boolean roomSwimEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_DISMOUNT_VEHICLE,
            key = "room_dismount_vehicle")
    protected static boolean roomDismountVehicleEnabled = true;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_CONSUME,
            key = "room_consume")
    protected static boolean roomConsumeEnabled = true;


    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_SNEAK_THRESHOLD,
            key = "room_sneak.threshold")
    protected static float roomSneakThreshold = 0.85f;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_CRAWL_THRESHOLD,
            key = "room_crawl.threshold")
    protected static float roomCrawlThreshold = 0.7f;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROOM_JUMP_THRESHOLD,
            key = "room_jump.threshold")
    protected static float roomJumpThreshold = 1.05f;



    // ---- OTHER



    @Setter
    @VROptionField(key = "player.full_height", excludeForcedChange = true)
    protected static float fullHeight = VRPlayer.DEFAULT_FULL_HEIGHT;


    public static void setVrPlayMode(VRPlayMode vrPlayMode) {
        if(vrPlayMode != VRPlayMode.DISABLED) {
            VisorState.clearVrInitFailed();
        }
        VRClientSettings.vrPlayMode = vrPlayMode;
        VisorClientImpl.LOGGER.info(
                "Changed VR Play Mode to: {}",
                VRClientSettings.getVrPlayMode()
        );
    }

    public static MovementMode getMoveMode(Player player) {
        var out = movementMode;
        var supported = VRServerSettings.getSupportedMovement();
        if (supported != SupportedMovement.BOTH) {
            out = supported == SupportedMovement.CONTROLLER
                    ? MovementMode.CONTROLLER
                    : MovementMode.TELEPORT;
        }
        if (player.isPassenger()
                || (!player.isPassenger()
                && player.getAbilities().flying)) {
            out = MovementMode.CONTROLLER;
        }
        return out;
    }

    public static RotationMode getRotationMode() {
        if(MC.player != null
                && !MC.player.isPassenger()
                && MC.player.getAbilities().flying){
            return rotationFlyMode;
        }
        return rotationMode;
    }

    public static @NotNull List<KeyboardLayout> getKeyboardLayouts() {
        return KeyboardLayouts.deserialize(keyboardLayoutsRaw);
    }

    public static void setKeyboardLayouts(@NotNull Collection<KeyboardLayout> layouts) {
        keyboardLayoutsRaw = KeyboardLayouts.serialize(layouts);
    }

    /**
     * Includes auto keyboard layout if supported
     */
    public static @NotNull List<KeyboardLayout> getEffectiveKeyboardLayouts() {
        List<KeyboardLayout> base = getKeyboardLayouts();
        KeyboardLayout autoLayout = getAutoKeyboardLayout();
        if (autoLayout == null || base.contains(autoLayout)) {
            return base;
        }
        var merged = new java.util.ArrayList<>(base);
        if(merged.isEmpty()){
            merged.add(KeyboardLayout.ENGLISH);
        }
        merged.add(autoLayout);
        return List.copyOf(merged);
    }

    public static @org.jetbrains.annotations.Nullable KeyboardLayout getAutoKeyboardLayout() {
        if (!keyboardAutoLayout) return null;
        if (MC == null) return null;
        String langCode = MC.options.languageCode;
        return KeyboardLayout.fromLangCode(langCode);
    }

    public static void updateThirdPersonCamera(@NotNull Vector3fc position,
                                               @NotNull Quaternionfc rotation,
                                               boolean save){
        thirdPersonCameraPosX = position.x();
        thirdPersonCameraPosY = position.y();
        thirdPersonCameraPosZ = position.z();
        thirdPersonCameraRotation = new Quaternionf(rotation);
        if(save) {
            ClientContext.settingsManager.saveOptions();
        }
    }


    public static void setEyeFovScaleCurrent(float value) {
        eyeFovScaleCurrent = value;
        eyeFovChanged = true;
    }



    public static final float MIN_CALIBRATION_HEIGHT = VRPlayer.DEFAULT_FULL_HEIGHT / 4;

    public static float getFullHeight() {
        if (fullHeight < 0) {
            return VRPlayer.DEFAULT_FULL_HEIGHT;
        }

        return fullHeight;
    }

    public static boolean tryCalibrateHeight() {
        var hmdData = ClientContext.rawPoseHandler.getHmdData();
        if (!hmdData.isTracking()) {
            return false;
        }

        float height = hmdData.getPivotHistory().averagePosition(0.2f).y;
        if (!Float.isFinite(height) || height < MIN_CALIBRATION_HEIGHT) {
            return false;
        }

        VRClientSettings.setFullHeight(height);
        ClientContext.settingsManager.saveOptions();

        int i = (int) (Math.round(100.0D
                * VRClientSettings.getFullHeight()
                / VRPlayer.DEFAULT_FULL_HEIGHT
        ));
        Minecraft.getInstance().gui.getChat()
                .addMessage(
                        Component.literal(
                                LangHelper.getText(
                                        "visor.messages.height_set",
                                        i
                                )
                        )
                );
        return true;
    }

    public static void calibrateHeight() {
        if (!tryCalibrateHeight()) {
            Minecraft.getInstance().gui.getChat()
                    .addMessage(
                            Component.literal(
                                    LangHelper.getText(
                                            "visor.messages.height_calibration_failed"
                                    )
                            )
                    );
        }
    }
    public static boolean isLimitedSurvivalTeleport() {
        return true; //leave it, for later easier navigation in code to change movement
    }

}
