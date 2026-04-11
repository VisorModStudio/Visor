package org.vmstudio.visor.core.client.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockAndTintGetter;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientUtils {
    public static Vector2f getPlayAreaSize() {

        return new Vector2f(2, 2);
    }

    public static int getCombinedLightWithMin(BlockAndTintGetter lightReader, BlockPos pos, int minLight) {
        int i = LevelRenderer.getLightColor(lightReader, pos);
        int j = i >> 4 & 15;

        if (j < minLight) {
            i = i & -256;
            i = i | minLight << 4;
        }

        return i;
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

    public static int getCombinedLight(BlockAndTintGetter lightReader,
                                       BlockPos pos,
                                       int minLight) {
        int i = LevelRenderer.getLightColor(lightReader, pos);
        int j = i >> 4 & 15;

        if (j < minLight) {
            i = i & -256;
            i = i | minLight << 4;
        }

        return i;
    }

    public static void takeScreenshot(RenderTarget fb) {
        Minecraft minecraft = Minecraft.getInstance();
        Screenshot.grab(minecraft.gameDirectory, fb, (text) ->
        {
            minecraft.execute(() -> {
                minecraft.gui.getChat().addMessage(text);
            });
        });
    }

    public static void disconnect(String message) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = minecraft.isLocalServer();
        boolean bl2 = minecraft.isConnectedToRealms();
        var connection = minecraft.getConnection();
        if(connection != null){
            connection.getConnection().disconnect(Component.literal(message));
        }
        if (bl) {
            minecraft.clearLevel(new GenericDirtMessageScreen(Component.literal("Saving world. " + message)));
        } else {
            minecraft.clearLevel();
        }

        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            minecraft.setScreen(titleScreen);
        } else if (bl2) {
            minecraft.setScreen(new RealmsMainScreen(titleScreen));
        } else {
            minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
        }
    }
}
