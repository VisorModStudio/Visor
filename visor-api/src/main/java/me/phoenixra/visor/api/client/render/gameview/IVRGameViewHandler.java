package me.phoenixra.visor.api.client.render.gameview;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.api.common.addon.VRElementRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface IVRGameViewHandler {
    String VIEW_EMPTY = "empty";

    String VIEW_MAIN_MENU = "main_menu";
    String VIEW_INGAME = "ingame";
    String VIEW_INGAME_SCREEN = "ingame_screen";
    String VIEW_END_TITLE = "end_title";

    @NotNull VRElementRegistry<VRGameView> getRegistry();


    @NotNull VRGameView getCurrentView();

    @Nullable VRGameView getView(@NotNull String id);
    default @NotNull VRGameView getViewOrEmpty(@NotNull String id){
        return Objects.requireNonNullElse(
                getView(id),
                getEmptyView()
        );
    }
    @NotNull VRGameView getEmptyView();


    void renderView(PoseStack poseStack, float partialTicks);
    void tick();
}
