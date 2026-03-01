package org.vmstudio.visor.api.client.gui.overlays.options.types;

import lombok.Getter;
import me.phoenixra.atumconfig.api.config.Config;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.client.gui.overlays.options.OptionsScreen;
import org.vmstudio.visor.api.client.gui.overlays.options.types.properties.Property;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OverlayOptionsGeneral extends OverlayOptionGroup<OverlayOptionsGeneral> {
    public static final String ID = "general";
    private static final Component NAME = Component.translatable("visor.overlay.options."+ID);


    private final Map<String, Property<?>> propertyMap;

    @Getter
    private final Collection<Property<?>> propertyList;

    public OverlayOptionsGeneral(@NotNull VROverlay owner,
                                 @NotNull List<Property<?>> properties){
        super(owner,
                (it)-> {
                    it.propertyMap.forEach(
                            (key, property) ->
                                    property.loadDefault()
                    );
                }
        );
        propertyMap = new LinkedHashMap<>();
        Runnable responder = ()->{
            changesNotSaved = true;
        };
        for(var property : properties){
            propertyMap.put(property.getKey(), property);
            property.setResponder(responder);
        }
        propertyList = Collections.unmodifiableCollection(propertyMap.values());
    }

    @Override
    public void update(boolean reset) {
        for(var entry : propertyMap.entrySet()){
            entry.getValue().update();
        }
    }

    @Override
    protected void onLoad(@NotNull Config config){
        for(var entry : propertyMap.entrySet()){
            entry.getValue().onLoad(config);
        }
    }
    @Override
    public void onSave(@NotNull Config config){
        for(var entry : propertyMap.entrySet()){
            entry.getValue().onSave(config);
        }
    }

    @Override
    public boolean supportsCopying() {
        return false;
    }

    @Nullable
    public <T extends Property<?>> T getProperty(String id, Class<T> type) {
        Property<?> p = propertyMap.get(id);
        if (p == null) {
            return null;
        }
        if (!type.isInstance(p)) {
            throw new IllegalArgumentException(
                    "Property '" + id + "' is " + p.getClass().getSimpleName() + " but " + type.getSimpleName() + " was requested"
            );
        }
        return type.cast(p);
    }

    @Override
    public @NotNull OptionsScreen<?> getScreen() {
        return VisorAPI.client().getGuiManager().getOverlayManager().getOptionsScreenFor(
                this
        );
    }

    @Override
    public @NotNull Component getDisplayName() {
        return NAME;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }




}
