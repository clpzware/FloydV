package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.component.impl.player.PingSpoofComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public final class DelayVelocity extends Mode<Velocity> {

    private final NumberValue delay = new NumberValue("Delay", this, 10, 1, 50, 1);
    private final BooleanValue pingSpoof = new BooleanValue("Ping Spoof", this, true);

    public DelayVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event ->
            PingSpoofComponent.spoof(delay.getValue().intValue() * 50, pingSpoof.getValue(), true, pingSpoof.getValue(), false);

}