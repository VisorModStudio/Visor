package org.vmstudio.visor.api.compatibility.mcversion.gui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
//? if >=1.21.6 {
import net.minecraft.client.renderer.RenderPipelines;
//?} else {
/*import net.minecraft.client.renderer.RenderType;
*///?}
//? if >=1.21.9 {
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
//?}

/**
 * Cross-mc-version Utils for GUI methods
 */
@Environment(EnvType.CLIENT)
public class McGuiUtils {
    private McGuiUtils() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    // ------- SCREEN -------

    public static void initScreen(Screen screen, int width, int height) {
        screen.init(Minecraft.getInstance(), width, height);
    }

    public static void renderWithTooltip(Screen screen, GuiGraphics guiGraphics,
                                         int mouseX, int mouseY, float partialTick) {
        //? if >=1.21.9 {
        screen.renderWithTooltipAndSubtitles(guiGraphics, mouseX, mouseY, partialTick);
        //?} else {
        /*screen.renderWithTooltip(guiGraphics, mouseX, mouseY, partialTick);
        *///?}
    }

    public static void setTooltipForNextRenderPass(Screen screen, GuiGraphics guiGraphics,
                                                   Tooltip tooltip, ClientTooltipPositioner positioner,
                                                   int mouseX, int mouseY, boolean focused) {
        //? if >=1.21.6 {
        guiGraphics.setTooltipForNextFrame(
                screen.getFont(), tooltip.toCharSequence(Minecraft.getInstance()),
                positioner, mouseX, mouseY, focused
        );
        //?} else {
        /*screen.setTooltipForNextRenderPass(tooltip, positioner, focused);
        *///?}
    }


    // ------- POSE -------

    public static void pushPose(GuiGraphics guiGraphics) {
        //? if >=1.21.6 {
        guiGraphics.pose().pushMatrix();
        //?} else {
        /*guiGraphics.pose().pushPose();
        *///?}
    }

    public static void popPose(GuiGraphics guiGraphics) {
        //? if >=1.21.6 {
        guiGraphics.pose().popMatrix();
        //?} else {
        /*guiGraphics.pose().popPose();
        *///?}
    }

    public static void translate(GuiGraphics guiGraphics, float x, float y) {
        //? if >=1.21.6 {
        guiGraphics.pose().translate(x, y);
        //?} else {
        /*guiGraphics.pose().translate(x, y, 0.0F);
        *///?}
    }

    public static void scale(GuiGraphics guiGraphics, float x, float y) {
        //? if >=1.21.6 {
        guiGraphics.pose().scale(x, y);
        //?} else {
        /*guiGraphics.pose().scale(x, y, 1.0F);
        *///?}
    }

    public static Matrix4f poseMatrix(GuiGraphics guiGraphics) {
        //? if >=1.21.6 {
        var pose = guiGraphics.pose();
        return new Matrix4f(
                pose.m00, pose.m01, 0.0F, 0.0F,
                pose.m10, pose.m11, 0.0F, 0.0F,
                0.0F, 0.0F, 1.0F, 0.0F,
                pose.m20, pose.m21, 0.0F, 1.0F);
        //?} else {
        /*return new Matrix4f(guiGraphics.pose().last().pose());
        *///?}
    }


