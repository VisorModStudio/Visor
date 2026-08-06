package org.vmstudio.visor.api.client.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.vmstudio.visor.api.client.VRPlayMode;
import org.vmstudio.visor.api.client.settings.enums.*;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.SupportedMovement;
import org.vmstudio.visor.api.server.VRServerSettings;

public class VRClientSettings {


    @Getter @Setter
    @VROptionField(excludeForcedChange = true)
    protected static VRPlayMode vrPlayMode = VRPlayMode.ENABLED;

    @Getter
    @VROptionField(key = "left_handed",
            category = VROptionCategory.CONTROLS,
            excludeForcedChange = true)
    protected static boolean leftHanded = false;


    //----Keyboard
    @Getter @Setter
    @VROptionField(key = "keyboard.layouts", category = VROptionCategory.GUI)
    protected static String keyboardLayoutsRaw = "ENGLISH";

    @Getter
    @VROptionField(key = "keyboard.auto_layout", category = VROptionCategory.GUI)
    protected static boolean keyboardAutoLayout = true;
    //---



    //----Movement

    @VROptionField(key = "mode", category = VROptionCategory.MOVEMENT)
    protected static MovementMode movementMode = MovementMode.CONTROLLER;

    @VROptionField(key = "rotation_mode", category = VROptionCategory.MOVEMENT)
    protected static RotationMode rotationMode = RotationMode.HMD;

    @VROptionField(key = "rotation_fly_mode", category = VROptionCategory.MOVEMENT)
    protected static RotationMode rotationFlyMode = RotationMode.OFFHAND;

    @Getter
    @VROptionField(key = "world_rotation.step", category = VROptionCategory.MOVEMENT)
    protected static float worldRotationStep = 45f;
    @Getter
    @VROptionField(key = "world_rotation.smooth_sensitivity", category = VROptionCategory.MOVEMENT)
    protected static float worldRotationSmoothSensitivity = 0.06f;

    @Getter
    @VROptionField(key = "walk_up", category = VROptionCategory.MOVEMENT)
    protected static boolean walkUpEnabled = true;

    @Getter
    @VROptionField(key = "compatible_look_direction", category = VROptionCategory.MOVEMENT)
    protected static boolean compatibleLookDirection = true;



    @Getter
    @VROptionField(key = "movement.sprintThreshold")
    protected static float sprintThreshold = 0.9f;


    //----Rendering
    @Getter
    @VROptionField(key = "world_scale", category = VROptionCategory.RENDERING)
    protected static float worldScale = 1.0f;

    @Getter
    @VROptionField(key = "mirror_mode", category = VROptionCategory.RENDERING)
    protected static MirrorMode mirrorMode = MirrorMode.CROPPED;

    @Getter
    @VROptionField(key = "mirror_eye", category = VROptionCategory.RENDERING)
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
    @VROptionField(key = "dh_mirror_passes", category = VROptionCategory.RENDERING)
    protected static boolean dhMirrorPasses = false;

    //----Shaders
    @Getter
    @VROptionField(key = "per_eye_pipelines", category = VROptionCategory.RENDERING_SHADERS)
    protected static boolean shaderPerEyePipelines = true;

    @Getter
    @VROptionField(key = "shared_shadows", category = VROptionCategory.RENDERING_SHADERS)
    protected static boolean shaderSharedShadows = false;

    @Getter
    @VROptionField(key = "shared_ssbo", category = VROptionCategory.RENDERING_SHADERS)
    protected static boolean shaderSharedSsbo = true;

    //----Eye Effects
    @Getter
    @VROptionField(key = "low_health_indicator", category = VROptionCategory.RENDERING_EYE_EFFECTS)
    protected static boolean lowHealthIndicatorEnabled = true;

    @Getter
    @VROptionField(key = "hit_indicator", category = VROptionCategory.RENDERING_EYE_EFFECTS)
    protected static boolean hitIndicatorEnabled = true;

    @Getter
    @VROptionField(key = "freeze", category = VROptionCategory.RENDERING_EYE_EFFECTS)
    protected static boolean freezeEffectEnabled = true;

    @Getter
    @VROptionField(key = "pumpkin", category = VROptionCategory.RENDERING_EYE_EFFECTS)
    protected static boolean pumpkinEffectEnabled = true;

    // ---- VR Body rendering
    @Getter @Setter
    @VROptionField
    protected static String defaultVrBody = "hands_only";

    @VROptionField(key = "fbt", category = VROptionCategory.VR_BODY)
    protected static boolean fbtEnabled = false;

    @VROptionField(key = "hand_tracking", category = VROptionCategory.VR_BODY)
    protected static boolean handTrackingEnabled = false;

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
    @VROptionField(key = "settings_text_scale", category = VROptionCategory.GUI)
    protected static float settingsTextScale = 0.85f;

