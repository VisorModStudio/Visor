package me.phoenixra.visor.api.client.input.button;

import me.phoenixra.atumvr.core.input.action.profileset.OpenXRProfileSet;
import me.phoenixra.atumvr.core.input.action.profileset.ProfileSetHolder;
import me.phoenixra.visor.api.common.addon.VisorElement;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;


public interface VisorAction extends VisorElement {


    void tick();


    void updateState(OpenXRProfileSet currentProfile);


    boolean isActive();




}
