package org.vmstudio.visor.api.client.player.body;

import lombok.Getter;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.client.player.VRClientPlayer;
import org.vmstudio.visor.api.client.render.decoration.VRBodyRenderer;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;

import java.util.HashMap;
import java.util.Objects;

public abstract class VRBodyType implements VisorComponent {
    @Getter
    @NotNull
    private final VisorAddon owner;



    public VRBodyType(@NotNull VisorAddon owner){
        Objects.requireNonNull(owner);
        this.owner = owner;

    }

    @NotNull
    public abstract VRBodyRenderer getRenderer();

    /**
     * If player can select this body.
     * When false, only selectable
     *
     * @return true/false
     */
    public abstract boolean isSelectable();

    /**
     * If self player model is visible (local player's model)
     *
     * @return true/false
     */
    public abstract boolean isSelfModelVisible();


    public @NotNull VRBody createBody(@NotNull VRClientPlayer vrPlayer){
        return new VRBody(this, vrPlayer);
    }


    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void setEnabled(boolean flag) { }

    /**
     * Display name for the body
     */
    public Component getName() {
        return Component.translatable("visor.vr_body_type."+getId());
    }

}
