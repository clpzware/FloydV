package fr.ambient.module.impl.player.scaffold.sprint;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.JumpEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S23PacketBlockChange;

public class WatchdogSprint extends ModuleMode {

    private final Scaffold sc = (Scaffold) this.getSuperModule();
    private boolean lagbackDetected = false;
    private int lagbackTicks = 50;

    public WatchdogSprint(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.motionX *= 0.8;
        mc.thePlayer.motionZ *= 0.8;
        lagbackDetected = false;
        lagbackTicks = 0;
        super.onDisable();
    }

    @Override
    public void onEnable(){
        lagbackTicks = 0;
        lagbackDetected = false;
    }

    @SubscribeEvent
    private void onPreMotion(PreMotionEvent event) {
        if (mc.gameSettings.keyBindJump.isKeyDown()) return;
        if (lagbackDetected) {
            if (lagbackTicks < 50) {
                lagbackTicks++;
                mc.thePlayer.setSprinting(false);
                mc.gameSettings.keyBindSprint.pressed = false;
                mc.gameSettings.keyBindSprint.unpressKey();
            } else {
                lagbackDetected = false;
                lagbackTicks = 0;
            }
        } else if (PlayerUtil.getLastDistance() > 0.22 && mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
            double xDelta = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            double zDelta = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;

            double speedMultiplier = 0.5 - PlayerUtil.getSpeedEffect() * 0.05;
            double randomOffset = Math.random() * 0.007;

            event.setPosX(event.getPosX() - xDelta * (speedMultiplier + randomOffset));
            event.setPosZ(event.getPosZ() - zDelta * (speedMultiplier + randomOffset));
            event.setPosY(event.getPosY() + 0.00625 + Math.random() * 1E-3);
        }
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            lagbackDetected = true;
            lagbackTicks = 0;
        }
    }

    @SubscribeEvent
    private void onJump(JumpEvent e) {
        if (sc.towerMMP.getModeProperty().equals("None")) {
            e.setSprinting(false);
        }
    }
}
