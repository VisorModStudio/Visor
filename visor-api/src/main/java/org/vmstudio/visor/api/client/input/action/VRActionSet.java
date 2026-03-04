package org.vmstudio.visor.api.client.input.action;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.Config;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.input.action.framework.VRActionKey;
import org.vmstudio.visor.api.common.VRException;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.component.PrioritySupporter;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

/**
 * Base class for VR action sets.
 * <p>
 *     An action set groups actions and manages their bindings/configuration.<br>
 *     Only one action set is active at a time.
 * </p>
 */
public abstract class VRActionSet implements VisorComponent, PrioritySupporter {

    @Getter
    private final VisorAddon owner;

    @Setter @Getter
    private boolean enabled = true;

    protected Map<String, VRAction> actionsMap;
    protected Map<String, VRActionKey> keyActionsMap;
    protected Map<VRInteractionProfileType, Boolean> keyModifiersActiveMap;


    @Getter
    protected ConfigFile config;

    public VRActionSet(@NotNull VisorAddon owner){
        this.owner = owner;
        this.actionsMap = new LinkedHashMap<>();
        this.keyActionsMap = new LinkedHashMap<>();
        this.keyModifiersActiveMap = new EnumMap<>(VRInteractionProfileType.class);

        try {

            config =  VisorAPI.client().getConfigManager()
                    .createConfigFile(
                            ConfigType.JSON,
                            getId(),
                            Path.of("controls/"+getId()+".json")
                    );

        } catch (Exception e) {
            throw new VRException(e);
        }

        for(var action : loadActions()){
            actionsMap.put(action.getId(), action);
        }


        loadKeyActions();
        load();

    }

    /**
     * Loads actions that belong to this set.
     */
    protected abstract List<VRAction> loadActions();

    /**
     * Loads map of default boolean flags
     * if key modifiers active for specific interaction profile type
     */
    protected abstract Map<VRInteractionProfileType, Boolean> loadDefaultKeyModifiersActive();

    /**
     * If the set can be activated right now.
     */
    public abstract boolean canActivate();


    /**
     * If enabled and allowed to activate
     *
     * @return true/false
     */
    public boolean isEnabledAndCanActivate() {
        return enabled && canActivate();
    }

    /**
     * Display name for the action set.
     */
    public Component getName() {
        return Component.translatable("visor.action_sets."+getId());
    }

    /**
     * Tooltip for the action set.
     */
    public Component getTooltip() {
        return Component.translatable("visor.action_sets."+getId()+".tooltip");
    }

    /**
     * Called before each tick to allow actions to update state.
     */
    public void preTick(){
        getActions().forEach(
                VRAction::preTick
        );
    }

    /**
     * Updates the state of all actions using the current profile.
     */
    public void updateState(@NotNull XRInteractionProfile currentProfile,
                            boolean leftHanded){
        getActions().forEach(
                it-> it.updateState(currentProfile, leftHanded)
        );
    }

    /**
     * Clears state of all actions.
     */
    public void clear(){
        getActions().forEach(
                VRAction::clear
        );
    }



    /**
     * Loads bindings for all actions from config.
     */
    public void load(@NotNull Config config){
        boolean requireSave = false;
        var subsection = config.getSubsection("bindings");
        var defaults = loadDefaultKeyModifiersActive();
        for(var profileType : VRInteractionProfileType.values()){
            Boolean keyModifiersActive = subsection.getBoolOrNull(
                    profileType.name()+".key_modifiers_active");
            if(keyModifiersActive == null){
                keyModifiersActive = defaults.getOrDefault(profileType, false);
                requireSave = true;
            }
            keyModifiersActiveMap.put(profileType, keyModifiersActive);
        }
        for(var action : actionsMap.values()){
            boolean flag = loadBinding(config,action);
            if(flag){
                requireSave = true;
            }
        }
        if(requireSave) {
            save();
        }
    }

    /**
     * Loads bindings for all actions from config.
     */
    public void load(){
        load(config);
    }

