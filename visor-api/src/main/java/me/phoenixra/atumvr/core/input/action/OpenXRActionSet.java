package me.phoenixra.atumvr.core.input.action;

import lombok.Getter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumvr.api.input.action.VRActionSet;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.init.OpenXRInstance;
import me.phoenixra.atumvr.core.input.action.types.HapticPulseAction;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openxr.XR10;
import org.lwjgl.openxr.XrActionSet;
import org.lwjgl.openxr.XrActionSetCreateInfo;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackCallocPointer;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memUTF8;

public abstract class OpenXRActionSet implements VRActionSet {

    private final OpenXRProvider provider;
    @Getter
    private final String name;
    @Getter
    private final String localizedName;

    @Getter
    private XrActionSet handle;
    @Getter
    private final int priority;

    @Getter
    private List<OpenXRAction> actions = new ArrayList<>();

    public OpenXRActionSet(OpenXRProvider provider,
                           String name,
                           String localizedName,
                           int priority) {
        this.provider = provider;
        this.name = name;
        this.localizedName = localizedName;
        this.priority = priority;
    }

    protected abstract List<OpenXRAction> loadActions(OpenXRProvider provider);


    public void update() {
        for (var action : getActions()) {
            action.update();
        }
    }

    @Override
    public void init() {
        try (MemoryStack stack = stackPush()) {
            OpenXRInstance xrInstance = provider.getState().getVrInstance();
            XrActionSetCreateInfo actionSetCreateInfo = XrActionSetCreateInfo
                    .calloc(stack)
                    .set(
                            XR10.XR_TYPE_ACTION_SET_CREATE_INFO,
                            NULL,
                            memUTF8(this.name),
                            memUTF8(this.localizedName),
                            priority
                    );
            PointerBuffer actionSetPoint = stackCallocPointer(1);
            provider.checkXRError(
                    XR10.xrCreateActionSet(
                            xrInstance.getHandle(),
                            actionSetCreateInfo,
                            actionSetPoint
                    ),
                    "xrCreateActionSet"
            );
            handle = new XrActionSet(actionSetPoint.get(0), xrInstance.getHandle());

            actions = loadActions(provider);
            for (var action : actions) {
                action.init(this);
            }

        }
    }

    public List<PairRecord<OpenXRAction, String>> getDefaultBindings(XRInteractionProfile profile){
        List<PairRecord<OpenXRAction, String>> out = new ArrayList<>();
        for(OpenXRAction action : actions){
            if(action instanceof OpenXRSingleAction<?> singleAction){
                var bind = singleAction.getDefaultBindings(profile);
                if(bind == null) {
                    continue;
                }
                out.add(new PairRecord<>(
                        singleAction,
                        bind
                ));
            }else if(action instanceof OpenXRMultiAction<?> multiAction){
                for(OpenXRMultiAction.SubAction<?> subAction : multiAction.getSubActions()){
                    var bind = subAction.getDefaultBindings(profile);
                    if(bind == null) {
                        continue;
                    }
                    out.add(new PairRecord<>(
                            multiAction,
                            bind
                    ));
                }
            }else if(action instanceof HapticPulseAction hapticPulseAction){
                var bind = hapticPulseAction.getDefaultBindings(profile);
                if(bind == null) {
                    continue;
                }
                out.add(new PairRecord<>(
                        action,
                        bind.first()
                ));
                out.add(new PairRecord<>(
                        action,
                        bind.second()
                ));
            }
        }
        return out;
    }

    public final void destroy() {
        if (handle != null) {
            XR10.xrDestroyActionSet(handle);
        }
        for (var action : getActions()) {
            action.destroy();
        }
    }

}
