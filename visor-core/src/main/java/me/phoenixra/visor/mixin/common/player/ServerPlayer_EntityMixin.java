package me.phoenixra.visor.mixin.common.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(Entity.class)
public abstract class ServerPlayer_EntityMixin {

    @Shadow @Final protected RandomSource random;

    @Shadow
    public abstract Pose getPose();

    @Shadow
    public abstract Level level();

    @Shadow
    public abstract boolean onGround();


    @Shadow
    public abstract double getZ();

    @Shadow
    public abstract double getY();

    @Shadow
    public abstract double getX();

    @Shadow
    public abstract Vec3 position();

    @Shadow public abstract float getXRot();

    @Shadow public abstract float getYRot();

    @Shadow public abstract double getEyeY();


    @WrapMethod(method = "setPosRaw")
    protected void visor$wrapSetPosRaw(double x,
                                       double y,
                                       double z,
                                       Operation<Void> original){
        original.call(x,y,z);
    }
}
