package fr.ambient.event.impl.client;

import fr.ambient.event.Event;
import lombok.Getter;
import lombok.Setter;

public class ClientRunTickEvent extends Event {


    @Getter
    @Setter
    private boolean isTickBase = false;
}
