package fr.ambient.event.impl.player.move;


import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class StepEvent extends Event {

    private double stepHeight;

}
