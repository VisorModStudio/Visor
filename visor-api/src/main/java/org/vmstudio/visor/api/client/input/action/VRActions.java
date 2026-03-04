package org.vmstudio.visor.api.client.input.action;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.common.HandType;
import java.util.function.Function;

public final class VRActions {

    public static VRAction createMouseLeft(@NotNull VRActionSet actionSet,
                                           @NotNull HandType handType) {
        if (handType == HandType.MAIN) {
            return Provider.mouseLeftMain.apply(actionSet);
        }else {
            return Provider.mouseLeftOffhand.apply(actionSet);
        }
    }
    public static VRAction createMouseRight(@NotNull VRActionSet actionSet,
                                            @NotNull HandType handType) {
        if (handType == HandType.MAIN) {
            return Provider.mouseRightMain.apply(actionSet);
        }else {
            return Provider.mouseRightOffhand.apply(actionSet);
        }
    }
    public static VRAction createMouseMiddle(@NotNull VRActionSet actionSet,
                                             @NotNull HandType handType) {
        if (handType == HandType.MAIN) {
            return Provider.mouseMiddleMain.apply(actionSet);
        }else {
            return Provider.mouseMiddleOffhand.apply(actionSet);
        }
    }
    public static VRAction createMouseScroll(@NotNull VRActionSet actionSet,
                                             @NotNull HandType handType) {
        if (handType == HandType.MAIN) {
            return Provider.mouseScrollMain.apply(actionSet);
        }else {
            return Provider.mouseScrollOffhand.apply(actionSet);
        }
    }

    public static VRAction createShift(@NotNull VRActionSet actionSet) {
        return Provider.shift.apply(actionSet);
    }

    public static VRAction createMenu(@NotNull VRActionSet actionSet) {
        return Provider.menu.apply(actionSet);
    }


    private VRActions() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    @ApiStatus.Internal
    public static final class Provider {
        private Provider() {
            throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
        }
        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseLeftMain;
        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseLeftOffhand;

        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseRightMain;
        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseRightOffhand;

        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseMiddleMain;
        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseMiddleOffhand;

        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseScrollMain;
        @Getter @Setter
        private static Function<VRActionSet, VRAction> mouseScrollOffhand;

        @Getter @Setter
        private static Function<VRActionSet, VRAction> shift;

        @Getter @Setter
        private static Function<VRActionSet, VRAction> menu;

    }
}
