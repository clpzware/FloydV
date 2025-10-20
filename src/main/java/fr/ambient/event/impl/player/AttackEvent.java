package fr.ambient.event.impl.player;

import fr.ambient.event.CancellableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;

@Getter
@Setter
@AllArgsConstructor
public class AttackEvent extends CancellableEvent {

    private Entity entity;

}
