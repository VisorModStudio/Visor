package org.vmstudio.visor.core.client.gui.overlays.builtin.hotbar;


import lombok.Getter;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.atumvr.api.utils.MathUtils;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.events.AllowClientFeatureVREvent;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.VROverlayHelper;
import org.vmstudio.visor.api.client.gui.overlays.framework.screen.VROverlayRadialSelector;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;


public class VROverlayHotBar extends VROverlayRadialSelector
        implements VREventListener {

    public static final String ID_MAIN = "hotbar_mainhand";
    public static final String ID_OFFHAND = "hotbar_offhand";


    private GuiTexture hotbarSelectedMain0Tex = new GuiTexture(
            new ResourceLocation(
                    VisorAPI.MOD_ID,"textures/gui/overlays/hotbar/hotbar_main_selected0.png"
            )
    );
    private GuiTexture hotbarSelectedMain1Tex = new GuiTexture(
            new ResourceLocation(
                    VisorAPI.MOD_ID,"textures/gui/overlays/hotbar/hotbar_main_selected1.png"
            )
    );

    private GuiTexture hotbarSelectedOffhand0Tex = new GuiTexture(
            new ResourceLocation(
                    VisorAPI.MOD_ID,"textures/gui/overlays/hotbar/hotbar_offhand_selected0.png"
            )
    );
    private GuiTexture hotbarSelectedOffhand1Tex = new GuiTexture(
            new ResourceLocation(
                    VisorAPI.MOD_ID,"textures/gui/overlays/hotbar/hotbar_offhand_selected1.png"
            )
    );

    private final Vector3f orientPosOffset = new Vector3f(0, 0, -0.6f);
    private final Vector3f orientRotationOffset = new Vector3f(0, 0, 0);


    private Vector3f orientPosOffsetRender;

    public VROverlayHotBar(@NotNull VisorAddon owner,
                           @NotNull HandType hand,
                           @NotNull String id) {

        super(owner, hand, id,
                hand == HandType.MAIN
                        ? ComponentPriority.HIGH
                        : ComponentPriority.NORMAL,
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
                    && ClientContext.localPlayer.getActiveHand() == getUsedHand()){
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
        HotBarSlice.fromSlot(selectedSlice)
                .getBackground()
                .blit(
                        guiGraphics,
                        x, y,
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

        if(slice.slot != 0) {
            hotbarSelectedMain0Tex.blit(
                    guiGraphics,
                    x + itemX - 5,
                    y + itemY - 5,
                    26, 26
            );
        }else{
            hotbarSelectedMain1Tex.blit(
                    guiGraphics,
                    x + itemX - 5,
                    y + itemY - 5,
                    26, 26
            );
        }

        slice = TaskHotBar.getCurrentStateOffhand();
        if (slice == HotBarSlice.NOT_SELECTED) return;
        selectionBox = (SelectionBoxHotBar) selectionBoxes.get(slice.slot);
        itemX = selectionBox.getItemX();
        itemY = selectionBox.getItemY();

        if(slice.slot != 0) {
            hotbarSelectedOffhand0Tex.blit(
                    guiGraphics,
                    x + itemX - 5,
                    y + itemY - 5,
                    26, 26
            );
        }else{
            hotbarSelectedOffhand1Tex.blit(
                    guiGraphics,
                    x + itemX - 5,
                    y + itemY - 5,
                    26, 26
            );
        }


    }

    @Override
    protected void onTick() {

    }

    @Override
    public boolean updateVisibility() {
        return true;
    }

    @Override
    public void onUpdatePose(float partialTicks) {
        var camPos = RenderPoseHelper.getCameraPosition(
                VRRenderPass.GUI,
                ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
        );

        getPose().updateOnlyPosition(new Vector3f(
                camPos.x() + orientPosOffsetRender.x,
                camPos.y() + orientPosOffsetRender.y,
                camPos.z() + orientPosOffsetRender.z
        ));
    }

    @Override
    public void onEnable() {
        PoseAnchor posAnchor = (getUsedHand() == HandType.OFFHAND ?
                PoseAnchor.OFFHAND : PoseAnchor.MAIN_HAND);

        VRPlayerPoseClient renderPose = ClientContext
                .localPlayer
                .getPoseData(PlayerPoseType.RENDER);

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



    @Override
    public @NotNull Component getName() {
        return Component.translatable("visor.overlay.%s.name".formatted(getId()));
    }

    @Override
    public @NotNull Component getDescription() {
        return Component.translatable("visor.overlay.%s.description".formatted(getId()));
    }
}
