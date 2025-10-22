package femcum.modernfloyd.clients.module.impl.player.antivoid;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.AntiVoid;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public class PacketAntiVoid extends Mode<AntiVoid> {

    private final NumberValue distance = new NumberValue("Distance", this, 5, 0, 10, 1);

    public PacketAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.fallDistance > distance.getValue().floatValue() && !PlayerUtil.isBlockUnder()) {
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition());
        }
    };
}