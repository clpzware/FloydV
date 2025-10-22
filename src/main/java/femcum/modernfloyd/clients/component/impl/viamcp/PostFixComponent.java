package femcum.modernfloyd.clients.component.impl.viamcp;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.component.impl.player.PingSpoofComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;

public final class PostFixComponent extends Component {

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
            PingSpoofComponent.spoof(1, true, true, false, true);
        }
    };

    @EventLink()
    public final Listener<TeleportEvent> onTeleport = event -> {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
            PingSpoofComponent.dispatch();
        }
    };

}