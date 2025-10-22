package femcum.modernfloyd.clients.module.impl.combat.criticals;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.impl.combat.Criticals;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;
import rip.vantage.commons.util.time.StopWatch;

public final class PacketCriticals extends Mode<Criticals> {
    private final NumberValue delay = new NumberValue("Delay", this, 500, 0, 1000, 1);

    private final double[] offsets = new double[]{0.0625, 0};
    private final StopWatch stopwatch = new StopWatch();

    public PacketCriticals(String name, Criticals parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        if (stopwatch.finished(delay.getValue().longValue()) && mc.thePlayer.onGroundTicks > 2) {
            for (final double offset : offsets) {
                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + offset, mc.thePlayer.posZ, false));
            }

            mc.thePlayer.onCriticalHit(event.getTarget());
            stopwatch.reset();
        }
    };
}
