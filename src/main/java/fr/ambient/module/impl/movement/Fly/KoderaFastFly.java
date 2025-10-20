package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.Pack;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class KoderaFastFly extends ModuleMode {
    public KoderaFastFly(String modeName, Module module) {
        super(modeName, module);
    }



    public void onEnable(){
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 3, mc.thePlayer.posZ, false));
        PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
        dmgTicks = 0;
    }

    private int dmgTicks = 0;

    @SubscribeEvent
    private void onPre(PreMotionEvent event){

        if(mc.thePlayer.hurtTime > 0){
            dmgTicks++;
        }

        if(dmgTicks > 0){
            dmgTicks++;
        }

    }

    @SubscribeEvent
    private void onMove(MovementEvent event){
        if(dmgTicks > 2){
            if(mc.thePlayer.posY % 1 == 0){
                event.setY(0);
            }
            MoveUtil.strafe(2f);
        }
        if(dmgTicks > 10){
            dmgTicks = 0;
            this.getSuperModule().setEnabled(false);
        }

    }
}
