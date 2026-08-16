package org.vmstudio.visor.extensions.client.entity;

import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.client.player.VRClientPlayer;

/**
 * Carries the VR data a model needs onto the vanilla render state.
 * <p>
 * 1.21.2 moved entity rendering onto the render-state system: models are handed a
 * {@code PlayerRenderState} snapshot and never see the entity, so they can no longer look
 * a player up by UUID or ask {@code VRRenderState} about it. Everything entity-derived has
 * to be resolved during {@code extractRenderState} and parked here instead.
 * <p>
 * Deliberately stores resolved values rather than the entity itself - render states are
 * pooled and reused per entity, so holding a live entity reference on one would outlive
 * the frame it was extracted for.
 *
 * @see org.vmstudio.visor.core.client.render.player.model.full.VRPlayerModelFull
 */
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
