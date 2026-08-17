package org.vmstudio.visor.extensions.client.entity;

import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.player.VRClientPlayer;


public interface EntityRenderStateExtension {

    /**
     * The tracked VR player this state was extracted from, or null when the entity is not
     * a VR player. Equivalent to the old {@code VRClientPlayers.getPlayer(entity)}.
     */
    @Nullable
    VRClientPlayer visor$getVRPlayer();

    void visor$setVRPlayer(@Nullable VRClientPlayer vrPlayer);

    /** Equivalent to the old {@code VRRenderState.isSelfModelRender(entity)}. */
    boolean visor$isSelfModelRender();

    void visor$setSelfModelRender(boolean selfModelRender);

    /** Equivalent to the old {@code VRRenderState.isSelfModelPlayer(entity)}. */
    boolean visor$isSelfModelPlayer();

    void visor$setSelfModelPlayer(boolean selfModelPlayer);

    /** Equivalent to the old {@code VRRenderState.isSelfModelHandsRender(entity)}. */
    boolean visor$isSelfModelHandsRender();

    void visor$setSelfModelHandsRender(boolean selfModelHandsRender);

}
