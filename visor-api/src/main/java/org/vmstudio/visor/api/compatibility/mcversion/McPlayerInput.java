package org.vmstudio.visor.api.compatibility.mcversion;

import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
//? if >=1.21.2 {
import net.minecraft.world.entity.player.Input;
//?}
//? if >=1.21.5 {
import net.minecraft.world.phys.Vec2;
//?}

/**
 * Cross-mc-version access to the client movement input
 */
public class McPlayerInput {
    private McPlayerInput() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    // ------- READ -------

    public static float leftImpulse(LocalPlayer player) {
        //? if >=1.21.5 {
        return player.input.getMoveVector().x;
        //?} else {
        /*return player.input.leftImpulse;
        *///?}
    }

    public static float forwardImpulse(LocalPlayer player) {
        //? if >=1.21.5 {
        return player.input.getMoveVector().y;
        //?} else {
        /*return player.input.forwardImpulse;
        *///?}
    }

    public static boolean isJumping(LocalPlayer player) {
        //? if >=1.21.2 {
        return player.input.keyPresses.jump();
        //?} else {
        /*return player.input.jumping;
        *///?}
    }

    public static boolean isSneaking(KeyboardInput input) {
        //? if >=1.21.2 {
        return input.keyPresses.shift();
        //?} else {
        /*return input.shiftKeyDown;
        *///?}
    }

    // ------- WRITE -------

    public static void setImpulses(KeyboardInput input, float left, float forward) {
        //? if >=1.21.5 {
        input.moveVector = new Vec2(left, forward);
        //?} else {
        /*input.leftImpulse = left;
        input.forwardImpulse = forward;
        *///?}
    }

    public static void setJumping(KeyboardInput input, boolean jumping) {
        //? if >=1.21.2 {
        Input keys = input.keyPresses;
        input.keyPresses = new Input(keys.forward(), keys.backward(), keys.left(), keys.right(),
                jumping, keys.shift(), keys.sprint());
        //?} else {
        /*input.jumping = jumping;
        *///?}
    }

    public static void setSneaking(KeyboardInput input, boolean sneaking) {
        //? if >=1.21.2 {
        Input keys = input.keyPresses;
        input.keyPresses = new Input(keys.forward(), keys.backward(), keys.left(), keys.right(),
                keys.jump(), sneaking, keys.sprint());
        //?} else {
        /*input.shiftKeyDown = sneaking;
        *///?}
    }
}
