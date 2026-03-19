package org.vmstudio.visor.core.client.player.body;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.body.RegisterVRBody;
import org.vmstudio.visor.api.client.player.body.VRBody;
import org.vmstudio.visor.api.client.player.body.VRBodyPart;
import org.vmstudio.visor.api.common.addon.VisorAddon;

@RegisterVRBody
public class VRBodyHandsOnly extends VRBody {

    public static final String ID = "hands_only";
    public static final Component NAME = Component.literal("Hands only");

    @Getter
    private final VRBodyRendererHandsOnly renderer;


    @Getter
    private VRBodyPart mainHand;
    @Getter
    private VRBodyPart offhand;

    public VRBodyHandsOnly(@NotNull VisorAddon owner) {
        super(owner);
        renderer = new VRBodyRendererHandsOnly();
    }

    @Override
    public void onInit() {
        mainHand = VRBodyPart.SIMPLE_MAIN_HAND;
        offhand = VRBodyPart.SIMPLE_OFFHAND;

        addBodyPart(mainHand);
        addBodyPart(offhand);
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean isFullBody() {
        return false;
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
