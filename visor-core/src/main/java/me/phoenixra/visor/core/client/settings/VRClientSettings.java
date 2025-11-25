package me.phoenixra.visor.core.client.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.api.enums.EyeType;
import me.phoenixra.atumvr.api.misc.color.AtumColor;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.settings.options.VROptionField;
import me.phoenixra.visor.core.client.settings.options.enums.MirrorMode;
import me.phoenixra.visor.core.client.settings.options.enums.RotationMode;
import me.phoenixra.visor.core.client.settings.options.enums.ShaderGUIRenderMode;
import me.phoenixra.visor.api.client.VRPlayMode;
import me.phoenixra.visor.core.client.utils.LangHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRClientSettings {


    @Getter
    @VROptionField
    protected static VRPlayMode vrPlayMode = VRPlayMode.ENABLED;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.LEFT_HANDED, key = "left_handed")
    protected static boolean leftHanded = false;


    //----Keyboard
    @Getter
    @VROptionField(key = "keyboard.keys")
    protected static String keyboardKeys = "`1234567890-=qwertyuiop[]\\asdfghjkl;':\"zxcvbnm,./?<>";

    @Getter
    @VROptionField(key = "keyboard.keysShift")
    protected static String keyboardKeysShift = "~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL;':\"ZXCVBNM,./?<>";
    //---

    //----World
    @Getter
    @VROptionField(key = "world_scale", category = VROptionCategory.RENDERING)
    protected static float worldScale = 1.0f;

    @Getter
    @VROptionField(widgetType = VROptionWidgetType.WORLD_ROTATION_INCREMENT,
            key = "world_rotation_increment")
    protected static float worldRotationIncrement = 45f; //Rotation with thumbstick

    //---



    //----Locomotion


    @Getter
    @VROptionField(key = "player.walkMultiplier")
    protected static float walkMultiplier = 1;


    //----Rendering

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


    //----Main menu panorama
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
    @VROptionField(key = "camera.pos.x", category = VROptionCategory.RENDERING_THIRD_PERSON)
    protected static float thirdPersonCameraPosX = -1.0f;
    @Getter
    @VROptionField(key = "camera.pos.y", category = VROptionCategory.RENDERING_THIRD_PERSON)
    protected static float thirdPersonCameraPosY = 2.4f;
    @Getter
    @VROptionField(key = "camera.pos.z", category = VROptionCategory.RENDERING_THIRD_PERSON)
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


    @Getter
    protected static final float sprintThreshold = 0.9f;
    @Getter
    protected static final float jumpThreshold = 0.05f;
    @Getter
    protected static final float sneakThreshold = 0.4f;
    @Getter
    protected static final float crawlThreshold = 0.82f;

    protected static final boolean walkUpEnabled = true;

    //
    @Getter
    @VROptionField(widgetType = VROptionWidgetType.ROTATION_MODE, key = "rotation_mode")
    protected static RotationMode rotationMode = RotationMode.HMD;


    private static final float defaultHeight = 1.52F;

    @Setter
    @VROptionField(key = "player.height")
    protected static float playerHeight = defaultHeight;


    public static void setVrPlayMode(VRPlayMode vrPlayMode) {
        VRClientSettings.vrPlayMode = vrPlayMode;
        VisorClientImpl.LOGGER.info(
                "Changed VR Play Mode to: {}",
                VRClientSettings.getVrPlayMode()
        );
    }

    public static void updateThirdPersonCamera(@NotNull Vector3fc position,
                                               @NotNull Quaternionfc rotation,
                                               boolean save){
        thirdPersonCameraPosX = position.x();
        thirdPersonCameraPosY = position.y();
        thirdPersonCameraPosZ = position.z();
        thirdPersonCameraRotation = new Quaternionf(rotation);
        if(save) {
            ClientContext.settingsHandler.saveOptions();
        }
    }


    public static void setEyeFovScaleCurrent(float value) {
        eyeFovScaleCurrent = value;
        eyeFovChanged = true;
    }

    public static boolean isWalkUpEnabled() {
        return walkUpEnabled
                && MC.player!= null
                && !MC.player.isSpectator();
    }

    public static float getPlayerHeight() {
        if (playerHeight < 0) {
            return defaultHeight;
        }

        return playerHeight;
    }

    public static void calibrateHeight() {

        VRClientSettings.setPlayerHeight(
                ClientContext.rawPoseHandler.getHmdData()
                        .getPivotHistory().averagePosition(0.5f).y
        );
        int i = (int) (Math.round(100.0D
                * VRClientSettings.getPlayerHeight()
                / defaultHeight
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
        ClientContext.settingsHandler.saveOptions();
    }


}
