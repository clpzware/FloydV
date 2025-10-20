package fr.ambient.module.impl.movement.speed;

import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.player.move.PostStrafeEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class HypixelSpeed extends ModuleMode {

    public boolean hasFinished = true;
    private final Speed speed = (Speed) this.getSuperModule();

    public HypixelSpeed(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.gameSettings.keyBindJump.pressed) return;
        if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isPressed() && mc.thePlayer.hurtTime == 0 && !speed.slab && !Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()) {
            event.setPosY(event.getPosY() + 1.0E-6);
            MoveUtil.strafe(0.2f, 0.2f, 0.2795f);
        }
    }


    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (!mc.gameSettings.keyBindJump.pressed) return;
        boolean isScaffoldEnabled = Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled();
        if (speed.watchdogmulti.isSelected("AirStrafe") && airstrafeConditions()) {
            if (mc.thePlayer.airTicks == 2) {
                mc.thePlayer.motionX *= 0.99;
                mc.thePlayer.motionZ *= 0.99;
            }
        }


        if (!speed.watchdogmulti.isSelected("LowHop On Scaffold") && Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()) {
            if (mc.thePlayer.onGround && MoveUtil.moving()) {
                mc.thePlayer.jump();
            }
            return;
        }

        if (speed.towerfix()) return;
        if (!speed.fastFallConditions()) return;

        hasFinished = true;

        if (mc.thePlayer.onGround && MoveUtil.moving()) {
            mc.thePlayer.motionY = 0.42;
            MoveUtil.strafe(0.48f, 0.51f, isScaffoldEnabled ? 0.5f : 0.6f);
        } else if (speed.watchdogmulti.isSelected("FastFall") && (speed.watchdogmulti.isSelected("Speed On Hurtime") || mc.thePlayer.hurtTime == 0)) {

            hasFinished = false;

            mc.thePlayer.motionY += switch (mc.thePlayer.airTicks) {
                case 1 -> 0.05;
                case 2 -> 0.012;
                case 3 -> -0.135;
                case 4 -> -0.2;
                default -> 0;
            };
        }
        if (speed.watchdogmulti.isSelected("Strafe") && strafeConditions() && !PlayerUtil.Speedblacklist()) {
            if (speed.watchdogmulti.isSelected("AirStrafe")) {
                switch (mc.thePlayer.airTicks) {
                    case 1, 3, 4, 5, 8, 9 -> MoveUtil.strafe(0.325f, 0.328f, 0.40f);
                }
            } else {
                switch (mc.thePlayer.airTicks) {
                    case 1, 3, 4, 5, 7, 8, 9, 10, 11, 12 -> MoveUtil.strafe(0.3f, 0.33f, 0.45f);
                }
            }
            doGlideStrafe();
        }
    }

    @SubscribeEvent
    private void onStrafeEvent(PostStrafeEvent event) {
        if (!mc.gameSettings.keyBindJump.pressed) return;
        if (speed.watchdogmulti.isSelected("AirStrafe") && airstrafeConditions()) {
            double attemptAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(MoveUtil.getDirection()));
            double movementAngle = MathHelper.wrapAngleTo180_double(Math.toDegrees(Math.atan2(mc.thePlayer.motionZ, mc.thePlayer.motionX)) - 90);

            if (MathHelper.wrappedDifference(attemptAngle, movementAngle) >= 120) {
                MoveUtil.strafe2(MoveUtil.speed(), (float) movementAngle - 180);
            }
        }
    }


    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && speed.watchdogmulti.isSelected("Disable On Lagback")) {
            Ambient.getInstance().getModuleManager().getModule(Speed.class).setEnabled(false);
            ChatUtil.display("Disabled ");
        }
    }

    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        if (MoveUtil.moving()) event.setJumping(false);
    }


    private void doGlideStrafe() {
        BlockPos relativePos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ);
        Block blockRelative = mc.theWorld.getBlockState(relativePos).getBlock();

        if (blockRelative != Blocks.air && blockRelative != Blocks.carpet && mc.thePlayer.airTicks > 2 && strafeConditions()) {
            MoveUtil.strafe(0.30f, 0.33f, 0.45f);
        }

        if (mc.thePlayer.airTicks >= 1 && (blockRelative != Blocks.air || blockRelative == Blocks.carpet || mc.thePlayer.airTicks == 9) && strafeConditions()) {
            mc.thePlayer.motionY += 0.075;
            MoveUtil.strafe(MoveUtil.speed());
        }
    }



    public boolean strafeConditions() {
        boolean airStrafe = speed.watchdogmulti.isSelected("AirStrafe");
        boolean hurtTimes = speed.watchdogmulti.isSelected("Speed On Hurtime");

        return (airStrafe || mc.thePlayer.moveStrafing != 0)
                && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down(1)).getBlock() instanceof BlockAir && !PlayerUtil.Speedblacklist() && (hurtTimes || mc.thePlayer.hurtTime == 0) &&
                !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isCollidedVertically && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava());
    }


    private boolean airstrafeConditions() {
        return !mc.thePlayer.isCollidedHorizontally && mc.thePlayer.hurtTime == 0 && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()
                && !Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() && !speed.slab && !mc.thePlayer.isCollidedVertically;
    }
}