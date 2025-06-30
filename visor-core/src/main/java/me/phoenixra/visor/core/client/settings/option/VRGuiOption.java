package me.phoenixra.visor.core.client.settings.option;

import lombok.Getter;
import me.phoenixra.visor.compatibility.ShadersHelper;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.lang.LangHandler;
import net.minecraft.client.resources.language.I18n;

import java.util.Arrays;
import java.util.List;

import me.phoenixra.visor.core.client.ClientContext;

//@TODO Finish, add gui screens
public enum VRGuiOption {

    NONE,
    LEFT_HANDED(){
        @Override
        public void onChanged() {

        }
    },
    THIRD_PERSON_FOV(0,150,1){
        @Override
        public String getDisplayString(String prefix, Object value) {
            return prefix + String.format("%.0f" + "\u00b0", (float) value);
        }
    },
    THIRD_PERSON_CAMERA_POS_X(true),
    THIRD_PERSON_CAMERA_POS_Y(true),
    THIRD_PERSON_CAMERA_POS_Z(true),
    THIRD_PERSON_CAMERA_ROTATION_X(true),
    THIRD_PERSON_CAMERA_ROTATION_Y(true),
    THIRD_PERSON_CAMERA_ROTATION_Z(true),
    MIXED_REALITY_FOV(0,150,1){
        @Override
        public String getDisplayString(String prefix, Object value) {
            return prefix + String.format("%.0f" + "\u00b0", (float) value);
        }
    },
    MIXED_REALITY_ALPHA_MASK,
    MIXED_REALITY_WITH_FIRST_PERSON,
    MIXED_REALITY_AS_GRID_2_X_2,
    MIXED_REALITY_RENDER_HANDS,
    HUD_DISABLED_HOTBAR,
    SHADER_GUI_RENDER,
    GUI_SCALE(0, 6, 1){
        @Override
        public String getDisplayString(String prefix, Object value) {
            int val = (int)((float)value);
            if (val == 0) {
                return prefix + LangHandler.getText("options.guiScale.auto");
            }
            return prefix + (int) Math.ceil(val * 0.5f);
        }
        @Override
        public void onChanged() {
            if (VisorState.getState().isActive()) {
                ClientContext.renderer.prepareResize("");
            }
        }
    },

    LOW_HEALTH_INDICATOR,
    HIT_INDICATOR,
    FREEZE_EFFECT,
    PUMPKIN_EFFECT,
    MIRROR_DISPLAY(){
        @Override
        public void onChanged() {
            if (VisorState.getState().isActive()
                    && !ShadersHelper.isShaderActive()) {
                ClientContext.renderer.prepareReinit(
                        "Mirror Setting Changed"
                );
            }
        }
    },
    MIRROR_EYE,

    EYE_FOV_SCALE(0.2f, 2.0f,0.1f){
        @Override
        public String getDisplayString(String prefix, Object value) {
            if ((float) value == 0) {
                return prefix + I18n.get("options.off");
            } else {
                return prefix + String.format("%.1f", (float) value) + "x";
            }
        }
    },
    WORLD_SCALE(0, 22, 1){
        private final List<Float> values = Arrays.asList(
                0.1f, 0.25f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f,
                1f, 1.1f, 1.2f, 1.3f, 1.4f, 1.5f, 1.6f,
                1.7f, 1.8f, 1.9f, 2f,
                3f, 4f, 5f, 6f,7f,8f,9f,10f
        );
        private int defaultIndex = 7;
        @Override
        public Float getSliderValue(float value) {
            for(int i = 0; i < values.size(); i++){
                if(value == values.get(i)){
                    return (float) i;
                }
            }
            return (float)defaultIndex;
        }

        @Override
        public Object setValue(Object newValue) {
            float value = (float) newValue;
            try{
                return values.get((int) value);
            }catch (Exception e){
                return values.get(defaultIndex);
            }
        }
        @Override
        public void onChanged() {
            //issue with going up when holding slider button

        }

        @Override
        public String getDisplayString(String prefix, Object value) {
            return prefix + String.format("%.2f", (float) value) + "x";
        }
    },
    WORLD_ROTATION_INCREMENT(-1, 4, 1){
        private final List<Float> values = Arrays.asList(
                10f,36f,45f,90f
        );
        private int defaultIndex = 2;
        @Override
        public Float getSliderValue(float value) {
            for(int i = 0; i < values.size(); i++){
                if(value == values.get(i)){
                    return (float) i;
                }
            }
            return (float)defaultIndex;
        }

        @Override
        public Object setValue(Object newValue) {
            float value = (float) newValue;
            try{
                return values.get((int) value);
            }catch (Exception e){
                return values.get(defaultIndex);
            }
        }
        @Override
        public void onChanged() {
            ClientContext.player.setRotationY(0);
        }

        @Override
        public String getDisplayString(String prefix, Object value) {
            if ((float) value == 0) {
                return prefix + LangHandler.getText("visor.option.smooth");
            }
            return prefix + String.format("%.0f" +"\u00b0", (float) value);
        }
    },
    ROOM_MOVEMENT_MULTIPLIER(1f, 10f, 0.1f),
    ROTATION_MODE;


    @Getter
    private final boolean specialSlider;

    //if less than 0, considered as percentage value
    @Getter
    private final boolean showAsPercentage;
    @Getter
    private final boolean sliderUsed;

    @Getter
    private final float sliderStep;
    @Getter
    private final float sliderValueMin;
    @Getter
    private final float sliderValueMax;

    VRGuiOption(boolean specialSlider){
        this(
                false,false, specialSlider,
                0,1,0
        );

    }
    VRGuiOption(boolean showAsPercentage,
                boolean sliderUsed,
                boolean specialSlider,
                float sliderStep,
                float sliderValueMin,
                float sliderValueMax
    ) {
        this.showAsPercentage = showAsPercentage;
        this.sliderUsed = sliderUsed;
        this.specialSlider = specialSlider;
        this.sliderStep = sliderStep;
        this.sliderValueMin = sliderValueMin;
        this.sliderValueMax = sliderValueMax;
    }
    VRGuiOption(float sliderValueMin,
                float sliderValueMax,
                float sliderStep) {
        this(
                false,true, false,
                sliderStep,
                sliderValueMin,
                sliderValueMax
        );
    }
    VRGuiOption() {
        this(
                false,false, false,
                0,1,0
        );
    }


    public Object updateValue(Object old) {
        return null;
    }
    public Object setValue(Object newValue) {
        return null;
    }

    public Float getSliderValue(float value) {
        return null;
    }

    public void onChanged() {
    }


    public String getDisplayString(String prefix, Object value) {
        return null;
    }

}
