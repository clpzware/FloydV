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
import net.minecraft.block.*;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import static fr.ambient.util.player.MoveUtil.getSwiftnessSpeed;

public class WatchdogSpeed extends ModuleMode {

    public boolean hasFinished = true;
    private final Speed speed = (Speed) this.getSuperModule();

    public WatchdogSpeed(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        boolean isScaffoldEnabled = Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled();

        if (!speed.watchdogmulti.isSelected("LowHop On Scaffold") && Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()) {
            if (mc.thePlayer.onGround && MoveUtil.moving()) {
                mc.thePlayer.jump();
            }
            return;
        }


        if (speed.towerfix()) return;
        if (speed.invmovefix()) return;
        if (!speed.fastFallConditions()) return;

        hasFinished = true;

        if (mc.thePlayer.onGround && MoveUtil.moving()) {
            mc.thePlayer.motionY = 0.42;

            float speed2 = isScaffoldEnabled ? 0.52f : (PlayerUtil.isNearSlabAndStairs() ? 0.5f : 0.6f);
            MoveUtil.strafe(0.48f, 0.51f, speed2);

        } else if (speed.watchdogmulti.isSelected("FastFall")) {

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
                switch (mc.thePlayer.airTicks) {
                    case 1, 3, 4, 5, 8, 9 ->  MoveUtil.strafe(getSwiftnessSpeed(0.3f, 0.23));
                }
            doGlideStrafe();
        }
    }


    @SubscribeEvent
    private void onStrafeEvent(PostStrafeEvent event) {
        if (speed.watchdogmulti.isSelected("AirStrafe") && airstrafeConditions() && mc.thePlayer.hurtTime == 0) {
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
            MoveUtil.strafe(getSwiftnessSpeed(0.3f, 0.23));
        }

        if (mc.thePlayer.airTicks >= 1 && (blockRelative != Blocks.air || blockRelative == Blocks.carpet || mc.thePlayer.airTicks == 9) && strafeConditions()) {
            mc.thePlayer.motionY += 0.075;
            MoveUtil.strafe(MoveUtil.speed() );
        }
    }



    public boolean strafeConditions() {
        boolean airStrafe = speed.watchdogmulti.isSelected("AirStrafe");

        return (airStrafe || mc.thePlayer.moveStrafing != 0) && !speed.slab
                && !(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down(1)).getBlock() instanceof BlockAir && !PlayerUtil.Speedblacklist() && !Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()
                && !mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isCollidedVertically && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava());

    }


    private boolean airstrafeConditions() {
        return !mc.thePlayer.isCollidedHorizontally && mc.thePlayer.hurtTime == 0 && !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()
                && !Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() && !speed.slab && !mc.thePlayer.isCollidedVertically;
    }
}