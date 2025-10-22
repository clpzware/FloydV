package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.input.KeyboardInputEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;

public class ScriptKeyboardInputEvent extends ScriptEvent<KeyboardInputEvent> {

    public ScriptKeyboardInputEvent(final KeyboardInputEvent wrappedEvent) {
        super(wrappedEvent);
    }

    public int getKey() {
        return this.wrapped.getKeyCode();
    }

    @Override
    public String getHandlerName() {
        return "onKeyboardInput";
    }
}
