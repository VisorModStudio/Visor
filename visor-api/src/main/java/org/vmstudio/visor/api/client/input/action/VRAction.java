package org.vmstudio.visor.api.client.input.action;

import me.phoenixra.atumvr.api.input.action.AtumVRAction;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Base interface for VR actions.
 * <p>
 *     It is used to handle specific user interactions with VR
 *     (controller buttons, joystick...)
 * </p>
 * <p>
 *     It uses {@link AtumVRAction} as an access point for VR data
 * </p>
 */
public interface VRAction {


    /**
     * On game pre-tick
     */
    void preTick();


    /**
     * Update action state
     * <p>
     *     Called at the beginning of the game loop every frame
     * </p>
     *
     * @param currentProfile the profile
     * @param leftHanded if left-handed
     */
    void updateState(@NotNull XRInteractionProfile currentProfile,
                     boolean leftHanded);

    /**
     * Clear action state
     */
    void clear();

    /**
     * If action is currently active and tracked by VR
     *
     * @return true/false
     */
    boolean isActive();

    /**
     * If action's data has been changed from previous update
     *
     * @return true/false
     */
    boolean isChanged();

    /**
     * If this action is common between action sets.
     * <p>
     *     When the player rebinds a common action,
     *     the player can sync the rebind
     *     in other action sets where this action is used
     * </p>
     *
     * @return true/false
     */
    default boolean isCommon(){
        return false;
    }

    /**
     * If this action is required to be set.
     * <p>
     *     When true, player cannot apply changes
     *     with this action not being bound.
     * </p>
     * @return true/false
     */
    default boolean isRequired(){
        return false;
    }

    /**
     * Set action binding for specified interaction profile
     *
     * @param profile the interaction profile
     * @param binding the action binding
     */
    void setBinding(@NotNull VRInteractionProfileType profile,
                    @NotNull ActionBinding binding);

    /**
     * Get action binding of specified interaction profile
     *
     * @param profile the interaction profile
     * @return the action binding
     */
    @Nullable
    ActionBinding getBinding(@NotNull VRInteractionProfileType profile);

    /**
     * Get binding for specified interaction profile or empty
     *
     * @param profile the interaction profile
     * @return the action binding
     */
    @NotNull
    default ActionBinding getBindingOrEmpty(@NotNull VRInteractionProfileType profile){
        return Optional.ofNullable(getBinding(profile)).orElse(ActionBinding.EMPTY);
    }

    /**
     * Get default bindings
     *
     * @return the map of bindings
     */
    @NotNull
    Map<VRInteractionProfileType, ActionBinding> getDefaultBindings();

    /**
     * Get default binding for specified interaction profile.
     *
     * @param profile the interaction profile
     * @return the action binding
     */
    @Nullable
    ActionBinding getDefaultBinding(@NotNull VRInteractionProfileType profile);


    /**
     * Get VR action identifiers that are supported by this action to bind
     * and available on specified interaction profile
     *
     * @param profileType the interaction profile type
     * @param keyModifiersActive if consider keyModifiers as active
     * @return the collection of binding identifiers
     */
    @NotNull
    Collection<VRActionIdentifier> getSupportedBindingIds(
            @NotNull VRInteractionProfileType profileType,
            boolean keyModifiersActive
    );

    /**
     * Get VR action identifiers that are supported by this action to bind
     * and available on specified interaction profile
     *
     * @param profileType the interaction profile type
     * @return the collection of binding identifiers
     */
    @NotNull
    default Collection<VRActionIdentifier> getSupportedBindingIds(@NotNull VRInteractionProfileType profileType){
        return getSupportedBindingIds(
                profileType,
                getActionSet().isKeyModifiersActive(profileType)
        );
    }


    /**
     * Get action set that is using this action.
     *
     * @return the action set
     */
    @NotNull
    VRActionSet getActionSet();


    /**
     * Get display name
     *
     * @return the name
     */
    @NotNull
    default Component getName(){
        return Component.translatable("visor.action."+getId());
    }

    /**
     * Get id
     * @return the id
     */
    @NotNull
    String getId();

}
