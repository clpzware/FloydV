package fr.ambient.module.impl.render.world;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.NumberProperty;
import net.minecraft.network.play.server.S03PacketTimeUpdate;


public class Ambience extends Module {

    private final NumberProperty val = NumberProperty.newInstance("Time", 0f, 0f, 24000f, 100f);

    public Ambience() {
        super(39,"Changes the time of day for your client", ModuleCategory.RENDER);
        this.registerProperties(val);
    }

    @SubscribeEvent
    private void onPre(UpdateEvent e) {
        mc.theWorld.setWorldTime(val.getValue().intValue());
    }


    @SubscribeEvent
    private void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S03PacketTimeUpdate)
            event.setCancelled(true);
    }
}





