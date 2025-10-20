package fr.ambient.module.impl.player.scaffold.sprint;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjglx.input.Keyboard;

import java.util.Random;

public class WatchdogBoostSprint extends ModuleMode {
    private boolean lagbackDetected = false;
    private int lagbackTicks = 0;
    private int slowdown = 0;
    private int offset = 0;
    private final Scaffold sc = (Scaffold) this.getSuperModule();

    public WatchdogBoostSprint(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        if (!conditions()) return;

        if (sc.jump.getValue()) {
            mc.thePlayer.motionY = 0.42;
        } else if (PlayerUtil.isBlockUnder(1) && !PlayerUtil.isNearSlabAndStairs()) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.1, mc.thePlayer.posZ);
        } else if (!PlayerUtil.isBlockUnder(50)) {
            mc.thePlayer.motionY = 0.42;
        }

        offset = 0;
        lagbackTicks = 0;
        lagbackDetected = false;
    }

    @Override
    public void onDisable() {
        if (conditions()) {
            mc.thePlayer.motionY = 0.42;
            mc.thePlayer.motionX *= 0.8;
            mc.thePlayer.motionZ *= 0.8;
        }
    }

    @SubscribeEvent
    private void onPreMotion(PreMotionEvent event) {
        Random random = new Random();

        if (!Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled() && MoveUtil.moving() && Math.random() > 0.05 && mc.thePlayer.isPotionActive(Potion.moveSpeed) && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ), EnumFacing.UP.getIndex(), new ItemStack(Blocks.ice), random.nextFloat(), 1.0F, random.nextFloat()));
        }

        if (conditions() && !Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
            event.setPosY(event.getPosY() + (1e-10 + (Math.random() * 1e-35)));
        }
        offset++;
    }

    @SubscribeEvent
    private void onUpdateEvent(UpdateEvent event) {
        if (lagbackDetected) {
            if (lagbackTicks++ < 50) {
                MoveUtil.strafe(0.15f, 0.18f, 0.145f);
            } else {
                lagbackDetected = false;
                lagbackTicks = 0;
            }
            return;
        }

        if (conditions() && mc.thePlayer.groundTicks > 2) {
            slowdown++;
            boolean diagonal = MoveUtil.isDiag();

            if (diagonal) {
                if (slowdown % 4 == 0) {
                    MoveUtil.strafe(0.15f, 0.145f, 0.145f);
                } else {
                    MoveUtil.strafe(0.2f, 0.245f, 0.280f);
                }
            } else {
                if (slowdown == 8 && mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe(0.20f, 0.15f, 0.145f);
                } else {
                    MoveUtil.strafe(0.2f, 0.245f, 0.296f);
                }
            }

            if (slowdown > 8) slowdown = 0;
        }
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            lagbackDetected = true;
            lagbackTicks = 0;
        }
    }

    private boolean conditions() {
        return !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && mc.thePlayer.onGround;
    }
}
