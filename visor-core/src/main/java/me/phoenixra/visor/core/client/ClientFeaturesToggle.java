package me.phoenixra.visor.core.client;

import lombok.Getter;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

@Getter
public class ClientFeaturesToggle {

    private final EnumMap<ClientFeature, Boolean> features =
            new EnumMap<>(ClientFeature.class);

    public boolean isAllowed(@NotNull ClientFeature feature) {
        return features.getOrDefault(feature, false);
    }

    protected void preTick() {
        updateAllFeatures(false);
    }

    protected void preRender() {
        updateAllFeatures(true);
    }

    private void updateAllFeatures(boolean preRender) {
        for (ClientFeature feature : ClientFeature.values()) {
            if (feature.isRenderFeature() != preRender) continue;

            if(!checkFeature(feature)){
                features.put(feature, false);
                continue;
            }
            var event = new AllowClientFeatureVREvent(feature);
            VisorAPI.eventBus().callEvent(event);

            features.put(feature, !event.isCanceled());
        }
    }

    private boolean checkFeature(ClientFeature feature) {
        return switch (feature) {
            case AIM_EFFECTS -> checkAimEffects();
            case VR_HANDS  -> checkVRHands();
            case INPUT_MOVEMENT -> !ClientContext.overlayManager.isShowingKeyboard();
            default -> true;
        };
    }

    private boolean checkAimEffects() {
        if (MC.level == null)               return false;
        if (MC.screen != null)              return false;
        if (ClientContext.overlayManager.isShowingKeyboard()) return false;
        return !ClientContext.cursorHandler.isCursorHandFocused();
    }

    private boolean checkVRHands() {
        // TODO
        return true;
    }
}
