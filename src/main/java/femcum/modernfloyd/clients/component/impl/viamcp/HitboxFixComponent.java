package femcum.modernfloyd.clients.component.impl.viamcp;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.render.MouseOverEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;

public final class HitboxFixComponent extends Component {

    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event -> {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
//            event.setExpand(event.getExpand() - 0.1F);
        }
    };
}
