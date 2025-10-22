package femcum.modernfloyd.clients.component.impl.packetlog;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.ServerJoinEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;

public class PacketLogComponent extends Component {

    private int worldChanges;

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        worldChanges++;
    };

    @EventLink
    public final Listener<ServerJoinEvent> onServerJoin = event -> {
        worldChanges = 0;
    };

    public boolean hasChangedWorlds() {
        return worldChanges > 0;
    }
}