    /**
     * Loads key actions from config.
     */
    public void loadKeyActions(@NotNull Config config){
        boolean requireSave = false;
        var subsection = config.getSubsection("key_actions");
        for(String key : subsection.getKeys(false)){
            if(getAction(key) != null){
                requireSave = true;
                continue; // key actions are lower priority than built-in
            }
            char character = subsection.getString(key+".key").charAt(0);
            String name = subsection.getString(key+".name");

            var action = new VRActionKey(key,this,  character, name);
            actionsMap.put(key, action);
            keyActionsMap.put(key, action);
        }
        if(requireSave){
            saveKeyActions();
        }
    }

    /**
     * Loads key actions from config.
     */
    public void loadKeyActions() {
        loadKeyActions(config);
    }

    /**
     * Loads bindings for a specified action from config.
     *
     * @param action the VR action to load
     * @return If require to call {@link #save()} (after finished loading of other action bindings if loading the action list)
     */
    public boolean loadBinding(@NotNull Config config,
                               @NotNull VRAction action){
        var subsection = config.getSubsection("bindings");
        var defaults = action.getDefaultBindings();
        boolean requireSave = false;
        for(var profileType : VRInteractionProfileType.values()){
            boolean keyModifiersActive = isKeyModifiersActive(profileType);

            String idPath = profileType.name() + ".actions." + action.getId()+".path";
            String keyModifierPath = profileType.name() + ".actions." + action.getId()+".key_modifier";

            String leftHandedPath = subsection.getStringOrNull("left_handed."+idPath);
            String rightHandedPath = subsection.getStringOrNull("right_handed."+idPath);

            VRActionIdentifier leftHandedId;
            ActionKeyModifierType leftHandedKeyModifier = ActionKeyModifierType.OFF;
            VRActionIdentifier rightHandedId;
            ActionKeyModifierType rightHandedKeyModifier = ActionKeyModifierType.OFF;

            ActionBinding defaultBinding = defaults.getOrDefault(profileType, ActionBinding.EMPTY);

            var supportedIds = action.getSupportedBindingIds(profileType);

            if(leftHandedPath != null){
                leftHandedId = new VRActionIdentifier(leftHandedPath);
                if (!supportedIds.contains(leftHandedId)){
                    leftHandedId = ActionBinding.ID_EMPTY;
                    requireSave = true;
                }
                if(keyModifiersActive) {
                    leftHandedKeyModifier = ActionKeyModifierType.valueOf(
                            subsection.getStringOrDefault("left_handed."+keyModifierPath, "OFF")
                    );
                }
            }else{
                leftHandedId = defaultBinding.getLeftHandedId();
                if(keyModifiersActive) {
                    leftHandedKeyModifier = defaultBinding.getLeftHandedKeyModifier();
                }
                requireSave = true;
            }

            if(rightHandedPath != null){
                rightHandedId = new VRActionIdentifier(rightHandedPath);
                if (!supportedIds.contains(rightHandedId)){
                    rightHandedId = ActionBinding.ID_EMPTY;
                    requireSave = true;
                }
                if(keyModifiersActive) {
                    rightHandedKeyModifier = ActionKeyModifierType.valueOf(
                            subsection.getStringOrDefault("right_handed."+keyModifierPath, "OFF")
                    );
                }
            }else{
                rightHandedId = defaultBinding.getRightHandedId();
                if(keyModifiersActive) {
                    rightHandedKeyModifier = defaultBinding.getRightHandedKeyModifier();
                }
                requireSave = true;
            }

            action.setBinding(
                    profileType,
                    new ActionBinding(
                            rightHandedKeyModifier, rightHandedId,
                            leftHandedKeyModifier, leftHandedId
                    )
            );
        }
        return requireSave;
    }

    /**
     * Loads bindings for a specified action from config.
     *
     * @param action the VR action to load
     * @return If require to call {@link #save()} (after finished loading of other action bindings if loading the action list)
     */
    public boolean loadBinding(@NotNull VRAction action){
        return loadBinding(config, action);
    }


    /**
     * Load default bindings for specified interaction profile type
     *
     * @param profileType the interaction profile type
     */
    public void loadDefaults(@NotNull VRInteractionProfileType profileType){
        getActions().forEach(
                it->{
                    var def =  it.getDefaultBinding(profileType);
                    if(def == null){
                        def = ActionBinding.EMPTY;
                    }
                    it.setBinding(profileType, def);
                }
        );

        var defaults = loadDefaultKeyModifiersActive();
        for(var profileEntry : VRInteractionProfileType.values()){
            keyModifiersActiveMap.put(profileEntry, defaults.getOrDefault(profileEntry, false));
        }

    }
    /**
     * Load default bindings for all profiles
     *
     */
    public void loadDefaults(){
        for(var type : VRInteractionProfileType.values()){
            loadDefaults(type);
        }
    }

