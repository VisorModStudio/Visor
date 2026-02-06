package me.phoenixra.visor.api.client.input.action;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumvr.api.input.action.VRActionIdentifier;
import me.phoenixra.atumvr.core.input.profile.XRInteractionProfile;
import me.phoenixra.atumvr.api.input.profile.VRInteractionProfileType;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.addon.component.ComponentPriority;
import me.phoenixra.visor.api.common.addon.component.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.component.VisorComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

/**
 * Base class for visor action sets.
 * <p>
 *     An action set groups actions and manages their bindings/configuration.<br>
 *     Only one action set is active at a time.
 * </p>
 */
public abstract class VisorActionSet implements VisorComponent, PrioritySupporter {

    @Getter
    private final VisorAddon owner;

    @Setter @Getter
    private boolean enabled = true;

    protected Map<String, VisorAction> actionsMap;
    protected Map<String, VisorActionKeyboard> keyboardActionsMap;


    @Getter
    protected ConfigFile configLeft;
    @Getter
    protected ConfigFile configRight;

    @Getter
    protected ConfigFile configKeyboardActions;

    public VisorActionSet(@NotNull VisorAddon owner){
        this.owner = owner;
        this.actionsMap = new LinkedHashMap<>();
        this.keyboardActionsMap = new LinkedHashMap<>();

        try {

            configKeyboardActions =  VisorAPI.client().getConfigManager()
                    .createConfigFile(
                            ConfigType.JSON,
                            getId() +"_keyActions",
                            Path.of("input/"+getId()+"_keyActions"+".json")
                    );

            configLeft = VisorAPI.client().getConfigManager()
                    .createConfigFile(
                            ConfigType.JSON,
                            "left_action_set_" + getId(),
                            Path.of("input/leftHanded/"+getId()+".json")
                    );

            configRight = VisorAPI.client().getConfigManager()
                    .createConfigFile(
                            ConfigType.JSON,
                            "right_action_set_" + getId(),
                            Path.of("input/rightHanded/"+getId()+".json")
                    );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for(var action : loadActions()){
            actionsMap.put(action.getId(), action);
        }

        //save current bindings if config has no subsections, i.e. empty
        if(configLeft.getAllSubsections().isEmpty()){
            saveBindings(true);
        }

        if(configRight.getAllSubsections().isEmpty()){
            saveBindings(false);
        }

        loadKeyboardActions();
        loadBindings();

    }

    /**
     * Loads actions that belong to this set.
     */
    protected abstract List<VisorAction> loadActions();

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
     * Called before each tick to allow actions to update state.
     */
    public void preTick(){
        getActions().forEach(
                VisorAction::preTick
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
                VisorAction::clear
        );
    }

    /**
     * Loads keyboard actions from config.
     */
    public void loadKeyboardActions(){
        for(String key : configKeyboardActions.getKeys(false)){
            if(key.length() > 1) {
                return;
            }
            char character = key.charAt(0);
            String nameKey = configKeyboardActions.getString(key);

            var action = new VisorActionKeyboard(this, character, nameKey);
            actionsMap.put(action.getId(), action);
            keyboardActionsMap.put(action.getId(), action);

        }
    }

    /**
     * Saves keyboard actions to config.
     */
    public void saveKeyboardActions(){
        for(var action : keyboardActionsMap.values()){
            configKeyboardActions.set(
                    String.valueOf(action.getCharacter()),
                    action.getNameKey()
            );
        }
        try {
            configKeyboardActions.save();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads bindings for all actions from config.
     */
    public void loadBindings(){
        for(var action : actionsMap.values()){
            loadBinding(action);
        }
    }

    /**
     * Loads bindings for a specified action from config.
     *
     * @param action the visor action to load
     */
    public void loadBinding(@NotNull VisorAction action){
        for(var profileType : VRInteractionProfileType.values()){
            String path = profileType.name()+"."+action.getId();
            String leftHandedPath = configLeft.getStringOrNull(path);
            String rightHandedPath = configRight.getStringOrNull(path);
            VRActionIdentifier leftHandedId = ActionBinding.EMPTY_ID;
            VRActionIdentifier rightHandedId = ActionBinding.EMPTY_ID;
            if(leftHandedPath != null){
                leftHandedId = new VRActionIdentifier(leftHandedPath);
                if (!VRInteractionProfileType
                        .getActionIdsOf(profileType)
                        .contains(leftHandedId)){
                    leftHandedId = ActionBinding.EMPTY_ID;
                }
            }

            if(rightHandedPath != null){
                rightHandedId = new VRActionIdentifier(rightHandedPath);
                if (!VRInteractionProfileType
                        .getActionIdsOf(profileType)
                        .contains(rightHandedId)){
                    rightHandedId = ActionBinding.EMPTY_ID;
                }
            }

            action.setBinding(
                    profileType,
                    new ActionBinding(rightHandedId, leftHandedId)
            );
        }
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
    }

    /**
     * Save bindings to config file (both left and right-handed)
     */
    public void saveBindings(){
        saveKeyboardActions();

        for(var action : actionsMap.values()){
            for(var profile : VRInteractionProfileType.values()){
                ActionBinding binding = action.getBindingOrEmpty(profile);
                String path = profile.name()+"."+action.getId();

                configLeft.set(path, binding.getLeftHandedId().getValue());
                configRight.set(path, binding.getRightHandedId().getValue());
            }
        }
        try {
            configLeft.save();
            configRight.save();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Save bindings to config file (left or right-handed)
     */
    public void saveBindings(boolean leftHanded){
        for(var action : actionsMap.values()){
            for(var profile : VRInteractionProfileType.values()){
                ActionBinding binding = action.getBindingOrEmpty(profile);
                String path = profile.name()+"."+action.getId();

                if(leftHanded){
                    configLeft.set(path, binding.getLeftHandedId().getValue());
                }else {
                    configRight.set(path, binding.getRightHandedId().getValue());
                }
            }
        }
        try {
            if(leftHanded){
                configLeft.save();
            }else {
                configRight.save();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * Add keyboard action to this set
     *
     * @param action the keyboard action
     */
    public void addKeyboardAction(@NotNull VisorActionKeyboard action){
        keyboardActionsMap.put(action.getId(), action);
        actionsMap.put(action.getId(), action);
        loadBinding(action);
    }

    /**
     * Remove keyboard action from this set
     *
     * @param action the keyboard action
     */
    public void removeKeyboardAction(@NotNull VisorActionKeyboard action){
        keyboardActionsMap.remove(action.getId());
        actionsMap.remove(action.getId());
        for(var profile : VRInteractionProfileType.values()){
            String path = profile.name()+"."+action.getId();
            configLeft.set(path, null);
            configRight.set(path, null);
        }
        try {
            configLeft.save();
            configRight.save();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get keyboard action or null if not found
     * @param id the keyboard action id
     * @return keyboard action or null
     */
    public @NotNull VisorActionKeyboard getKeyboardAction(@NotNull String id){
        return keyboardActionsMap.get(id);
    }

    /**
     * Get all keyboard actions of this set
     *
     * @return collection of keyboard actions
     */
    public Collection<VisorActionKeyboard> getKeyboardActions(){
        return keyboardActionsMap.values();
    }


    /**
     * Get action of specified id
     *
     * @param id the action id
     * @return the action
     */
    public VisorAction getAction(@NotNull String id){
        return actionsMap.get(id);
    }

    /**
     * Get all actions of this set
     *
     * @return collection of actions
     */
    public Collection<VisorAction> getActions(){
        return actionsMap.values();
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
