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
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import org.lwjglx.input.Keyboard;

public class WatchdogSemiSprint extends ModuleMode {

    private final Scaffold sc = (Scaffold) this.getSuperModule();
    private boolean lagbackDetected = false;
    private int lagbackTicks = 0;

    public WatchdogSemiSprint(String modeName, Module module) {
        super(modeName, module);
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
        } else if(((Scaffold)getSuperModule()).isMightPlaceThisTurn()){
            MoveUtil.strafe(MoveUtil.speed() * 0.95f);
            ((Scaffold)getSuperModule()).setMightPlaceThisTurn(false); // GOD BYPASS NO METHOD NEEDED OMG
        }
    }

    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            lagbackDetected = true;
            lagbackTicks = 0;
            ChatUtil.display("Lagback detected !!! Slowing Down...");
        }
    }

    @SubscribeEvent
    private void onJump(JumpEvent e) {
        if (sc.towerMMP.getModeProperty().equals("None")) {
            e.setSprinting(false);
        }
    }
}
