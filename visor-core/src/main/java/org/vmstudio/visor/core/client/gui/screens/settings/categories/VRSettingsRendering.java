package org.vmstudio.visor.core.client.gui.screens.settings.categories;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.gui.screens.settings.VROptionsSet;
import org.vmstudio.visor.core.client.gui.screens.settings.VRSettingsScreen;
import org.vmstudio.visor.compatibility.dh.DhCompatHelper;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsEyeEffects;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsMixedReality;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsShaders;
import org.vmstudio.visor.core.client.gui.screens.settings.categories.rendering.VRSettingsThirdPerson;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.VROptionWidgetType;
import org.vmstudio.visor.core.client.settings.options.enums.MirrorMode;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetEntry;
import org.vmstudio.visor.core.client.gui.screens.settings.OptionWidgetPosition;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;


public class VRSettingsRendering extends VROptionsSet {


    public VRSettingsRendering(@NotNull VRSettingsScreen screen, @Nullable VROptionsSet previousOptions, @NotNull Runnable onWidgetsChanged) {
        super(screen, previousOptions, onWidgetsChanged);
    }

    @Override
    protected VROptionWidgetType[] getOptionTypes() {

        return new VROptionWidgetType[0];
    }

    @Override
    protected OptionWidgetEntry[] getOptionEntries() {
        List<OptionWidgetEntry> options = new ArrayList<>();
        options.add(
                new OptionWidgetEntry(
                        this,
                        VROptionWidgetType.MIRROR_MODE,
                        OptionWidgetPosition.LEFT,
                        0,
                        null
                )
        );
        options.add(
                new OptionWidgetEntry(
                        this,
                        new VRSettingsEyeEffects(getScreen(), this, onWidgetsChanged),
                        OptionWidgetPosition.RIGHT,
                        0,
                        "visor.options.rendering.eye_effects.button"
                )
        );

        if (IrisCompatHelper.isLoaded()) {
            options.add(
                    new OptionWidgetEntry(
                            this,
                            new VRSettingsShaders(getScreen(), this, onWidgetsChanged),
                            OptionWidgetPosition.RIGHT,
                            1,
                            "visor.options.rendering.shaders.button"
                    )
            );
        }

        MirrorMode mirrorMode = VRClientSettings.getMirrorMode();

        if(mirrorMode == MirrorMode.CROPPED
                || mirrorMode == MirrorMode.SINGLE){
            options.add(
                    new OptionWidgetEntry(
                            this,
                            VROptionWidgetType.MIRROR_EYE,
                            OptionWidgetPosition.LEFT,
                            1,
                            null
                    )
            );
        } else if(mirrorMode == MirrorMode.THIRD_PERSON){
            options.add(
                    new OptionWidgetEntry(
                            this,
                            new VRSettingsThirdPerson(getScreen(), this, onWidgetsChanged),
                            OptionWidgetPosition.LEFT,
                            1,
                            "visor.options.rendering.third_person.button"
                    )
            );
        }else if(mirrorMode == MirrorMode.MIXED_REALITY){
            options.add(
                    new OptionWidgetEntry(
                            this,
                            new VRSettingsMixedReality(getScreen(), this, onWidgetsChanged),
                            OptionWidgetPosition.LEFT,
                            1,
                            "visor.options.rendering.mixed_reality.button"
                    )
            );
        }

        if (DhCompatHelper.isLoaded()
                && (mirrorMode == MirrorMode.FIRST_PERSON
                        || mirrorMode == MirrorMode.THIRD_PERSON
                        || mirrorMode == MirrorMode.MIXED_REALITY)) {
            options.add(
                    new OptionWidgetEntry(
                            this,
                            VROptionWidgetType.DH_MIRROR_PASSES,
                            OptionWidgetPosition.LEFT,
                            2,
                            null
                    )
            );
        }


        return options.toArray(new OptionWidgetEntry[0]);
    }


    @Override
    public void loadDefaults() {
        super.loadDefaults();
        MC.options.fov().set(70);
        if(VisorState.get().isActive()) {
            ClientContext.renderer.prepareReinit("Defaults Loaded");
        }
    }

    @Override
    protected void mouseClicked(double mouseX, double mouseY,
                                int button,
                                boolean success) {
        if(!success) return;

        if(!(getScreen().getFocused() instanceof AbstractWidget clicked)){
            return;
        }

        var clickedOption = getTypeFromWidget(clicked);
        if(clickedOption == null) return;

        if(clickedOption == VROptionWidgetType.MIRROR_MODE){
            reinit();
        }
    }
}
