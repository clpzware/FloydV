package fr.ambient.event.impl.render;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.player.EntityPlayer;

@AllArgsConstructor
@Getter
public class ModelAnglesEvent extends Event {

    private EntityPlayer player;
    private ModelPlayer model;

}
