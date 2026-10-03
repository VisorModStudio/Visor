package org.vmstudio.visor.api.compatibility.mcversion.render;

//? if >=1.21.9 {
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
//?}
//? if >=26.2 {
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
//?}
//? if >=26.3 {
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import java.util.Optional;
import java.util.OptionalDouble;
//?}

/**
 * Cross-mc-version drawing of the render nodes Visor submits
 */
public final class McFeatureRenderer {
    private McFeatureRenderer() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    //? if >=26.2 {
    private static SubmitNodeStorage storage;
    private static RenderBuffers buffers;
    private static FeatureRenderDispatcher dispatcher;

    private static void create() {
        Minecraft minecraft = Minecraft.getInstance();
        storage = new SubmitNodeStorage();
        buffers = new RenderBuffers(1);
        dispatcher = new FeatureRenderDispatcher(buffers, minecraft.getModelManager(), minecraft.getAtlasManager(),
                minecraft.font, minecraft.gameRenderer.gameRenderState());
    }
    //?}

    //? if >=1.21.9 {
    public static SubmitNodeCollector collector() {
        //? if >=26.2 {
        if (dispatcher == null) {
            create();
        }
        return storage;
        //?} else {
        /*return Minecraft.getInstance().gameRenderer.getSubmitNodeStorage();
        *///?}
    }

    public static void render() {
        //? if >=26.3 {
        if (dispatcher != null) {
            RenderTarget target = McRenderTarget.mainTarget();
            try (FeatureRenderDispatcher.PreparedFrame frame = dispatcher.prepareFrame(storage);
                 RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                         () -> "visor features", target.getColorTextureView(), Optional.empty(),
                         target.getDepthTextureView(), OptionalDouble.empty())) {
                RenderSystem.bindDefaultUniforms(pass);
                FeatureRenderDispatcher.renderAllFeatures(pass, frame);
            }
        }
        //?} elif >=26.2 {
        /*if (dispatcher != null) {
            dispatcher.renderAllFeatures(storage);
        }
        *///?} else {
        /*Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        minecraft.renderBuffers().bufferSource().endBatch();
        *///?}
    }
    //?}

    public static void endFrame() {
        //? if >=26.2 {
        if (buffers != null) {
            buffers.endFrame();
        }
        //?}
    }

    public static void close() {
        //? if >=26.2 {
        if (dispatcher != null) {
            dispatcher.close();
            buffers.close();
            dispatcher = null;
            buffers = null;
            storage = null;
        }
        //?}
    }
}
