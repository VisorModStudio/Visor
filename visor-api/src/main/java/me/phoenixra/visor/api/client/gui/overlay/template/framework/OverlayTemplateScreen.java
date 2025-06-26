package me.phoenixra.visor.api.client.gui.overlay.template.framework;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.template.options.sections.OverlayOptionsGlobal;
import me.phoenixra.visor.api.client.gui.overlay.template.options.sections.OverlayOptionsLocation;
import me.phoenixra.visor.api.client.gui.overlay.template.RegisterOverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlay.template.OverlayTemplate;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class OverlayTemplateScreen extends VROverlayScreen implements OverlayTemplate {
    @Getter
    private final String typeId;
    @Getter
    private final Component overlayName;

    @Getter
    private final ConfigFile typeConfig;
    protected final Map<Class<? extends OverlayOptionCategory>, OverlayOptionCategory> options;

    protected final OverlayOptionsGlobal optionsGlobal;
    protected final OverlayOptionsLocation optionsModelView;

    @Getter @Setter
    private @Nullable PoseAnchor demoAnchor;

    private boolean initializedPose;


    public OverlayTemplateScreen(@NotNull VisorAddon owner,
                                 @NotNull String id) {
        this(owner, id, ElementPriority.NORMAL,1.0f);
    }
    public OverlayTemplateScreen(@NotNull VisorAddon owner,
                                 @NotNull String id,
                                 @NotNull ElementPriority priority,
                                 float overlayScale) {
        super(owner, id, priority, overlayScale);

        try {
            this.typeConfig = VisorAPI.client()
                    .getGuiManager()
                    .getOverlayManager()
                    .getOverlayCatalog()
                    .getConfigOrCreate(id);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        RegisterOverlayTemplate annotation = getClass().getAnnotation(
                RegisterOverlayTemplate.class
        );
        typeId = annotation.id();
        typeConfig.set("template",typeId);
        try {
            typeConfig.save();
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        options = new LinkedHashMap<>();
        List<OverlayOptionCategory> preOptions = createOptions();
        preOptions.forEach(it->{
            options.put(it.getClass(),it);
        });

        optionsGlobal = (OverlayOptionsGlobal) options.get(OverlayOptionsGlobal.class);
        optionsModelView = (OverlayOptionsLocation) options.get(OverlayOptionsLocation.class);

        overlayName = Component.literal(
                typeConfig.getStringOrDefault("displayName", id)
        );
    }

    @NotNull
    protected abstract List<OverlayOptionCategory> createOptions();


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
    public @NotNull Collection<OverlayOptionCategory> getOptions() {
        return options.values();
    }

    @Override
    public <T extends OverlayOptionCategory> @Nullable T getOption(@NotNull Class<T> type) {
        var overlay = options.get(type);
        if(type.isInstance(overlay)){
            return type.cast(overlay);
        }
        return null;
    }
}