    // ------- TEXTURES -------

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture,
                            int x, int y, int width, int height,
                            float u, float v, int uWidth, int vHeight,
                            int textureWidth, int textureHeight) {
        //? if >=1.21.6 {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height,
                uWidth, vHeight, textureWidth, textureHeight);
        //?} elif >=1.21.2 {
        /*guiGraphics.blit(RenderType::guiTextured, texture, x, y, u, v, width, height,
                uWidth, vHeight, textureWidth, textureHeight);
        *///?} else {
        /*guiGraphics.blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
        *///?}
    }

    public static void blit(GuiGraphics guiGraphics, ResourceLocation texture,
                            int x, int y, float u, float v,
                            int width, int height, int textureWidth, int textureHeight) {
        //? if >=1.21.6 {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        //?} elif >=1.21.2 {
        /*guiGraphics.blit(RenderType::guiTextured, texture, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?} else {
        /*guiGraphics.blit(texture, x, y, u, v, width, height, textureWidth, textureHeight);
        *///?}
    }

    public static void renderOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        //? if >=1.21.9 {
        guiGraphics.submitOutline(x, y, width, height, color);
        //?} else {
        /*guiGraphics.renderOutline(x, y, width, height, color);
        *///?}
    }

    public static void fillGuiOverlay(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        //? if >=1.21.6 {
        guiGraphics.fill(RenderPipelines.GUI, x0, y0, x1, y1, color);
        //?} else {
        /*guiGraphics.fill(RenderType.guiOverlay(), x0, y0, x1, y1, color);
        *///?}
    }

    public static void fillTextHighlight(GuiGraphics guiGraphics, int x0, int y0, int x1, int y1, int color) {
        //? if >=1.21.6 {
        guiGraphics.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, x0, y0, x1, y1, color);
        //?} else {
        /*guiGraphics.fill(RenderType.guiTextHighlight(), x0, y0, x1, y1, color);
        *///?}
    }


    // ------- CHAT -------

    public static void renderChat(ChatComponent chat,
                                  GuiGraphics guiGraphics,
                                  int tickCount, int mouseX, int mouseY) {
        //? if >=1.20.5 {
        chat.render(guiGraphics, tickCount, mouseX, mouseY, false);
        //?} else {
        /*chat.render(guiGraphics, tickCount, mouseX, mouseY);
        *///?}
    }

    // ------- INPUT -------
    // classic signatures, forwarded to whatever the version expects

    public static boolean mouseClicked(GuiEventListener listener,
                                       double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return listener.mouseClicked(mouseButtonEvent(mouseX, mouseY, button), false);
        //?} else {
        /*return listener.mouseClicked(mouseX, mouseY, button);
        *///?}
    }

    public static boolean mouseReleased(GuiEventListener listener,
                                        double mouseX, double mouseY, int button) {
        //? if >=1.21.9 {
        return listener.mouseReleased(mouseButtonEvent(mouseX, mouseY, button));
        //?} else {
        /*return listener.mouseReleased(mouseX, mouseY, button);
        *///?}
    }

    public static boolean mouseDragged(GuiEventListener listener,
                                       double mouseX, double mouseY, int button,
                                       double dragX, double dragY) {
        //? if >=1.21.9 {
        return listener.mouseDragged(mouseButtonEvent(mouseX, mouseY, button), dragX, dragY);
        //?} else {
        /*return listener.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        *///?}
    }

    public static boolean mouseScrolled(GuiEventListener listener,
                                        double mouseX, double mouseY, double verticalAmount) {
        //? if >=1.20.2 {
        return listener.mouseScrolled(mouseX, mouseY, 0, verticalAmount);
        //?} else {
        /*return listener.mouseScrolled(mouseX, mouseY, verticalAmount);
        *///?}
    }

    public static boolean keyPressed(GuiEventListener listener,
                                     int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        return listener.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        //?} else {
        /*return listener.keyPressed(keyCode, scanCode, modifiers);
        *///?}
    }

    public static boolean keyReleased(GuiEventListener listener,
                                      int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        return listener.keyReleased(new KeyEvent(keyCode, scanCode, modifiers));
        //?} else {
        /*return listener.keyReleased(keyCode, scanCode, modifiers);
        *///?}
    }

    public static boolean charTyped(GuiEventListener listener, char chr, int modifiers) {
        //? if >=1.21.9 {
        return listener.charTyped(new CharacterEvent(chr, modifiers));
        //?} else {
        /*return listener.charTyped(chr, modifiers);
        *///?}
    }


    // ------- KEY MODIFIERS -------

    public static void moveCursorToStart(EditBox editBox) {
        //? if >=1.20.2 {
        editBox.moveCursorToStart(false);
        //?} else {
        /*editBox.moveCursorToStart();
        *///?}
    }

    public static void tickEditBox(EditBox editBox) {
        //? if <1.20.2 {
        /*editBox.tick();
        *///?}
    }

    public static void setRenderTopAndBottom(AbstractSelectionList<?> list, boolean render) {
        //? if <1.20.2 {
        /*list.setRenderTopAndBottom(render);
        *///?}
    }

    public static boolean hasControlDown() {
        //? if >=1.21.9 {
        return Minecraft.getInstance().hasControlDown();
        //?} else {
        /*return Screen.hasControlDown();
        *///?}
    }

    public static boolean hasShiftDown() {
        //? if >=1.21.9 {
        return Minecraft.getInstance().hasShiftDown();
        //?} else {
        /*return Screen.hasShiftDown();
        *///?}
    }

    public static boolean hasAltDown() {
        //? if >=1.21.9 {
        return Minecraft.getInstance().hasAltDown();
        //?} else {
        /*return Screen.hasAltDown();
        *///?}
    }

    public static boolean isCopy(int keyCode) {
        //? if >=1.21.9 {
        return keyEvent(keyCode).isCopy();
        //?} else {
        /*return Screen.isCopy(keyCode);
        *///?}
    }

    public static boolean isCut(int keyCode) {
        //? if >=1.21.9 {
        return keyEvent(keyCode).isCut();
        //?} else {
        /*return Screen.isCut(keyCode);
        *///?}
    }

    public static boolean isPaste(int keyCode) {
        //? if >=1.21.9 {
        return keyEvent(keyCode).isPaste();
        //?} else {
        /*return Screen.isPaste(keyCode);
        *///?}
    }

    public static boolean isSelectAll(int keyCode) {
        //? if >=1.21.9 {
        return keyEvent(keyCode).isSelectAll();
        //?} else {
        /*return Screen.isSelectAll(keyCode);
        *///?}
    }

    //? if >=1.21.9 {
    public static MouseButtonEvent mouseButtonEvent(double mouseX, double mouseY, int button) {
        return new MouseButtonEvent(mouseX, mouseY, new MouseButtonInfo(button, currentModifiers()));
    }

    private static KeyEvent keyEvent(int keyCode) {
        return new KeyEvent(keyCode, 0, currentModifiers());
    }

    private static int currentModifiers() {
        Minecraft mc = Minecraft.getInstance();
        return (mc.hasShiftDown() ? InputConstants.MOD_SHIFT : 0)
                | (mc.hasControlDown() ? InputConstants.MOD_CONTROL : 0)
                | (mc.hasAltDown() ? InputConstants.MOD_ALT : 0);
    }
    //?}
}
