package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptAttackEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityLivingBase;

@Getter
@Setter
@AllArgsConstructor
public final class AttackEvent extends CancellableEvent {
    private EntityLivingBase target;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptAttackEvent(this);
    }
}