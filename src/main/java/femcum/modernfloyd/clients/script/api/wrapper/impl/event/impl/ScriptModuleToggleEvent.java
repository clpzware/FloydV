package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.other.ModuleToggleEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.ScriptModule;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptModuleToggleEvent extends ScriptEvent<ModuleToggleEvent> {

    public ScriptModuleToggleEvent(ModuleToggleEvent wrappedEvent) {
        super(wrappedEvent);
    }

    public ScriptModule getModule() {
        return new ScriptModule(wrapped.getModule());
    }

    @Override
    public String getHandlerName() {
        return "onModuleToggle";
    }
}
