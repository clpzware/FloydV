package femcum.modernfloyd.clients.component.impl.viamcp;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.MinimumMotionEvent;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;

public final class MinimumMotionFixComponent extends Component {

    @EventLink
    public final Listener<MinimumMotionEvent> onMinimumMotion = event -> {
        if (ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8)) {
            event.setMinimumMotion(0.003D);
        }
    };
}