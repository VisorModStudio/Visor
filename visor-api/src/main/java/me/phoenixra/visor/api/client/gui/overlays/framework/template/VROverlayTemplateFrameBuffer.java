package me.phoenixra.visor.api.client.gui.overlays.framework.template;

import com.mojang.blaze3d.pipeline.RenderTarget;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.phoenixra.visor.api.client.gui.GuiTexture;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayFrameBuffer;
import me.phoenixra.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsMisc;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsIdentity;
import me.phoenixra.visor.api.client.gui.overlays.options.types.OverlayOptionsPose;
import me.phoenixra.visor.api.client.gui.overlays.RegisterVROverlayTemplate;
import me.phoenixra.visor.api.client.gui.overlays.VROverlayTemplate;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract class for {@link VROverlayFrameBuffer} templates
 */
public abstract class VROverlayTemplateFrameBuffer extends VROverlayFrameBuffer implements VROverlayTemplate {

    @Getter @Accessors(makeFinal = true)
    private final String templateId;
    @Getter @Accessors(makeFinal = true)
    private final Component templateName;
    @Getter @Accessors(makeFinal = true)
    private final Component templateDescription;

    //Custom overlay
    @Getter @Setter
    @Accessors(makeFinal = true)
    private Component name;
    @Getter @Setter
    @Accessors(makeFinal = true)
    private Component description;
    @Getter @Setter
    @Accessors(makeFinal = true)
    private GuiTexture icon;

    protected final OverlayOptionsIdentity optionsIdentity;
    protected final OverlayOptionsMisc optionsGlobal;
    protected final OverlayOptionsPose optionsPose;


    protected boolean initializedPose;


    public VROverlayTemplateFrameBuffer(@NotNull VisorAddon owner,
                                        @NotNull String id) {
        this(owner, id, ElementPriority.NORMAL,null,1.0f);
    }
    public VROverlayTemplateFrameBuffer(@NotNull VisorAddon owner,
                                        @NotNull String id,
                                        @NotNull ElementPriority priority,
                                        @Nullable RenderTarget renderTarget,
                                        float overlayScale) {
        super(owner, id, priority, renderTarget, overlayScale);

        RegisterVROverlayTemplate annotation = getClass().getAnnotation(
                RegisterVROverlayTemplate.class
        );
        templateId = annotation.id();
        templateName = Component.translatable(annotation.name());
        templateDescription = Component.translatable(annotation.description());


        if(getOptions().isEmpty()){
            throw new IllegalArgumentException(
                    "Tried to instantiate an overlay template '%s' with NO OPTIONS"
                            .formatted(templateId)
            );
        }



        optionsIdentity = getOption(OverlayOptionsIdentity.ID, OverlayOptionsIdentity.class);
        optionsGlobal = getOption(OverlayOptionsMisc.ID, OverlayOptionsMisc.class);
        optionsPose = getOption(OverlayOptionsPose.ID, OverlayOptionsPose.class);

        //NULL POINTER EXCEPTION
        Objects.requireNonNull(optionsIdentity);

        updateIdentity();
    }

    @NotNull
    protected abstract List<OverlayOptionGroup<?>> createTemplateOptions();

    @Override
    protected final @NotNull List<OverlayOptionGroup<?>> createOptions() {
        var options = new ArrayList<OverlayOptionGroup<?>>();
        //Identity always added for custom overlays
        options.add(
                new OverlayOptionsIdentity(
                        this,
                        (it)->{
                            var defIcon = VisorAddon.MISSING_ICON.getResourceLocation();
                            it.setName(getId());
                            it.setDescription("No description");
                            it.setIcon(defIcon.getNamespace()+":"+defIcon.getPath());
                        })
        );
        options.addAll(createTemplateOptions());
        return options;
    }

    @Override
    protected final void initOptions() {
        RegisterVROverlayTemplate annotation = getClass().getAnnotation(
                RegisterVROverlayTemplate.class
        );
        optionsConfig.set("template", annotation.id());

        super.initOptions();
    }

    @Override
    protected void onPreTick() {

        if(optionsGlobal != null
                && optionsGlobal.getOptionsUpdaterType() == OverlayOptionsMisc.OptionsUpdaterType.TICK) {
            optionsMap.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }

        if(optionsPose != null
                && optionsPose.isTickModelView()){
            updatePose(1);
        }
    }

    @Override
    protected void onPreRender(float partialTicks) {
        if(optionsGlobal != null
                && optionsGlobal.getOptionsUpdaterType() == OverlayOptionsMisc.OptionsUpdaterType.FRAME) {
            optionsMap.forEach(
                    (key,value)
                            ->
                            value.update(false)
            );
        }
    }

    @Override
    public void onUpdatePose(float partialTicks) {
        if(optionsPose == null) return;
        float scale = optionsGlobal == null
                ? getPose().getScale()
                : optionsPose.getScale();
        if(!initializedPose || optionsPose.isTickModelView()) {
            VROverlayHelper.applyPose(
                    this,
                    optionsPose.getPositionAnchor(),
                    optionsPose.getRotationAnchor(),
                    scale,
                    optionsPose.isAimRotation(),
                    optionsPose.getPosOffset(),
                    optionsPose.getRotationOffsetVec()
            );
            initializedPose = true;
        }
    }

    @Override
    public void updateIdentity() {
        if(optionsIdentity == null){
            return; //happens on super constructor
        }

        name = optionsIdentity.getName();
        description = optionsIdentity.getDescription();
        icon = optionsIdentity.getIcon();

    }


    @Override
    public void setEnabled(boolean flag) {
        super.setEnabled(flag);
        if(!flag) {
            initializedPose = false;
        }
    }

}
