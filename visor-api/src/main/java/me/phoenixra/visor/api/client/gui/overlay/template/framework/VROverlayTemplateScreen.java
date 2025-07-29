package me.phoenixra.visor.api.client.gui.overlay.template.framework;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptions;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.template.options.types.OverlayOptionsLocation;
import me.phoenixra.visor.api.client.gui.overlay.template.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.VROverlayTemplate;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for {@link VROverlayScreen} templates
 */
public abstract class VROverlayTemplateScreen extends VROverlayScreen implements VROverlayTemplate {
    @Getter
    private final String templateId;
    @Getter
    private final Component overlayName;

    @Getter
    private final ConfigFile config;
    protected final Map<Class<? extends OverlayOptions>, OverlayOptions> options;

    protected final OverlayOptionsGlobal optionsGlobal;
    protected final OverlayOptionsLocation optionsModelView;

    @Getter @Setter
    private @Nullable PoseAnchor demoAnchor;

    private boolean initializedPose;


    public VROverlayTemplateScreen(@NotNull VisorAddon owner,
                                   @NotNull String id) {
        this(owner, id, ElementPriority.NORMAL,1.0f);
    }
    public VROverlayTemplateScreen(@NotNull VisorAddon owner,
                                   @NotNull String id,
                                   @NotNull ElementPriority priority,
                                   float overlayScale) {
        super(owner, id, priority, overlayScale);

        try {
            this.config = VisorAPI.client()
                    .getGuiManager()
                    .getOverlayManager()
                    .getConfigOverlaysAccessor()
                    .getConfigOrCreate(owner, id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RegisterVROverlayTemplate annotation = getClass().getAnnotation(
                RegisterVROverlayTemplate.class
        );
        templateId = annotation.id();
        config.set("template", templateId);
        try {
            config.save();
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        options = new LinkedHashMap<>();
        List<OverlayOptions> preOptions = createOptions();
        preOptions.forEach(it->{
            options.put(it.getClass(),it);
        });

        optionsGlobal = (OverlayOptionsGlobal) options.get(OverlayOptionsGlobal.class);
        optionsModelView = (OverlayOptionsLocation) options.get(OverlayOptionsLocation.class);

        overlayName = Component.literal(
                config.getStringOrDefault("displayName", id)
        );
    }

    @NotNull
    protected abstract List<OverlayOptions> createOptions();


    @Override
    protected void onPreTick() {
        if(optionsGlobal != null
                && optionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.TICK) {
            options.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }

        if(optionsModelView != null
                && optionsModelView.isTickModelView()){
            updatePose(1);
        }
    }

    @Override
    protected void onPreRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if(optionsGlobal != null
                && optionsGlobal.getUpdateOptionsType() == OverlayOptionsGlobal.UpdateOptionsType.FRAME) {
            options.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }
    }

    @Override
    public void updatePose(float partialTicks) {
        if(optionsModelView == null) return;
        float scale = optionsGlobal == null
                ? getPose().getScale()
                : optionsGlobal.getOverlayScale();
        if(demoAnchor != null) {
            VROverlayHelper.applyPose(
                    this,
                    demoAnchor,
                    demoAnchor,
                    scale,
                    false,
                    new Vector3f(0,0,-0.3f),
                    new Vector3f(0,0,0)
            );
            return;
        }
        if(!initializedPose || optionsModelView.isTickModelView()) {
            VROverlayHelper.applyPose(
                    this,
                    optionsModelView.getPositionAnchor(),
                    optionsModelView.getRotationAnchor(),
                    scale,
                    optionsModelView.isAimRotation(),
                    optionsModelView.getPosOffset(),
                    optionsModelView.getRotationOffsetVec()
            );
            initializedPose = true;
        }
    }

    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        if(!flag) {
            initializedPose = false;
        }
    }

    @Override
    public @NotNull Collection<OverlayOptions> getTemplateOptions() {
        return options.values();
    }

    @Override
    public <T extends OverlayOptions> @Nullable T getTemplateOption(@NotNull Class<T> type) {
        var overlay = options.get(type);
        if(type.isInstance(overlay)){
            return type.cast(overlay);
        }
        return null;
    }
}
