package femcum.modernfloyd.clients.event.impl.other;

import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptModuleToggleEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class ModuleToggleEvent implements Event {
    private Module module;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptModuleToggleEvent(this);
    }
}