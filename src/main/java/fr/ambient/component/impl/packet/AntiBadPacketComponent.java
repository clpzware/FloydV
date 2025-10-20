package fr.ambient.component.impl.packet;

import fr.ambient.component.Component;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.client.ClientRunTickEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class AntiBadPacketComponent extends Component {


    private boolean sent08 = false;
    private boolean sent07 = false;
    private boolean sent09 = false;

}