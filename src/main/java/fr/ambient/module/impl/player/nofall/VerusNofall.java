package fr.ambient.module.impl.player.nofall;


import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.NoFall;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class VerusNofall extends ModuleMode {

    private final NoFall noFall = (NoFall) this.getSuperModule();

    public VerusNofall(String modeName, Module module) {
        super(modeName, module);
    }


    public void onDisable() {
        mc.timer.timerSpeed = 1f;
        BlinkComponent.onDisable();
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.fallDistance > 3.0f && noFall.getDistanceToGround() != 0) {
            event.setPosY(event.getPosY() + 1.0E-5);
            BlinkComponent.onEnable();
            mc.timer.timerSpeed = 0.5f;
            PacketUtil.sendPacket(new C03PacketPlayer(true));
            mc.thePlayer.fallDistance = 1f;
        } else {
            mc.timer.timerSpeed = 1f;
            BlinkComponent.onDisable();
        }
    }
}
