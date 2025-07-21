package me.phoenixra.visor.core.client.gui.overlays.builtin;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayScreenInScreen;
import me.phoenixra.visor.api.client.gui.overlay.template.framework.VROverlayTemplateScreenInScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VROverlayDraggedItem extends VROverlayScreen
        implements VREventListener {
    public static final String ID = "dragged_item";

    private Vector3f orientPosOffset = new Vector3f(0,0,-0.6f);
    private Vector3f orientRotationOffset = new Vector3f(0,0,0);

    public VROverlayDraggedItem(@NotNull VisorAddon owner,
                                @NotNull String id) {
        super(owner, id, ElementPriority.HIGHER, 0.1f);
        setEnabled(true);
        cursorEdgeX = width/2 - 8;
        cursorEdgeY = height/2 - 8;
        cursorEdgeWidth = 16;
        cursorEdgeHeight = 16;
        VisorAPI.eventBus().registerListener(owner,this);
    }


    @VREventHandler
    public void disableWorldHands(AllowClientFeatureVREvent event){
        var featureToDisable = ClientContext.cursorHandler.getCursorHand() == ControllerHand.MAIN
                ? ClientFeature.VR_WORLD_HAND_MAIN
                : ClientFeature.VR_WORLD_HAND_OFFHAND;
        if(event.getFeature() == featureToDisable) {
            if(isDraggingItem()){
                //To fix flickering on changing focus
                // from container/inventory to this overlay
                event.setCanceled(true);
            }
        }
    }


    @Override
    protected void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        var keyboard = ClientContext.overlayManager.getKeyboardAccessor();
        if(keyboard.isVisible()){
            keyboard.setVisible(false);
        }

        renderFloatingItem(
                guiGraphics,
                minecraft.player.containerMenu.getCarried(),
                width/2 - 8,height/2 - 8,
                null
        );
    }

    private void renderFloatingItem(GuiGraphics guiGraphics,
                                    ItemStack itemStack,
                                    int posX, int posY,
                                    String string) {
        guiGraphics.pose().pushPose();
        guiGraphics.renderItem(itemStack, posX, posY);
        guiGraphics.renderItemDecorations(
                this.font,
                itemStack,
                posX, posY, string
        );
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean updateVisibility() {
        if(!isDraggingItem()) {
            return false;
        }
        var cursorHandler = ClientContext.cursorHandler;
        if(supportsDragging(cursorHandler.getFocusedOverlay())){
            return false;
        }

        if(!ClientContext.rawPoseHandler.getControllerData(
                cursorHandler.getCursorHand()).isTracking()){
            return false;
        }

        if(isVisible()){
            var cursorResult  = cursorHandler.getCursorResult(
                    cursorHandler.getCursorHand(),
                    ClientContext.player.getPoseData(PoseDataType.RENDER),
                    it->it != this,
                    false
            );
            if(supportsDragging(cursorResult.focusedOverlay())){
                return false;
            }

        }
        return true;
    }


    @Override
    public void updatePose(float partialTicks) {
        PoseAnchor anchor =  ClientContext.cursorHandler
                .getCursorHand() == ControllerHand.MAIN
                ? PoseAnchor.MAIN_HAND
                : PoseAnchor.OFFHAND;
        VROverlayHelper.applyPose(
                this,
                anchor,
                anchor,
                1.0f,
                true,
                orientPosOffset,
                orientRotationOffset
        );
    }


    @Override
    public boolean mouseClicked(double d, double e, int i) {
        this.minecraft.gameMode.handleInventoryMouseClick(
                minecraft.player.containerMenu.containerId,
                -999, i, ClickType.PICKUP, this.minecraft.player
        );
        return true;
    }
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    private boolean supportsDragging(VROverlay overlay){
        if(overlay instanceof VROverlayGameScreen){
            if(MC.screen instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        if(overlay instanceof VROverlayScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        if(overlay instanceof VROverlayTemplateScreenInScreen<?> screenInScreen){
            if(screenInScreen.getScreen() instanceof AbstractContainerScreen<?>){
                return true;
            }
        }
        return false;
    }
    public static boolean isDraggingItem(){
        return MC.level != null
                && MC.player.containerMenu != null
                && MC.player.containerMenu.getCarried() != null
                && !MC.player.containerMenu.getCarried().isEmpty();
    }
}
