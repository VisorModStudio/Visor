package me.phoenixra.visor.api.client.input.action;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.VisorElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class VisorActionSet implements VisorElement, PrioritySupporter {

    @Getter
    private final VisorAddon owner;

    @Getter @Setter
    private boolean enabled;

    protected Map<String, VisorAction> actionsMap;

    public VisorActionSet(VisorAddon owner){
        this.owner = owner;
        this.actionsMap = new LinkedHashMap<>();
        for(var entry : loadActions()){
            actionsMap.put(entry.getId(), entry);
        }
    }

    protected abstract List<VisorAction> loadActions();

    public abstract boolean canActivate();

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
