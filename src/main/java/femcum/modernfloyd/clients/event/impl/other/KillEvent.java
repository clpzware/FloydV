package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptKillEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;

@Getter
@AllArgsConstructor
public final class KillEvent implements Event {

    Entity entity;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptKillEvent(this);
    }

}