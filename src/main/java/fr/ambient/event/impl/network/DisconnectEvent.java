package fr.ambient.event.impl.network;

import fr.ambient.event.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.GuiScreen;

@Getter
@AllArgsConstructor
public class DisconnectEvent extends Event {

    private GuiScreen screen;

}
