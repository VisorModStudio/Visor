package me.phoenixra.visor.core.client.render.helpers;

import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.client.render.VRDisplay;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRScreenHelper {


    public static boolean shouldOccludeGui() {
        if(VRRenderState.getCurrentVRDisplay() == VRDisplay.THIRD_PERSON){
            return true;
        }
        Vec3 pos = ClientContext.player
                .getPose(PoseType.RENDER)
                .getElementForDisplay(VRRenderState.getCurrentVRDisplay())
                .getPosition();

        return !VRRenderState.isInMainMenu()
                && MC.screen == null
                && !isInSolidBlock(pos);
    }

    public static boolean isInSolidBlock(Vec3 in) {
        if (MC.level == null) {
            return false;
        } else {
            BlockPos blockpos = BlockPos.containing(in);
            return MC.level.getBlockState(blockpos).isSolidRender(MC.level, blockpos);
        }
    }
}
