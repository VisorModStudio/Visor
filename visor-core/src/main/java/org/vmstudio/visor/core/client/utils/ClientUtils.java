package org.vmstudio.visor.core.client.utils;

import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.compatibility.mcversion.render.McRenderUtils;
import org.vmstudio.visor.api.compatibility.mcversion.gui.McGuiUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.LevelReader;
import org.joml.Vector2f;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientUtils {
    public static boolean isFullscreenInVr() {
        return VisorState.get().isActive()
                && Minecraft.getInstance().getWindow().isFullscreen();
    }

    public static Vector2f getPlayAreaSize() {

        return new Vector2f(2, 2);
    }

    public static int packedLightWithFloor(LevelReader level, BlockPos pos, int minBlockLight) {
        int packed = McRenderUtils.packedLight(level, pos);
        int blockLight = (packed >> 4) & 0xF;
        if (blockLight >= minBlockLight) {
            return packed;
        }
        return (packed & ~0xFF) | (minBlockLight << 4);
    }

    public static void updateKeyMappingState(KeyMapping keyMapping,
                                             boolean pressed) {
        if (keyMapping != null) {
            keyMapping.setDown(pressed);
            if(pressed) {
                keyMapping.clickCount += 1;
            }
        }
    }


    /**
     * Wraps the given text into lines of no more than maxLineLength characters.
     * Preserves existing paragraph breaks.
     *
     * @param text           the input text (may contain \r or \n)
     * @param maxLineLength  maximum number of characters per line (must be > 0)
     * @return a list of wrapped lines
     */
    public static List<String> wrapText(String text, int maxLineLength) {
        // edge-cases
        if (text == null || maxLineLength <= 0) {
            return text == null
                    ? Collections.emptyList()
                    : Collections.singletonList(text);
        }

        List<String> wrappedLines = new ArrayList<>();
        // split into paragraphs on any CRLF or LF
        String[] paragraphs = text.split("\\r?\\n");

        for (String paragraph : paragraphs) {
            // if paragraph is empty, preserve a blank line
            if (paragraph.isEmpty()) {
                wrappedLines.add("");
                continue;
            }

            String[] words = paragraph.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                // if this word alone is longer than maxLineLength, we let it overflow
                if (!line.isEmpty()
                        && line.length() + 1 + word.length() > maxLineLength) {
                    // flush current line
                    wrappedLines.add(line.toString());
                    line.setLength(0);
                }

                if (!line.isEmpty()) {
                    line.append(' ');
                }
                line.append(word);
            }

            // flush last line of this paragraph
            if (!line.isEmpty()) {
                wrappedLines.add(line.toString());
            }
        }

        return wrappedLines;
    }

    public static void takeScreenshot(RenderTarget fb) {
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, fb, (text) ->
        {
            minecraft.execute(() -> {
                McGuiUtils.addChatMessage(McVersionClientUtils.chat(), text);
            });
        });
    }

    public static void disconnect(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        boolean bl = minecraft.isLocalServer();
        boolean bl2 = McVersionClientUtils.isConnectedToRealms(minecraft);
        var connection = minecraft.getConnection();
        if(connection != null){
            connection.getConnection().disconnect(Component.literal(message));
        }
        if (bl) {
            McVersionClientUtils.clearLevel(minecraft,
                    McVersionClientUtils.savingLevelScreen(Component.translatable("visor.messages.saving_world", message)));
        } else {
            McVersionClientUtils.clearLevel(minecraft);
        }

        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            McVersionClientUtils.setScreen(titleScreen);
        } else if (bl2) {
            McVersionClientUtils.setScreen(new RealmsMainScreen(titleScreen));
        } else {
            McVersionClientUtils.setScreen(new JoinMultiplayerScreen(titleScreen));
        }
    }
}
