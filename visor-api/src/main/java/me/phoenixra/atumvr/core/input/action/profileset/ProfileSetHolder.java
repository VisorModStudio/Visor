package me.phoenixra.atumvr.core.input.action.profileset;

import lombok.Getter;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ProfileSetHolder {
    @Getter
    private final SharedActionSet sharedSet;

    private OpenXRProfileSet lastActive;

    private final Map<XRInteractionProfile, OpenXRProfileSet> profileSetMap = new HashMap<>();

    public ProfileSetHolder(OpenXRProvider provider){
        this(new SharedActionSet(provider),
                        XRInteractionProfile.getSupportedProfileSets(provider)
        );
    }

    public ProfileSetHolder(@NotNull SharedActionSet sharedSet,
                            @NotNull List<OpenXRProfileSet> profileSets){
        this.sharedSet = sharedSet;
        for(var entry : profileSets){
            profileSetMap.put(entry.getType(), entry);
        }
    }


    /**
     *
     * @return null if unrecognizable profile or controllers weren't connected yet
     */
    public @Nullable OpenXRProfileSet getActiveProfileSet(){
        if(lastActive != null && lastActive.isProfileActive()){
            return lastActive;
        }
        for(var entry : profileSetMap.values()){
            if(entry.isProfileActive()) {
                lastActive = entry;
                return entry;
            }
        }
        return lastActive;
    }

    public @Nullable OpenXRProfileSet getProfileSet(XRInteractionProfile type){
        return profileSetMap.get(type);
    }

    public Collection<OpenXRProfileSet> getProfileSets(){
        return profileSetMap.values();
    }
    public List<? extends OpenXRActionSet> getAllSets(){
        var list = new ArrayList<OpenXRActionSet>();
        list.add(sharedSet);
        list.addAll(profileSetMap.values());
        return list;
    }

}
