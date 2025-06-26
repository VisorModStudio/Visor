package me.phoenixra.visor.core.client.gui.overlays.builtin.hotbar;


import lombok.Getter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumvr.api.utils.MathUtils;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.ClientFeature;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.events.AllowClientFeatureVREvent;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.gui.overlay.VROverlayHelper;
import me.phoenixra.visor.api.client.gui.overlay.framework.screen.VROverlayRadialSelector;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.api.common.eventbus.listener.VREventHandler;
import me.phoenixra.visor.api.common.eventbus.listener.VREventListener;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.helpers.RenderPoseHelper;
import me.phoenixra.visor.core.client.tasks.types.TaskHotBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


public class VROverlayHotBar extends VROverlayRadialSelector
        implements VREventListener {

    public static final String ID_MAIN = "hotbar-mainhand";
    public static final String ID_OFFHAND = "hotbar-offhand";


    private ResourceLocation hotbarSelectedMain0 = new ResourceLocation(
            "visor:textures/gui/hotbar/hotbar_main_selected0.png"
    );
    private ResourceLocation hotbarSelectedMain1 = new ResourceLocation(
            "visor:textures/gui/hotbar/hotbar_main_selected1.png"
    );

    private ResourceLocation hotbarSelectedOffhand0 = new ResourceLocation(
            "visor:textures/gui/hotbar/hotbar_offhand_selected0.png"
    );
    private ResourceLocation hotbarSelectedOffhand1 = new ResourceLocation(
            "visor:textures/gui/hotbar/hotbar_offhand_selected1.png"
    );
    private final Vector3f orientPosOffset = new Vector3f(0, 0, -0.6f);
    private final Vector3f orientRotationOffset = new Vector3f(0, 0, 0);


    private Vector3f orientPosOffsetRender;

    public VROverlayHotBar(@NotNull VisorAddon owner,
                           @NotNull ControllerHand hand,
                           @NotNull String id) {

        super(owner, hand, id,
                hand == ControllerHand.MAIN
                        ? ElementPriority.HIGH
                        :ElementPriority.NORMAL,
                98,
                new SelectionBoxHotBar(
                        HotBarSlice.CENTER.getSlot(),
                        41, 41,
                        new PairRecord<>(0d, 0d) //separately checked
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.TOP_LEFT.getSlot(),
                        5, 5,
                        new PairRecord<>(
                                (-7 * Math.PI) / 8,
                                (-5 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.TOP.getSlot(),
                        41, 5,
                        new PairRecord<>(
                                (-5 * Math.PI) / 8,
                                (-3 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.TOP_RIGHT.getSlot(),
                        77, 5,
                        new PairRecord<>(
                                (-3 * Math.PI) / 8,
                                (-1 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.RIGHT.getSlot(),
                        77, 41,
                        new PairRecord<>(
                                (-1 * Math.PI) / 8,
                                (1 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.BOTTOM_RIGHT.getSlot(),
                        77, 77,
                        new PairRecord<>(
                                (1 * Math.PI) / 8,
                                (3 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.BOTTOM.getSlot(),
                        41, 77,
                        new PairRecord<>(
                                (3 * Math.PI) / 8,
                                (5 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.BOTTOM_LEFT.getSlot(),
                        5, 77,
                        new PairRecord<>(
                                (5 * Math.PI) / 8,
                                (7 * Math.PI) / 8
                        )
                ),
                new SelectionBoxHotBar(
                        HotBarSlice.LEFT.getSlot(),
                        5, 41,
                        new PairRecord<>(
                                -1d, //separately checked
                                -1d  //separately checked
                        )
                ));
        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void disableAimEffectsAndMouse(AllowClientFeatureVREvent event){
        if(event.getFeature() == ClientFeature.AIM_EFFECTS
                || event.getFeature() == ClientFeature.INPUT_MOUSE
                || event.getFeature() == ClientFeature.GUI_CURSOR) {

            if(isVisible()
                    && ClientContext.player.getActiveHand() == getUsedHand()){
                event.setCanceled(true);
            }

        }
    }




    @Override
    public void onRender(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks) {


        VROverlayHotBar hotBarOffhand = (VROverlayHotBar) ClientContext.overlayManager
                .getOverlay(ID_OFFHAND);
        VROverlayHotBar hotBarMainHand = (VROverlayHotBar) ClientContext.overlayManager
                .getOverlay(ID_MAIN);

        //if selected slices are the same
        //change left to 1
        if (hotBarOffhand == this) {
            if (hotBarMainHand.isEnabled()
                    && hotBarMainHand.getSelectedSlice() == 0
                    && hotBarOffhand.getSelectedSlice() == 0) {
                hotBarOffhand.setSelectedSlice(1);
            }
        }
        //disabled box slices update
        if (hotBarMainHand == this) {
            hotBarOffhand.getDisabledBoxes().clear();
            if (hotBarMainHand.getSelectedSlice() != -1) {
                hotBarOffhand.getDisabledBoxes().add(
                        hotBarMainHand.getSelectedSlice()
                );
            }
            if (!hotBarOffhand.isEnabled()) {
                disabledBoxes.clear();
            }
        } else {
            hotBarMainHand.getDisabledBoxes().clear();
            if (hotBarOffhand.getSelectedSlice() != -1) {
                hotBarMainHand.getDisabledBoxes().add(
                        hotBarOffhand.getSelectedSlice()
                );
            }
            if (!hotBarMainHand.isEnabled()) {
                disabledBoxes.clear();
            }
        }
        super.onRender(guiGraphics, pMouseX, pMouseY, pPartialTicks);
    }

    @Override
    protected void renderRadialImage(GuiGraphics guiGraphics,
                                     float pPartialTicks,
                                     int selectedSlice,
                                     int x, int y, int size
    ) {

        //----Main image
        VROverlayHelper.renderImage(
                guiGraphics,
                HotBarSlice.fromSlot(selectedSlice).getImage(),
                x, y,
                size, size,
                size, size
        );


        //----Items
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack.isEmpty()) continue;
            SelectionBox selectionBox = selectionBoxes.get(slot);
            int itemX = ((SelectionBoxHotBar) selectionBox).getItemX();
            int itemY = ((SelectionBoxHotBar) selectionBox).getItemY();

            guiGraphics.pose().pushPose();
            guiGraphics.renderItem(
                    itemStack,
                    x + itemX,
                    y + itemY

            );
            guiGraphics.renderItemDecorations(
                    this.font,
                    itemStack,
                    x + itemX,
                    y + itemY,
                    null
            );
            guiGraphics.pose().popPose();
        }


        //----Highlighting for selected slots
        HotBarSlice slice = TaskHotBar.getCurrentStateMain();
        if (slice == HotBarSlice.NOT_SELECTED) return;
        SelectionBoxHotBar selectionBox = (SelectionBoxHotBar)selectionBoxes.get(slice.slot);
        int itemX = selectionBox.getItemX();
        int itemY = selectionBox.getItemY();

        VROverlayHelper.renderImage(
                guiGraphics,
                slice.slot != 0
                        ? hotbarSelectedMain0
                        : hotbarSelectedMain1,
                x + itemX - 5,
                y + itemY - 5,
                26, 26,
                26, 26

        );

        slice = TaskHotBar.getCurrentStateOffhand();
        if (slice == HotBarSlice.NOT_SELECTED) return;
        selectionBox = (SelectionBoxHotBar) selectionBoxes.get(slice.slot);
        itemX = selectionBox.getItemX();
        itemY = selectionBox.getItemY();

        VROverlayHelper.renderImage(
                guiGraphics,
                slice.slot != 0
                        ? hotbarSelectedOffhand0
                        : hotbarSelectedOffhand1,
                x + itemX - 5,
                y + itemY - 5,
                26, 26,
                26, 26

        );


    }

    @Override
    protected void onTick() {

    }

    @Override
    public boolean updateVisibility() {
        return true;
    }

    @Override
    public void updatePose(float partialTicks) {
        var camPos = RenderPoseHelper.getCameraPosition(
                VRDisplay.GUI,
                ClientContext.player.getPose(PoseDataType.RENDER)
        );

        getPose().updateOnlyPosition(new Vector3f(
                camPos.x() + orientPosOffsetRender.x,
                camPos.y() + orientPosOffsetRender.y,
                camPos.z() + orientPosOffsetRender.z
        ));
    }

    @Override
    public void onEnable() {
        PoseAnchor posAnchor = (getUsedHand() == ControllerHand.OFFHAND ?
                PoseAnchor.OFFHAND : PoseAnchor.MAIN_HAND);

        PoseData renderPose = ClientContext
                .player
                .getPose(PoseDataType.RENDER);

        VROverlayHelper.applyPose(
                this,
                posAnchor,
                PoseAnchor.HMD,
                getPose().getScale(),
                true,
                orientPosOffset,
                orientRotationOffset

        );
        orientPosOffsetRender = getPose().getPosition().sub(
                renderPose.getHmd().getPosition(),
                new Vector3f()
        );

        disabledBoxes.clear();
    }

    @Override
    public void onDisable() {
    }


    @Getter
    private static class SelectionBoxHotBar extends SelectionBox {
        //max and min angle bounds
        private final PairRecord<Double, Double> selectionAngle;


        private final int itemX;
        private final int itemY;

        public SelectionBoxHotBar(int id,
                                  int itemX, int itemY,
                                  @NotNull PairRecord<Double, Double> selectionAngle
        ) {
            super(id);
            this.selectionAngle = selectionAngle;
            this.itemX = itemX;
            this.itemY = itemY;
        }

        @Override
        public boolean isInBox(int x, int y) {
            if (getId() == HotBarSlice.LEFT.getSlot()) {
                double angle = MathUtils.fastAtan2(y, x);
                //handle boundary between positive
                // and negative angle
                return (angle <= Math.PI
                        && angle >= (7 * Math.PI) / 8)
                        ||
                        (angle >= -Math.PI
                                && angle <= (-7 * Math.PI) / 8);
            }
            if(getId() == HotBarSlice.CENTER.getSlot()){
                return Math.sqrt(x*x+y*y) <= 20;
            }

            double angle = MathUtils.fastAtan2(y, x);

            return angle >= selectionAngle.first()
                    && angle <= selectionAngle.second();
        }
    }

}
