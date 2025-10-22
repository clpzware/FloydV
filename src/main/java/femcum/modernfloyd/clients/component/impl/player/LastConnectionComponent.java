package femcum.modernfloyd.clients.component.impl.player;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.ServerJoinEvent;

public final class LastConnectionComponent extends Component {
    public static String ip;
    public static int port;

    @EventLink
    public final Listener<ServerJoinEvent> join = event -> {
        ip = event.getIp();
        port = event.getPort();
    };

}