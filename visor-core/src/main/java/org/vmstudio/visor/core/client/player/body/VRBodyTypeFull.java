package org.vmstudio.visor.core.client.player.body;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.player.body.RegisterVRBodyType;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.player.body.VRBodyPart;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.addon.VisorAddon;

@RegisterVRBodyType
public class VRBodyTypeFull extends VRBodyType {

    public static final String ID = "full_body";
    public static final Component NAME = Component.literal("Full Body");

    @Getter
    private static VRBodyTypeFull instance;

    @Getter
    private final VRBodyRendererFull renderer;


    public VRBodyTypeFull(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
        renderer = new VRBodyRendererFull();
    }


    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isSelfModelVisible() {
        return true;
    }

    @Override
    public @NotNull VRBody createBody(@NotNull VRClientPlayer vrPlayer, @NotNull VRPlayerPoseClient vrPlayerPose) {
        return new VRBodyFull(this, vrPlayer, vrPlayerPose);
    }

    @Override
    public Component getName() {
        return NAME;
    }
    @Override
    public @NotNull String getId() {
        return ID;
    }

}
