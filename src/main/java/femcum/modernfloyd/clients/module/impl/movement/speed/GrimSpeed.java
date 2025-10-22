package femcum.modernfloyd.clients.module.impl.movement.speed;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Speed;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;

public class GrimSpeed extends Mode<Speed> {

    public GrimSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<StrafeEvent> strafe = event -> mc.theWorld.playerEntities.stream()
            .filter(entityPlayer -> entityPlayer != mc.thePlayer &&
                    mc.thePlayer.getEntityBoundingBox().expand(1, 1, 1)
                            .intersectsWith(entityPlayer.getEntityBoundingBox()))
            .forEach(entityPlayer -> MoveUtil.moveFlying(0.08));
}
