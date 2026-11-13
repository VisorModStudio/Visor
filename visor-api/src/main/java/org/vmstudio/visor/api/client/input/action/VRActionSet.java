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
import org.vmstudio.visor.api.client.input.action.framework.VRActionKeyCustom;
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
    protected Map<String, VRActionKeyCustom> customActionsMap;
    protected Map<VRInteractionProfileType, Boolean> keyModifiersActiveMap;


    @Getter
    protected ConfigFile config;

    public VRActionSet(@NotNull VisorAddon owner){
        this.owner = owner;
        this.actionsMap = new LinkedHashMap<>();
        this.customActionsMap = new LinkedHashMap<>();
        this.keyModifiersActiveMap = new EnumMap<>(VRInteractionProfileType.class);

        for(var action : loadActions()){
            actionsMap.put(action.getId(), action);
        }

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

        load();
    }

    /**
     * Loads actions that belong to this set.
     */
    protected abstract List<VRAction> loadActions();

    /**
     * Get map of default boolean flags
     * if key modifiers active for specific interaction profile type
     */
    public abstract Map<VRInteractionProfileType, Boolean> getDefaultKeyModifiersActive();

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
        for(var action : new ArrayList<>(getActions())){
            action.preTick();
        }
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
        for(var action : new ArrayList<>(getActions())){
            action.clear();
        }
    }



    /**
     * Loads bindings for all actions from config.
     */
    public void load(@NotNull Config config){

        customActionsMap.forEach(
                (key, value)->{
                    actionsMap.remove(key);
                }
        );
        customActionsMap.clear();
        keyModifiersActiveMap.clear();

        loadCustomActions(config);

        boolean requireSave = false;
        var subsection = config.getSubsection("bindings");
        var defaults = getDefaultKeyModifiersActive();
        for(var profileType : VRInteractionProfileType.valuesController()){
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
     * Loads custom actions from config.
     */
    protected void loadCustomActions(@NotNull Config config){
        boolean requireSave = false;
        var subsection = config.getSubsection("custom_actions");
        for(String key : subsection.getKeys(false)){
            if(getAction(key) != null){
                requireSave = true;
                continue; // custom actions are lower priority than built-in
            }
            String name = subsection.getString(key+".name");
            String mcKey = subsection.getStringOrNull(key+".mc_key");
            VRActionKeyCustom action;
            if(mcKey != null) {
                action = new VRActionKeyCustom(key,this,  mcKey, name);
            }else{
                char character = subsection.getString(key + ".key").charAt(0);
                action = new VRActionKeyCustom(key,this,  character, name);
            }

            actionsMap.put(key, action);
            customActionsMap.put(key, action);
        }
        if(requireSave){
            saveCustomActions();
        }
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
        for(var profileType : VRInteractionProfileType.valuesController()){
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

        var defaults = getDefaultKeyModifiersActive();
        for(var profileEntry : VRInteractionProfileType.valuesController()){
            keyModifiersActiveMap.put(profileEntry, defaults.getOrDefault(profileEntry, false));
        }

    }
    /**
     * Load default bindings for all profiles
     *
     */
    public void loadDefaults(){
        config.getFile().delete();
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
        load();
    }

    /**
     * Save bindings to config file (both left and right-handed)
     */
    public void save(){
        saveCustomActions();

        var subsectionPath = "bindings.";

        for(var entry : keyModifiersActiveMap.entrySet()){
            config.set(
                    subsectionPath+entry.getKey().name()+".key_modifiers_active",
                    entry.getValue()
            );
        }

        for(var action : actionsMap.values()){
            for(var profile : VRInteractionProfileType.valuesController()){
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
     * Saves custom actions to config.
     */
    public void saveCustomActions(){
        var subsectionPath = "custom_actions.";
        for(var action : customActionsMap.values()){
            if(action.isUsingMcKey()){
                config.set(
                        subsectionPath + action.getId() + ".mc_key",
                        action.getMcKey()
                );
            }else {
                config.set(
                        subsectionPath + action.getId() + ".key",
                        String.valueOf(action.getCharacter())
                );
            }
            config.set(
                    subsectionPath + action.getId() + ".name",
                    action.getLangKeyName()
            );
        }
        try {
            config.save();

        } catch (Exception e) {
            throw new VRException(e);
        }
    }



    /**
     * Add custom action to this set
     *
     * @param action the custom action
     */
    public void addCustomAction(@NotNull VRActionKeyCustom action){
        customActionsMap.put(action.getId(), action);
        actionsMap.put(action.getId(), action);
        if(loadBinding(action)){
            save();
        }
    }

    /**
     * Remove custom action from this set
     *
     * @param id the custom action id
     */
    public void removeCustomAction(@NotNull String id){
        customActionsMap.remove(id);
        actionsMap.remove(id);

        config.set("custom_actions." + id, null);

        var subsection = config.getSubsection("bindings");
        for(var profile : VRInteractionProfileType.valuesController()){
            String path = profile.name()+".actions."+id;
            subsection.set("left_handed."+path, null);
            subsection.set("right_handed."+path, null);
        }

        try {
            config.save();
        } catch (Exception e) {
            throw new VRException(e);
        }
    }

    /**
     * Get custom action or null if not found
     * @param id the custom action id
     * @return custom action or null
     */
    public @Nullable VRActionKeyCustom getCustomAction(@NotNull String id){
        return customActionsMap.get(id);
    }

    /**
     * Get all custom actions of this set
     *
     * @return collection of custom actions
     */
    public Collection<VRActionKeyCustom> getCustomActions(){
        return customActionsMap.values();
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
