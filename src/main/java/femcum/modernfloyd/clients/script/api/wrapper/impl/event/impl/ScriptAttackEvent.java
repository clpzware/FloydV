package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.ScriptEntityLiving;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.CancellableScriptEvent;
public class ScriptAttackEvent extends CancellableScriptEvent<AttackEvent> {

    public ScriptAttackEvent(final AttackEvent wrappedEvent) {
        super(wrappedEvent);
    }

    public ScriptEntityLiving getTarget() {
        return new ScriptEntityLiving(this.wrapped.getTarget());
    }

    @Override
    public String getHandlerName() {
        return "onAttack";
    }
}
