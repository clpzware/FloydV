package fr.ambient.event.impl.render;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;

@AllArgsConstructor
@Getter
public class RenderCapeLayerEvent extends Event {
    private EntityPlayer entityPlayer;
}
