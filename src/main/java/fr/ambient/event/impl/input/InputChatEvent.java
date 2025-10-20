package fr.ambient.event.impl.input;

import fr.ambient.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class InputChatEvent extends CancellableEvent {
    private String message;
}