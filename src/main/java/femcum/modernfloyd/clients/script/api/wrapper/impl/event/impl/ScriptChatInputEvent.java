package femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl;

import femcum.modernfloyd.clients.event.impl.input.ChatInputEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.CancellableScriptEvent;

public class ScriptChatInputEvent extends CancellableScriptEvent<ChatInputEvent> {

    public ScriptChatInputEvent(final ChatInputEvent wrappedEvent) {
        super(wrappedEvent);
    }

    public String getMessage() {
        return this.wrapped.getMessage();
    }

    @Override
    public String getHandlerName() {
        return "onChatInput";
    }
}
