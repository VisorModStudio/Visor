package me.phoenixra.visor.core.client.render.gameview.views;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.client.render.gameview.VRGameViewBase;
import me.phoenixra.visor.api.client.render.gameview.IVRGameViewHandler;
import me.phoenixra.visor.api.client.render.gameview.annotations.RegisterVRGameView;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import org.jetbrains.annotations.NotNull;

@RegisterVRGameView
public class VRGameViewEmpty extends VRGameViewBase {
    public VRGameViewEmpty(@NotNull VisorAddon owner) {
        super(owner, IVRGameViewHandler.VIEW_EMPTY);
        ClientContext.gameViewHandler.initView(this);
    }

    @Override
    public void onEnter() {

    }

    @Override
    public void onExit() {

    }

    @Override
    public void tick() {

    }

    @Override
    public void render(PoseStack poseStack, float partialTicks) {

    }
}
