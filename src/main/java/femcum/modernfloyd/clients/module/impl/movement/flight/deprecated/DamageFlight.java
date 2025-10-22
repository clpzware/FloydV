package femcum.modernfloyd.clients.module.impl.movement.flight.deprecated;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.MoveInputEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.player.DamageUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;

public class DamageFlight extends Mode<Flight> {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);

    public DamageFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        DamageUtil.damagePlayer(DamageUtil.DamageType.POSITION, 3.42F, 1, false, false);
    }

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        final float speed = this.speed.getValue().floatValue();

        event.setSpeed(speed);
    };

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        final float speed = this.speed.getValue().floatValue();

        mc.thePlayer.motionY = 0.0D
                + (mc.gameSettings.keyBindJump.isKeyDown() ? speed : 0.0D)
                - (mc.gameSettings.keyBindSneak.isKeyDown() ? speed : 0.0D);
    };

    @EventLink
    public final Listener<MoveInputEvent> onMove = event -> {
        event.setSneak(false);
    };

    @Override
    public void onDisable() {
        MoveUtil.stop();
    }

}
