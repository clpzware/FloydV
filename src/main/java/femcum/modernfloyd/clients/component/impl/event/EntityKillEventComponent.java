package femcum.modernfloyd.clients.component.impl.event;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.event.impl.other.KillEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.util.interfaces.ThreadAccess;
import net.minecraft.entity.Entity;

public class EntityKillEventComponent extends Component implements ThreadAccess {

    Entity target = null;

    @EventLink(value = Priorities.LOW)
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (target != null && !mc.theWorld.loadedEntityList.contains(target)) {
            Floyd.INSTANCE.getEventBus().handle(new KillEvent(target));
            target = null;
        }
    };

    @EventLink(value = Priorities.LOW)
    public final Listener<AttackEvent> onAttackEvent = event -> {
        target = event.getTarget();
    };

    @EventLink(value = Priorities.LOW)
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        target = null;
    };
}