    /**
     * Save bindings to config file (both left and right-handed)
     */
    public void save(){
        saveKeyActions();

        var subsectionPath = "bindings.";

        for(var entry : keyModifiersActiveMap.entrySet()){
            config.set(
                    subsectionPath+entry.getKey().name()+".key_modifiers_active",
                    entry.getValue()
            );
        }

        for(var action : actionsMap.values()){
            for(var profile : VRInteractionProfileType.values()){
                ActionBinding binding = action.getBindingOrEmpty(profile);
                String idPath = profile.name()+".actions."+action.getId()+".path";
                String keyModifierPath = profile.name()+".actions."+action.getId()+".key_modifier";

                config.set(subsectionPath+"left_handed."+idPath, binding.getLeftHandedId().getValue());
                config.set(subsectionPath+"left_handed."+keyModifierPath, binding.getLeftHandedKeyModifier().name());
                config.set(subsectionPath+"right_handed."+idPath, binding.getRightHandedId().getValue());
                config.set(subsectionPath+"right_handed."+keyModifierPath, binding.getRightHandedKeyModifier().name());
            }
        }
        try {

            config.save();

        } catch (Exception e) {
            throw new VRException(e);
        }

    }

    /**
     * Saves key actions to config.
     */
    public void saveKeyActions(){
        var subsectionPath = "key_actions.";
        for(var action : keyActionsMap.values()){
            config.set(
                    subsectionPath + action.getId() + ".key",
                    String.valueOf(action.getCharacter())
            );
            config.set(
                    subsectionPath + action.getId() + ".name",
                    action.getNameKey()
            );
        }
        try {
            config.save();

        } catch (Exception e) {
            throw new VRException(e);
        }
    }



    /**
     * Add key action to this set
     *
     * @param action the key action
     */
    public void addKeyAction(@NotNull VRActionKey action){
        keyActionsMap.put(action.getId(), action);
        actionsMap.put(action.getId(), action);
        if(loadBinding(action)){
            save();
        }
    }

    /**
     * Remove key action from this set
     *
     * @param id the key action id
     */
    public void removeKeyAction(@NotNull String id){
        keyActionsMap.remove(id);
        actionsMap.remove(id);

        config.set(id, null);

        var subsection = config.getSubsection("bindings");
        for(var profile : VRInteractionProfileType.values()){
            String path = profile.name()+".actions."+id;
            subsection.set(path, null);
            subsection.set(path, null);
        }
        try {
            config.save();
        } catch (Exception e) {
            throw new VRException(e);
        }
    }

    /**
     * Get key action or null if not found
     * @param id the key action id
     * @return key action or null
     */
    public @Nullable VRActionKey getKeyAction(@NotNull String id){
        return keyActionsMap.get(id);
    }

    /**
     * Get all key actions of this set
     *
     * @return collection of key actions
     */
    public Collection<VRActionKey> getKeyActions(){
        return keyActionsMap.values();
    }


    /**
     * Get action of specified id
     *
     * @param id the action id
     * @return the action
     */
    public @Nullable VRAction getAction(@NotNull String id){
        return actionsMap.get(id);
    }

    /**
     * Get all actions of this set
     *
     * @return collection of actions
     */
    public Collection<VRAction> getActions(){
        return actionsMap.values();
    }



    public void setKeyModifiersActive(@NotNull VRInteractionProfileType profileType,
                                      boolean flag){
        keyModifiersActiveMap.put(profileType, flag);
    }

    public boolean isKeyModifiersActive(@NotNull VRInteractionProfileType profileType){
        return keyModifiersActiveMap.getOrDefault(profileType, false);
    }


    /**
     * Get priority of this action set.
     * <p>
     *     The priority determine which
     *     action set will be active if more than one return true
     *     with this method {@link #isEnabledAndCanActivate()}
     * </p>
     * @return the priority
     */
    @Override
    public @NotNull ComponentPriority getPriority(){
        return ComponentPriority.NORMAL;
    }
}
