package femcum.modernfloyd.clients.component.impl.render;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import rip.vantage.commons.util.time.StopWatch;

public class SmoothCameraComponent extends Component {

    public static double y;
    public static StopWatch stopWatch = new StopWatch();

    public static void setY(double y) {
        stopWatch.reset();
        SmoothCameraComponent.y = y;
    }

    public static void setY() {
        if (stopWatch.finished(80)) SmoothCameraComponent.y = mc.thePlayer.lastTickPosY;
        stopWatch.reset();
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (stopWatch.finished(80)) return;
        mc.thePlayer.cameraYaw = 0;
        mc.thePlayer.cameraPitch = 0;
    };
}