    @Getter
    @VROptionField(key = "gui.scale")
    protected static float guiScale = 0;

    @Getter
    @VROptionField(key = "hud_disabled", category = VROptionCategory.GUI_HOTBAR)
    protected static boolean hudDisableHotBar = true;

    @Getter
    @VROptionField(key = "center_radius", category = VROptionCategory.GUI_HOTBAR)
    protected static float hotBarCenterRadius = 50;

    @Getter
    @VROptionField(key = "hysteresis_margin", category = VROptionCategory.GUI_HOTBAR)
    protected static float hotBarHysteresisMargin = 7;

    @Getter
    @VROptionField(key = "slot_numbers", category = VROptionCategory.GUI_HOTBAR)
    protected static boolean hotBarSlotNumbers = false;


    //----


    //----Third Person Mirror
    @Getter
    @VROptionField(key = "fov", category = VROptionCategory.RENDERING_THIRD_PERSON)
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
    @VROptionField(key = "render_hands", category = VROptionCategory.RENDERING_MIXED_REALITY)
    protected static boolean mixedRealityRenderHands = false;

    @Getter
    @VROptionField(key = "as_grid_2_x_2", category = VROptionCategory.RENDERING_MIXED_REALITY)
    protected static boolean mixedRealityAsGrid2x2 = true;

    @Getter
    @VROptionField(key = "with_first_person", category = VROptionCategory.RENDERING_MIXED_REALITY)
    protected static boolean mixedRealityWithFirstPerson = true;

    @Getter
    @VROptionField(key = "alpha_mask", category = VROptionCategory.RENDERING_MIXED_REALITY)
    protected static boolean mixedRealityAlphaMask = false;

    @Getter
    @VROptionField(key = "fov", category = VROptionCategory.RENDERING_MIXED_REALITY)
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
    @VROptionField(key = "room_sneak", category = VROptionCategory.IMMERSION)
    protected static boolean roomSneakEnabled = true;

    @Getter
    @VROptionField(key = "room_crawl", category = VROptionCategory.IMMERSION)
    protected static boolean roomCrawlEnabled = true;

    @Getter
    @VROptionField(key = "room_climb", category = VROptionCategory.IMMERSION)
    protected static boolean roomClimbEnabled = true;

    @Getter
    @VROptionField(key = "room_jump", category = VROptionCategory.IMMERSION)
    protected static boolean roomJumpEnabled = true;

    @Getter
    @VROptionField(key = "room_swim", category = VROptionCategory.IMMERSION)
    protected static boolean roomSwimEnabled = true;

    @Getter
    @VROptionField(key = "room_dismount_vehicle", category = VROptionCategory.IMMERSION)
    protected static boolean roomDismountVehicleEnabled = true;

    @Getter
    @VROptionField(key = "room_consume", category = VROptionCategory.IMMERSION)
    protected static boolean roomConsumeEnabled = true;


    @Getter
    @VROptionField(key = "room_sneak.threshold", category = VROptionCategory.IMMERSION_ADVANCED)
    protected static float roomSneakThreshold = 0.85f;

    @Getter
    @VROptionField(key = "room_crawl.threshold", category = VROptionCategory.IMMERSION_ADVANCED)
    protected static float roomCrawlThreshold = 0.7f;

    @Getter
    @VROptionField(key = "room_jump.threshold", category = VROptionCategory.IMMERSION_ADVANCED)
    protected static float roomJumpThreshold = 1.05f;

    @Getter
    @VROptionField(key = "swing.speed_threshold", category = VROptionCategory.IMMERSION_ADVANCED)
    protected static float swingSpeedThreshold = 3.0f;



    // ---- OTHER



    @Setter
    @VROptionField(key = "player.full_height", excludeForcedChange = true)
    protected static float fullHeight = VRPlayer.DEFAULT_FULL_HEIGHT;


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
        var player = Minecraft.getInstance().player;
        if(player != null
                && !player.isPassenger()
                && player.getAbilities().flying){
            return rotationFlyMode;
        }
        return rotationMode;
    }

    public static boolean isFbtEnabled() {
        return fbtEnabled && VRServerSettings.isBodyTrackersSupported();
    }

    public static boolean isHandTrackingEnabled() {
        return handTrackingEnabled && VRServerSettings.isHandTrackersSupported();
    }

    public static void updateThirdPersonCamera(@NotNull Vector3fc position,
                                               @NotNull Quaternionfc rotation){
        thirdPersonCameraPosX = position.x();
        thirdPersonCameraPosY = position.y();
        thirdPersonCameraPosZ = position.z();
        thirdPersonCameraRotation = new Quaternionf(rotation);
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

    public static boolean isLimitedSurvivalTeleport() {
        return true; //leave it, for later easier navigation in code to change movement
    }

}
