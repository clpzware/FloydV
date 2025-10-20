package fr.ambient.module.impl.player.antivoid;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.AntiVoid;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class PacketAntivoid extends ModuleMode {

    private boolean tried = false;

    public PacketAntivoid(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {


        AntiVoid av = (AntiVoid) this.getSuperModule();
        if (mc.thePlayer.fallDistance > av.distance.getValue() && !tried && !PlayerUtil.isBlockUnder(50)) {
            PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 1, false));
            tried = true;
        }
    }
}
