package me.phoenixra.visor.core.client.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.core.client.VisorClientImpl;
import me.phoenixra.visor.core.client.settings.lang.LangHandler;
import me.phoenixra.visor.core.client.settings.option.VROptionField;
import me.phoenixra.visor.core.client.settings.option.VRGuiOption;
import me.phoenixra.visor.core.client.settings.option.enums.MirrorMode;
import me.phoenixra.visor.core.client.settings.option.enums.RotationMode;
import me.phoenixra.visor.core.client.settings.option.enums.ShaderGUIRenderMode;
import me.phoenixra.visor.api.client.VRPlayMode;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.joml.Quaternionf;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRClientSettings {


    @Getter
    @VROptionField
    protected static VRPlayMode vrPlayMode = VRPlayMode.ENABLED;

    public static void setVrPlayMode(VRPlayMode vrPlayMode) {
        VRClientSettings.vrPlayMode = vrPlayMode;
        VisorClientImpl.LOGGER.info(
                "Changed VR Play Mode to: {}",
                VRClientSettings.getVrPlayMode()
        );
    }

    @Getter
    @VROptionField(guiOptionType = VRGuiOption.LEFT_HANDED)
    protected static boolean leftHanded = false;


    @Getter
    @VROptionField
    protected static boolean customInputSettings = false;

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
    @VROptionField(guiOptionType = VRGuiOption.WORLD_SCALE,
            key = "world.scale")
    protected static float worldScale = 1.0f;

    @Getter
    @VROptionField(guiOptionType = VRGuiOption.WORLD_ROTATION_INCREMENT,
            key = "world.rotationIncrement")
    protected static float worldRotationIncrement = 45f; //Rotation with thumbstick

    //---

    //----Locomotion


    @Getter
    @VROptionField(guiOptionType = VRGuiOption.ROOM_MOVEMENT_MULTIPLIER,
            key = "player.walkMultiplier")
    protected static float walkMultiplier = 1;


    //----Rendering

    @Getter
    @VROptionField(guiOptionType = VRGuiOption.MIRROR_DISPLAY,
            key = "rendering.mirror.mode")
    protected static MirrorMode displayMirrorMode = MirrorMode.CROPPED;
    @Getter
    @VROptionField(guiOptionType = VRGuiOption.MIRROR_USE_LEFT_EYE,
            key = "rendering.mirror.useLeftEye")
    protected static boolean displayMirrorLeftEye = false;

    @Getter
    @VROptionField(guiOptionType = VRGuiOption.LOW_HEALTH_INDICATOR,
            key = "rendering.lowHealthIndicator")
    protected static boolean lowHealthIndicatorEnabled = true;

    @Getter
    @VROptionField(guiOptionType = VRGuiOption.EYE_FOV_SCALE,
            key = "rendering.eyes.fovScale")
    protected static float eyesFovScale = 1f;

    //FOV changes detection, to apply properly
    @Getter
    private static float eyeFovScaleCurrent = 1.0f;
    @Getter @Setter
    private static boolean eyeFovChanged = false;

    public static void setEyeFovScaleCurrent(float value) {
        eyeFovScaleCurrent = value;
        eyeFovChanged = true;
    }

    @Getter
    protected static final float renderScaleFactor = 1.0f;
    @Getter
    protected static final float mirrorSmooth = 0.0F;
    @Getter
    protected static final float mirrorCrop = 0.15F;
    //

    //----GUI && HUD
    @Getter
    @VROptionField(guiOptionType = VRGuiOption.SHADER_GUI_RENDER,
            key = "gui.shaderMode")
    protected static ShaderGUIRenderMode shaderGUIRender = ShaderGUIRenderMode.AFTER_SHADER;
    @Getter
    @VROptionField(guiOptionType = VRGuiOption.GUI_SCALE,
            key = "gui.scale")
    protected static float guiScale = 0;

    @Getter
    @VROptionField(guiOptionType = VRGuiOption.HUD_DISABLED_HOTBAR,
            key = "gui.hud.disabled_hotbar")
    protected static boolean hudDisableHotBar = true;


    //----


    //----Third Person Mirror
    @Getter
    @VROptionField(guiOptionType = VRGuiOption.THIRD_PERSON_FOV,
            key = "rendering.mirror.thirdPerson.fov")
    protected static float thirdPersonFov = 40;
    @Getter
    @VROptionField(key = "rendering.mirror.thirdPerson.fixedCamera.x")
    protected static float fixedCameraPosX = -1.0f;
    @Getter
    @VROptionField(key = "rendering.mirror.thirdPerson.fixedCamera.y")
    protected static float fixedCameraPosY = 2.4f;
    @Getter
    @VROptionField(key = "rendering.mirror.thirdPerson.fixedCamera.z")
    protected static float fixedCameraPosZ = 2.7f;
    @Getter
    @VROptionField(key = "rendering.mirror.thirdPerson.fixedCamera.rotation")
    protected static Quaternionf fixedCameraRotation = new Quaternionf(.041f, .125f, .239f, .962f);



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
    @VROptionField(guiOptionType = VRGuiOption.ROTATION_MODE)
    protected static RotationMode rotationMode = RotationMode.CONTROLLER_OFFHAND;

    public static boolean isWalkUpEnabled() {
        return walkUpEnabled
                && MC.player!= null
                && !MC.player.isSpectator();
    }


    private static final float defaultHeight = 1.52F;

    @Setter
    @VROptionField(key = "player.height")
    protected static float playerHeight = defaultHeight;


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
                                LangHandler.getText(
                                        "visor.messages.height_set",
                                        i
                                )
                        )
                );
        ClientContext.settingsHandler.saveOptions();
    }


}
