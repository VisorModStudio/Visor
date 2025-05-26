package me.phoenixra.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.VRLogger;
import me.phoenixra.atumvr.api.rendering.IRenderContext;
import me.phoenixra.atumvr.api.rendering.VRRenderer;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.OpenXRState;
import me.phoenixra.atumvr.core.enums.XRSessionStateChange;
import me.phoenixra.atumvr.core.input.OpenXRInputHandler;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.VisorClient;
import me.phoenixra.visor.core.client.provider.openxr.render.XrRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class XrVRProvider extends OpenXRProvider {

    public XrVRProvider(@NotNull String appName, @NotNull VRLogger logger) {
        super(appName, logger);

        ClientContext.rawPlayerPose = new XrRawPlayerPose(this);
    }

    @Override
    public void initializeVR() throws Throwable {

        super.initializeVR();

        ClientContext.settingsHandler.loadOptions();

        VisorClient.LOGGER.info("OpenXR initialized");
    }

    @Override
    public void preRender(@NotNull IRenderContext context) {
        super.preRender(context);
        ClientContext.rawPlayerPose.updatePose();
    }

    @Override
    public @Nullable OpenXRState createStateHandler() {
        return new OpenXRState(this);
    }

    @Override
    public @Nullable OpenXRInputHandler createInputHandler() {
        return new XrInputHandler(this);
    }

    @Override
    public @NotNull VRRenderer createRenderer() {
        return new XrRenderer(this);
    }

    @Override
    public void onStateChanged(XRSessionStateChange state) {

    }
}
