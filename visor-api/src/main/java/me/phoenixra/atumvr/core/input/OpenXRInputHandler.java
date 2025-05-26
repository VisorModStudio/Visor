package me.phoenixra.atumvr.core.input;

import lombok.Getter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumvr.api.exceptions.VRException;
import me.phoenixra.atumvr.api.input.VRInputHandler;
import me.phoenixra.atumvr.api.input.device.VRDevice;
import me.phoenixra.atumvr.core.OpenXRProvider;
import me.phoenixra.atumvr.core.enums.XRInteractionProfile;
import me.phoenixra.atumvr.core.input.action.OpenXRAction;
import me.phoenixra.atumvr.core.input.action.OpenXRActionSet;
import me.phoenixra.atumvr.core.input.device.OpenXRDevice;
import org.lwjgl.openxr.*;
import org.lwjgl.system.MemoryStack;

import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.openxr.XR10.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.NULL;


public abstract class OpenXRInputHandler implements VRInputHandler {
    @Getter
    private final OpenXRProvider vrProvider;


    private final HashMap<String, Long> paths = new HashMap<>();

    private final Map<String, OpenXRActionSet> actionSets = new LinkedHashMap<>();
    private final Map<String, OpenXRDevice> devices = new LinkedHashMap<>();



    public OpenXRInputHandler(OpenXRProvider provider){
        this.vrProvider = provider;
    }

    protected abstract List<? extends OpenXRActionSet> generateActionSets(MemoryStack stack);
    protected abstract List<? extends OpenXRDevice> generateDevices(MemoryStack stack);


    @Override
    public void init() {

        XrSession xrSession = vrProvider.getState().getVrSession().getHandle();


        try (MemoryStack stack = MemoryStack.stackPush()) {


            //LOAD ACTION SETS
            actionSets.clear();
            var loadedActionSets = generateActionSets(stack);
            loadedActionSets.forEach(OpenXRActionSet::init);

            long[] actionSetsArray = new long[loadedActionSets.size()];
            int i = 0;
            for(OpenXRActionSet entry : loadedActionSets){
                actionSets.put(entry.getName(), entry);
                actionSetsArray[i] = entry.getHandle().address();
                i++;
            }

            //suggest defaults before attaching action sets
            suggestDefaultBindings(stack);

            //attach action sets
            XrSessionActionSetsAttachInfo attach_info = XrSessionActionSetsAttachInfo.calloc(stack).set(
                    XR10.XR_TYPE_SESSION_ACTION_SETS_ATTACH_INFO,
                    NULL,
                    stackPointers(actionSetsArray)
            );
            vrProvider.checkXRError(
                    XR10.xrAttachSessionActionSets(xrSession, attach_info),
                    "xrAttachSessionActionSets"
            );


            //LOAD DEVICES
            devices.clear();
            for(OpenXRDevice entry : generateDevices(stack)){
                devices.put(entry.getId(), entry);
            }

        }
    }



    @Override
    public void update() {
        XrInstance instance = vrProvider.getState().getVrInstance().getHandle();
        XrSession session = vrProvider.getState().getVrSession().getHandle();

        // Sync actions
        try (MemoryStack stack = MemoryStack.stackPush()) {

            XrActiveActionSet.Buffer toUpdate = XrActiveActionSet
                    .calloc(actionSets.size(), stack);
            int i = 0;
            for(OpenXRActionSet actionSet : actionSets.values()) {
                toUpdate.get(i).set(actionSet.getHandle(), XR_NULL_PATH);
                i++;
            }

            XrActionsSyncInfo syncInfo = XrActionsSyncInfo
                    .calloc(stack)
                    .type(XR_TYPE_ACTIONS_SYNC_INFO)
                    .activeActionSets(toUpdate);
            vrProvider.checkXRError(
                    xrSyncActions(session, syncInfo),
                    "xrSyncActions"
            );


        }

        for (OpenXRActionSet entry : actionSets.values()) {
            entry.update();
        }
        for (OpenXRDevice entry : devices.values()) {
            entry.update();
        }

    }


    private void suggestDefaultBindings(MemoryStack stack) {

        XrInstance xrInstance = vrProvider.getState().getVrInstance().getHandle();
        List<XRInteractionProfile> supportedProfiles = XRInteractionProfile.getSupported(vrProvider);

        for (XRInteractionProfile profile : supportedProfiles) {
            List<PairRecord<OpenXRAction, String>> bindingsSet = new ArrayList<>();
            for(OpenXRActionSet actionSet : actionSets.values()){
                var binds = actionSet.getDefaultBindings(profile);
                if(binds == null || binds.isEmpty()) continue;
                bindingsSet.addAll(binds);
            }
            if(bindingsSet.isEmpty()) continue;

            var bindings = XrActionSuggestedBinding.calloc(bindingsSet.size(), stack);

            for (int i = 0; i < bindingsSet.size(); i++) {
                var binding = bindingsSet.get(i);
                bindings.get(i).set(
                        binding.first().getHandle(),
                        getPath(binding.second())
                );
            }

            var suggested_binds = XrInteractionProfileSuggestedBinding.calloc(stack)
                    .set(
                            XR10.XR_TYPE_INTERACTION_PROFILE_SUGGESTED_BINDING,
                            NULL,
                            getPath(profile.getPathName()),
                            bindings
                    );

            vrProvider.checkXRError(
                    XR10.xrSuggestInteractionProfileBindings(
                            xrInstance,
                            suggested_binds
                    ),
                    "xrSuggestInteractionProfileBindings"
            );
        }
    };

    public Collection<? extends OpenXRActionSet> getActionSets(){
        return actionSets.values();
    }

    @Override
    public Collection<? extends OpenXRDevice> getDevices() {
        return devices.values();
    }

    @Override
    public OpenXRDevice getDevice(String id) {
        return devices.get(id);
    }

    @Override
    public void registerDevice(VRDevice device) {
        if(!(device instanceof OpenXRDevice openXRDevice)){
            throw new VRException("Cannot register device that is not instanceof OpenXRDevice");
        }
        devices.put(device.getId(),openXRDevice);
    }


    public long getPath(String pathString) {
        return paths.computeIfAbsent(pathString, s -> {
            try (MemoryStack ignored = stackPush()) {
                LongBuffer buf = stackCallocLong(1);
                int xrResult = XR10.xrStringToPath(
                        vrProvider.getState().getVrInstance().getHandle(),
                        pathString, buf
                );
                if (xrResult == XR10.XR_ERROR_PATH_FORMAT_INVALID) {
                    throw new VRException("Invalid path:\"" + pathString + "\"");
                } else {
                    vrProvider.checkXRError(xrResult, "xrStringToPath");
                }
                return buf.get();
            }
        });
    }

    @Override
    public void destroy() {
        actionSets.values().forEach(OpenXRActionSet::destroy);
        actionSets.clear();
        devices.clear();
    }
}
