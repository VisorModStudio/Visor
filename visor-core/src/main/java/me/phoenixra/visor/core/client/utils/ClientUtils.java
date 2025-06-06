package me.phoenixra.visor.core.client.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientUtils {
    public static Vector2f getPlayAreaSize() {

        return null;    //@TODO
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

    public static AABB getEntityHeadHitbox(Entity entity, double inflate) {
        if ((entity instanceof Player player && !player.isSwimming()) || // swimming players hitbox is just a box around their butt
                entity instanceof Zombie ||
                entity instanceof AbstractPiglin ||
                entity instanceof AbstractSkeleton ||
                entity instanceof Witch ||
                entity instanceof AbstractIllager ||
                entity instanceof Blaze ||
                entity instanceof Creeper ||
                entity instanceof EnderMan ||
                entity instanceof AbstractVillager ||
                entity instanceof SnowGolem ||
                entity instanceof Vex ||
                entity instanceof Strider) {

            Vec3 headpos = entity.getEyePosition();
            double headsize = entity.getBbWidth() * 0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            return new AABB(headpos.subtract(headsize, headsize - inflate, headsize), headpos.add(headsize, headsize + inflate, headsize)).inflate(inflate);
        } else if (!(entity instanceof EnderDragon) // no ender dragon, the code doesn't work for it
                && entity instanceof LivingEntity livingEntity) {

            float yrot = -(livingEntity.yBodyRot) * Mth.DEG_TO_RAD;
            // offset head in entity rotation
            Vec3 headpos = entity.getEyePosition()
                    .add(new Vec3(Mth.sin(yrot), 0, Mth.cos(yrot))
                            .scale(livingEntity.getBbWidth() * 0.5F));

            double headsize = livingEntity.getBbWidth() * 0.25;
            if (livingEntity.isBaby()) {
                // babies have big heads
                headsize *= 1.5;
            }
            return new AABB(headpos.subtract(headsize, headsize, headsize), headpos.add(headsize, headsize, headsize)).inflate(inflate * 0.25).expandTowards(headpos.subtract(entity.position()).scale(inflate));
        }
        return null;
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

}
