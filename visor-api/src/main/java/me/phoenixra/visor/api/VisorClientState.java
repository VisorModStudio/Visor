package me.phoenixra.visor.api;

import me.phoenixra.visor.api.client.VRPlayMode;
import me.phoenixra.visor.api.client.VRStateMode;
import me.phoenixra.visor.api.client.render.RenderPhase;
import me.phoenixra.visor.api.client.render.VRDisplay;

public interface VisorClientState {

    VRPlayMode playMode();

    VRStateMode stateMode();



    RenderPhase renderPhase();

    VRDisplay renderingDisplay();
}
