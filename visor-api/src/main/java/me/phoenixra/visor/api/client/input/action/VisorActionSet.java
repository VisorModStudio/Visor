package me.phoenixra.visor.api.client.input.action;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.atumconfig.api.config.ConfigType;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.element.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.VisorElement;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public abstract class VisorActionSet implements VisorElement, PrioritySupporter {

    @Getter
    private final VisorAddon owner;

    @Setter @Getter
    private boolean enabled = true;

    protected Map<String, VisorAction> actionsMap;


    @Getter
    protected ConfigFile configRight;
    @Getter
    protected ConfigFile configLeft;

    public VisorActionSet(VisorAddon owner){
        this.owner = owner;
        this.actionsMap = new LinkedHashMap<>();

        try {
            configRight = VisorAPI.client().getConfigManager()
                    .createConfigFile(
                            ConfigType.JSON,
                            "right_action_set_" + getId(),
                            Path.of("input/rightHanded/"+getId()+".json")
                    );

            configLeft = VisorAPI.client().getConfigManager()
                    .createConfigFile(
                            ConfigType.JSON,
                            "left_action_set_" + getId(),
                            Path.of("input/leftHanded/"+getId()+".json")
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

        loadBindings();

    }

    protected abstract List<VisorAction> loadActions();

    public abstract boolean canActivate();


    public boolean isEnabledAndCanActivate() {
        return enabled && canActivate();
    }

    public Component getName() {
        return Component.translatable("visor.action_sets."+getId());
    }

    public void preTick(){
        getActions().forEach(
                VisorAction::preTick
        );
    }


    public void updateState(OpenXRProfileSet currentProfile, boolean leftHanded){
        getActions().forEach(
                it-> it.updateState(currentProfile, leftHanded)
        );
    }


    public void clear(){
        getActions().forEach(
                VisorAction::clear
        );
    }

    public void loadBindings(){
        for(var action : actionsMap.values()){
            for(var profile : XRInteractionProfile.values()){
                String path = profile.name()+"."+action.getId();
                String leftHandedPath = configLeft.getStringOrDefault(path, BindingPath.EMPTY_PATH);
                String rightHandedPath = configRight.getStringOrDefault(path, BindingPath.EMPTY_PATH);

                action.setBinding(profile, new BindingPath(rightHandedPath, leftHandedPath));
            }
        }
    }

    public void loadDefaults(XRInteractionProfile profile){
        getActions().forEach(
                it->{
                    var def =  it.getDefaultBinding(profile);
                    if(def == null){
                        def = BindingPath.EMPTY;
                    }
                    it.setBinding(profile, def);
                }
        );
    }

    public void saveBindings(){
        for(var action : actionsMap.values()){
            for(var profile : XRInteractionProfile.values()){
                BindingPath binding = action.getBindingOrEmpty(profile);
                String path = profile.name()+"."+action.getId();

                configLeft.set(path, binding.getLeftHandedPath());
                configRight.set(path, binding.getRightHandedPath());
            }
        }
        try {
            configLeft.save();
            configRight.save();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
    public void saveBindings(boolean leftHanded){
        for(var action : actionsMap.values()){
            for(var profile : XRInteractionProfile.values()){
                BindingPath binding = action.getBindingOrEmpty(profile);
                String path = profile.name()+"."+action.getId();

                if(leftHanded){
                    configLeft.set(path, binding.getLeftHandedPath());
                }else {
                    configRight.set(path, binding.getRightHandedPath());
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


    public VisorAction getAction(String id){
        return actionsMap.get(id);
    }

    public Collection<VisorAction> getActions(){
        return actionsMap.values();
    }






    @Override
    public @NotNull ElementPriority getPriority(){
        return ElementPriority.NORMAL;
    }
}
