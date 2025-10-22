package femcum.modernfloyd.clients.script.api.wrapper.impl.event;

import femcum.modernfloyd.clients.event.CancellableEvent;

public abstract class CancellableScriptEvent<T extends CancellableEvent> extends ScriptEvent<T> {

    public CancellableScriptEvent(final T wrappedEvent) {
        super(wrappedEvent);
    }

    public boolean isCancelled() {
        return this.wrapped.isCancelled();
    }

    public void setCancelled(final boolean cancelled) {
        this.wrapped.setCancelled(cancelled);
    }
}
