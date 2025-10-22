package femcum.modernfloyd.clients.module.impl.combat.regen;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.combat.Regen;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class VanillaRegen extends Mode<Regen> {

    private final NumberValue health = new NumberValue("Minimum Health", this, 15, 1, 20, 1);
    private final NumberValue packets = new NumberValue("Speed", this, 20, 1, 100, 1);

    public VanillaRegen(String name, Regen parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.getHealth() < this.health.getValue().floatValue()) {
            for (int i = 0; i < this.packets.getValue().intValue(); i++) {
                PacketUtil.send(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
            }
        }
    };
}
