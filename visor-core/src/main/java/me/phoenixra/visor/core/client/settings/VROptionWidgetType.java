package me.phoenixra.visor.core.client.settings;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.options.OptionBehaviour;
import me.phoenixra.visor.core.client.settings.options.OptionBehaviourFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import me.phoenixra.visor.core.client.ClientContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum VROptionWidgetType {

    EMPTY(
            VROptionCategory.EMPTY,
            (it) -> null
    ),

    LEFT_HANDED(
            VROptionCategory.CONTROLS,
            (it) -> null
    ),
    THIRD_PERSON_FOV(
            VROptionCategory.RENDERING_THIRD_PERSON,
            (it) -> {
                List<Float> entries = new ArrayList<>();
                for (int i = 0; i <= 150; i++) {
                    entries.add((float) i);
                }
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf(VRClientSettings.getThirdPersonFov());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            return pair.first() + String.format("%.0f" + "°"/*degrees*/, (float) pair.second());
                        }
                ).build();
            }),
    MIXED_REALITY_FOV(
            VROptionCategory.RENDERING_MIXED_REALITY,
            (it) -> {
                List<Integer> entries = new ArrayList<>();
                for (int i = 0; i <= 150; i++) {
                    entries.add(i);
                }
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf((int) VRClientSettings.getMixedRealityFov());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            return pair.first() + String.format("%.0f" + "°"/*degrees*/, (float) pair.second());
                        }
                ).build();
            }),
    MIXED_REALITY_ALPHA_MASK(
            VROptionCategory.RENDERING_MIXED_REALITY,
            (it) -> null
    ),
    MIXED_REALITY_WITH_FIRST_PERSON(
            VROptionCategory.RENDERING_MIXED_REALITY,
            (it) -> null
    ),
    MIXED_REALITY_AS_GRID_2_X_2(
            VROptionCategory.RENDERING_MIXED_REALITY,
            (it) -> null
    ),
    MIXED_REALITY_RENDER_HANDS(
            VROptionCategory.RENDERING_MIXED_REALITY,
            (it) -> null
    ),
    HUD_DISABLED_HOTBAR(
            VROptionCategory.GUI,
            (it) -> null
    ),
    SHADER_GUI_RENDER(
            VROptionCategory.GUI,
            (it) -> null
    ),

    LOW_HEALTH_INDICATOR(
            VROptionCategory.RENDERING_EYE_EFFECTS,
            (it) -> null
    ),
    HIT_INDICATOR(
            VROptionCategory.RENDERING_EYE_EFFECTS,
            (it) -> null
    ),
    FREEZE_EFFECT(
            VROptionCategory.RENDERING_EYE_EFFECTS,
            (it) -> null
    ),
    PUMPKIN_EFFECT(
            VROptionCategory.RENDERING_EYE_EFFECTS,
            (it) -> null
    ),
    MIRROR_MODE(
            VROptionCategory.RENDERING,
            (it) ->
                    OptionBehaviourFactory
                            .simple(it)
                            .setOnChanged(() -> {
                                if (VisorState.getState().isActive()
                                        && !ShadersHelper.isShaderActive()) {
                                    ClientContext.renderer.prepareReinit(
                                            "Mirror Setting Changed"
                                    );
                                }
                            }).build()
    ),
    MIRROR_EYE(
            VROptionCategory.RENDERING,
            (it) -> null
    ),
    MOVEMENT_MODE(
            VROptionCategory.MOVEMENT,
            (it) -> null
    ),
    ROTATION_MODE(
            VROptionCategory.MOVEMENT,
            (it) -> null
    ),
    ROTATION_FLY_MODE(
            VROptionCategory.MOVEMENT,
            (it) -> null
    ),
    WALK_UP(
            VROptionCategory.MOVEMENT,
            (it) -> null
    ),
    WORLD_ROTATION_INCREMENT(
            VROptionCategory.MOVEMENT,
            (it) -> {
        List<Float> entries = List.of(0f, 10f, 30f, 45f, 90f);
        return OptionBehaviourFactory.discreteSlider(
                it, entries,
                () -> {
                    int initialIndex = entries.indexOf(VRClientSettings.getWorldRotationIncrement());
                    return initialIndex != -1
                            ? initialIndex
                            : entries.size() / 2;
                }
        ).setOnUpdateName(
                (pair) -> {
                    String value;
                    if((float)pair.second() == 0){
                        value = Component.translatable("visor.options.movement.world_rotation.smooth").getString();
                    }else{
                        value = String.format("%.0f" + "°"/*degrees*/, (float) pair.second());
                    }
                    return pair.first() + value;
                }
        ).setOnChanged(
                () -> ClientContext.localPlayer.setRotationY(0)
        ).build();
    });

    @Getter
    @Setter
    private String key;

    @Getter
    private final VROptionCategory category;

    @Getter
    private final OptionBehaviour behaviour;


    VROptionWidgetType(@NotNull VROptionCategory category,
                       @NotNull Function<VROptionWidgetType, OptionBehaviour> behaviourProvider) {
        this.category = category;
        this.behaviour = Objects.requireNonNullElse(
                behaviourProvider.apply(this),
                OptionBehaviourFactory.simple(this).build()
        );
    }


    @Nullable
    public <T extends OptionBehaviour> T getBehaviourAs(@NotNull Class<T> type) {
        if (type.isInstance(behaviour)) {
            return type.cast(behaviour);
        }
        return null;
    }


}
