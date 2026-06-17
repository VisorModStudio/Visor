package org.vmstudio.visor.core.client.settings;

import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.compatibility.ShadersHelper;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.settings.options.OptionBehaviour;
import org.vmstudio.visor.core.client.settings.options.OptionBehaviourFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.vmstudio.visor.core.client.ClientContext;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

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
    SETTINGS_TEXT_SCALE(
            VROptionCategory.GUI,
            (it) -> {
                List<Float> entries = List.of(0.5f, 0.55f, 0.6f, 0.65f, 0.7f, 0.75f, 0.8f, 0.85f, 0.9f, 0.95f, 1.0f, 1.1f);
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf(VRClientSettings.getSettingsTextScale());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            String value = String.format("%.2f", (float) pair.second());
                            return pair.first() + value;
                        }
                ).setOnChanged(
                        ()->{
                            //reinit screen
                            MC.setScreen(MC.screen);
                        }
                ).build();
            }
    ),
    HUD_DISABLED_HOTBAR(
            VROptionCategory.GUI,
            (it) -> null
    ),
    SHADER_GUI_RENDER(
            VROptionCategory.GUI,
            (it) -> null
    ),
    KEYBOARD_AUTO_LAYOUT(
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
    SHADER_PER_EYE_PIPELINES(
            VROptionCategory.RENDERING_SHADERS,
            (it) ->
                    OptionBehaviourFactory
                            .simple(it)
                            .setOnChanged(
                                    IrisCompatHelper::requestPipelineReload
                            ).build()
    ),
    SHADER_SHARED_SHADOWS(
            VROptionCategory.RENDERING_SHADERS,
            (it) ->
                    OptionBehaviourFactory
                            .simple(it)
                            .setOnChanged(
                                    IrisCompatHelper::requestPipelineReload
                            ).build()
    ),
    SHADER_SHARED_SSBO(
            VROptionCategory.RENDERING_SHADERS,
            (it) ->
                    OptionBehaviourFactory
                            .simple(it)
                            .setOnChanged(
                                    IrisCompatHelper::requestPipelineReload
                            ).build()
    ),
    MIRROR_MODE(
            VROptionCategory.RENDERING,
            (it) ->
                    OptionBehaviourFactory
                            .simple(it)
                            .setOnChanged(() -> {
                                if (VisorState.get().isActive()
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
    DH_MIRROR_PASSES(
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
    COMPATIBLE_LOOK_DIRECTION(
            VROptionCategory.MOVEMENT,
            (it) -> null
    ),
    WORLD_ROTATION_STEP(
            VROptionCategory.MOVEMENT,
            (it) -> {
        List<Float> entries = List.of(0f, 10f, 30f, 45f, 90f);
        return OptionBehaviourFactory.discreteSlider(
                it, entries,
                () -> {
                    int initialIndex = entries.indexOf(VRClientSettings.getWorldRotationStep());
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
    }),
    WORLD_ROTATION_SMOOTH(
            VROptionCategory.MOVEMENT,
            (it) -> {
                List<Float> entries = List.of(0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.09f, 0.1f, 0.11f, 0.12f, 0.13f, 0.14f, 0.15f);
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf(VRClientSettings.getWorldRotationSmoothSensitivity());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            String value = String.format("%.2f", (float) pair.second());
                            return pair.first() + value;
                        }
                ).build();
            }),
    ROOM_SNEAK(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_CRAWL(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_CLIMB(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_JUMP(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_SWIM(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_DISMOUNT_VEHICLE(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_CONSUME(
            VROptionCategory.IMMERSION,
            (it) -> null
    ),
    ROOM_SNEAK_THRESHOLD(
            VROptionCategory.IMMERSION_ADVANCED,
            (it) -> {
                List<Float> entries = List.of(0.77f, 0.78f, 0.79f, 0.8f, 0.81f, 0.82f, 0.83f, 0.84f, 0.85f, 0.86f, 0.87f, 0.88f);
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf(VRClientSettings.getRoomSneakThreshold());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            String value = String.format("%.0f%%", (float) pair.second() * 100);
                            return pair.first() + value;
                        }
                ).build();
            }
    ),
    ROOM_CRAWL_THRESHOLD(
            VROptionCategory.IMMERSION_ADVANCED,
            (it) -> {
                List<Float> entries = List.of(0.6f, 0.61f, 0.62f, 0.63f, 0.64f, 0.65f, 0.66f, 0.67f, 0.68f, 0.69f, 0.7f, 0.71f, 0.72f, 0.73f);
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf(VRClientSettings.getRoomCrawlThreshold());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            String value = String.format("%.0f%%", (float) pair.second() * 100);
                            return pair.first() + value;
                        }
                ).build();
            }
    ),
    ROOM_JUMP_THRESHOLD(
            VROptionCategory.IMMERSION_ADVANCED,
            (it) -> {
                List<Float> entries = List.of(1.01f,1.02f,1.03f, 1.04f, 1.05f, 1.06f, 1.07f, 1.08f, 1.09f, 1.1f, 1.11f, 1.12f, 1.13f, 1.14f, 1.15f);
                return OptionBehaviourFactory.discreteSlider(
                        it, entries,
                        () -> {
                            int initialIndex = entries.indexOf(VRClientSettings.getRoomJumpThreshold());
                            return initialIndex != -1
                                    ? initialIndex
                                    : entries.size() / 2;
                        }
                ).setOnUpdateName(
                        (pair) -> {
                            String value = String.format("%.0f%%", (float) pair.second() * 100);
                            return pair.first() + value;
                        }
                ).build();
            }
    );

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
