package femcum.modernfloyd.clients.event.impl.input;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptChatInputEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public final class ChatInputEvent extends CancellableEvent {
    private String message;

    public static void implementation(String message) {
        Floyd.INSTANCE.getEventBus().handle(new ChatInputEvent(message));
    }

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptChatInputEvent(this);
    }
}