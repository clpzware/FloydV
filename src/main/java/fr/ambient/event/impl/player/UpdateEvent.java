package fr.ambient.event.impl.player;

import fr.ambient.event.Event;

public class UpdateEvent extends Event {
    public UpdateEvent(){
        this.setName("PlayerTickEvent");
    }
}
