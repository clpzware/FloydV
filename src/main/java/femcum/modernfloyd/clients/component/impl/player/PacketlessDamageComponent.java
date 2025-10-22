package femcum.modernfloyd.clients.component.impl.player;

import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostStrafeEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import lombok.Getter;

public final class PacketlessDamageComponent extends Component {

    @Getter
    private static boolean active;
    private static float timer;
    @Getter
    private static int jumps;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (active) {
            if (jumps < 4) {
                mc.timer.timerSpeed = timer;

                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    jumps++;
                }

                event.setOnGround(false);
            } else if (mc.thePlayer.offGroundTicks >= 11) {
                mc.timer.timerSpeed = 1.0F;
                active = false;
                timer = 1.0F;
                jumps = 0;
            }

           // SmoothCameraComponent.setY();
        }
    };

    @EventLink
    public final Listener<PostStrafeEvent> onPostStrafe = event -> {
        if (active) {
            MoveUtil.stop();
        }
    };

    public static void setActive(final float timer) {
        PacketlessDamageComponent.active = true;
        PacketlessDamageComponent.timer = timer;
        jumps = 0;
    }
}
