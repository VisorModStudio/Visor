package org.vmstudio.visor.core.client.provider.openxr;

import me.phoenixra.atumvr.api.AtumVRLogger;
import me.phoenixra.atumvr.api.rendering.AtumVRRenderer;
import me.phoenixra.atumvr.core.XRProvider;
import me.phoenixra.atumvr.core.XRState;
import me.phoenixra.atumvr.core.enums.XRSessionState;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorClientImpl;
import org.vmstudio.visor.core.client.provider.openxr.render.XrRenderer;
import org.jetbrains.annotations.NotNull;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class XrProvider extends XRProvider {

    public XrProvider(@NotNull String appName, @NotNull AtumVRLogger logger) {
        super(appName, logger);

        ClientContext.rawPoseHandler = new XrRawPoseHandler(this);
        ClientContext.inputProvider = inputHandler;
    }

    @Override
    public void initializeVR() throws Throwable {

        super.initializeVR();

        ClientContext.settingsManager.loadOptions();

        VisorClientImpl.LOGGER.info("OpenXR initialized");
    }

    @Override
    public void startFrame() {
        super.startFrame();
        ClientContext.rawPoseHandler.updatePose();
    }

    @Override
    public @NotNull XRState createStateHandler() {
        return new XRState(this);
    }

    @Override
    public @NotNull XrInputHandler createInputHandler() {
        return new XrInputHandler(this);
    }

    @Override
    public @NotNull AtumVRRenderer createRenderer() {
        return new XrRenderer(this);
    }

    @Override
    public void onStateChanged(XRSessionState state) {
        if(state == XRSessionState.EXITING){
            MC.stop();
        }
    }

    @Override
    public @NotNull XrInputHandler getInputHandler() {
        return (XrInputHandler) super.getInputHandler();
    }
}
