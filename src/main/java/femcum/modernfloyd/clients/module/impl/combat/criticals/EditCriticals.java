package femcum.modernfloyd.clients.module.impl.combat.criticals;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.impl.combat.Criticals;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import rip.vantage.commons.util.time.StopWatch;

public final class EditCriticals extends Mode<Criticals> {

    private final NumberValue delay = new NumberValue("Delay", this, 500, 0, 1000, 50);

    private final double[] VALUES = new double[]{0.0005D, 0.0001D};
    private final StopWatch stopwatch = new StopWatch();

    private boolean attacked;
    private int ticks;

    public EditCriticals(String name, Criticals parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.onGround && attacked) {
            ticks++;

            switch (ticks) {
                case 1: {
                    event.setPosY(event.getPosY() + VALUES[0]);
                    break;
                }

                case 2: {
                    event.setPosY(event.getPosY() + VALUES[1]);
                    attacked = false;
                    break;
                }
            }

            event.setOnGround(false);
        } else {
            attacked = false;
            ticks = 0;
        }
    };

    @EventLink
    public final Listener<AttackEvent> onAttackEvent = event -> {
        if (mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && stopwatch.finished(delay.getValue().longValue())) {
            mc.thePlayer.onCriticalHit(event.getTarget());

            stopwatch.reset();
            attacked = true;
        }
    };
}
