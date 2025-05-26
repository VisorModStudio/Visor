package me.phoenixra.visor.api.client;

public interface IClientProperties {
    /**
     * If movement modifiers are blocked.
     * I.e. what intercepts in a movement, like climb,
     * jump, crawl trackers
     * @return blocked/free
     */
    boolean isMoveModifiersAllowed();

    /**
     * If whatever movement via VR Input buttons is blocked
     */
    boolean isInputMovementAllowed();

    boolean isAimEffectsAllowed();

    boolean isVrHandsAllowed();

}
