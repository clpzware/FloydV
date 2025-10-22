package femcum.modernfloyd.clients.event.impl.input;

import femcum.modernfloyd.clients.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public final class GuiClickEvent extends CancellableEvent {
    private final int mouseX, mouseY, mouseButton;
}