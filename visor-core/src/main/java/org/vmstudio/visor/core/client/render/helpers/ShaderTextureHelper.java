package org.vmstudio.visor.core.client.render.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.lwjgl.opengl.GL11C;
import org.vmstudio.visor.api.common.utils.LoggerUtils;
import org.vmstudio.visor.mixin.client.accessors.RenderSystemAccessor;

public class ShaderTextureHelper {
    private static final IntSet DELETED = new IntOpenHashSet();
    private static boolean recoveryLogged;
    private ShaderTextureHelper(){
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static void onTextureDeleted(int textureId) {
        if (textureId <= 0) {
            return;
        }
        DELETED.add(textureId);

        int[] slots = RenderSystemAccessor.getShaderTextures();
        if (slots == null){
            return;
        }
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == textureId){
                slots[i] = 0;
            }
        }
    }

    public static void onTextureCreated(int textureId) {
        if (textureId > 0) {
            DELETED.remove(textureId);
        }
    }

    public static int sanitize(int textureId) {
        if (textureId < 0) {
            return 0;
        }
        if (textureId == 0 || DELETED.isEmpty() || !DELETED.contains(textureId)) {
            return textureId;
        }
        if (!RenderSystem.isOnRenderThread()) {
            return textureId;
        }
        if (GL11C.glIsTexture(textureId)) {
            DELETED.remove(textureId);
            return textureId;
        }

        if (!recoveryLogged) {
            recoveryLogged = true;
            LoggerUtils.getLogger().info("Visor: dropped a deleted texture ({})", textureId);
        }
        return 0;
    }
}