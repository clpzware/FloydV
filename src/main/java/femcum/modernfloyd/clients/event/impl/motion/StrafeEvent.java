package femcum.modernfloyd.clients.event.impl.motion;

import femcum.modernfloyd.clients.event.CancellableEvent;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.ScriptEvent;
import femcum.modernfloyd.clients.script.api.wrapper.impl.event.impl.ScriptStrafeEvent;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public final class StrafeEvent extends CancellableEvent implements Accessor {

    private float forward;
    private float strafe;
    private float friction;
    private float yaw;

    public void setSpeed(final double speed, final double motionMultiplier) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        mc.thePlayer.motionX *= motionMultiplier;
        mc.thePlayer.motionZ *= motionMultiplier;
    }

    public void setSpeed(final double speed) {
        setFriction((float) (getForward() != 0 && getStrafe() != 0 ? speed * 0.98F : speed));
        MoveUtil.stop();
    }

    @Override
    public ScriptEvent<? extends Event> getScriptEvent() {
        return new ScriptStrafeEvent(this);
    }
}
