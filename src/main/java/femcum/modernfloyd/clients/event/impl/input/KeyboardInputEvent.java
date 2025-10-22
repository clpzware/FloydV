package femcum.modernfloyd.clients.event.impl.input;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptKeyboardInputEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;
@Getter
@AllArgsConstructor
public final class KeyboardInputEvent extends CancellableEvent {

    private final int keyCode;
    private final char character;
    private final GuiScreen guiScreen;

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptKeyboardInputEvent(this);
    }
}
