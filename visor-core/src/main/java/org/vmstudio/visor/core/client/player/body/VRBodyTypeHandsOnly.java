package org.vmstudio.visor.core.client.player.body;

import lombok.Getter;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.body.RegisterVRBodyType;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.common.addon.VisorAddon;

@RegisterVRBodyType
public class VRBodyTypeHandsOnly extends VRBodyType {

    public static final String ID = "hands_only";
    public static final Component NAME = Component.translatable("visor.vr_body_type.hands_only.name");

    @Getter
    private static VRBodyTypeHandsOnly instance;


    @Getter
    private final VRBodyRendererHandsOnly renderer;


    public VRBodyTypeHandsOnly(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
        renderer = new VRBodyRendererHandsOnly();

        FALLBACK_BODY_TYPE = this;
    }



    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public ModelSelfVisibility getSelfModelVisibility() {
        return ModelSelfVisibility.NO_MODEL;
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
