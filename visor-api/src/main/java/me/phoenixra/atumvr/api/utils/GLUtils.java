package me.phoenixra.atumvr.api.utils;

import org.lwjgl.opengl.GL11;

public class GLUtils {
    public static void checkGLError(String message) {
        int error = GL11.glGetError();
        if (error != 0) {
            throw new RuntimeException(message+" OpenGL Error Code: " + error);
        }
    }

}
