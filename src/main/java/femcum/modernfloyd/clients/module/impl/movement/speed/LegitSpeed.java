package femcum.modernfloyd.clients.module.impl.movement.speed;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Speed;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.SubMode;

public class LegitSpeed extends Mode<Speed> {

    private final ModeValue rotationExploit = new ModeValue("Rotation Exploit Mode", this)
            .add(new SubMode("Off"))
            .add(new SubMode("Rotate (Fully Legit)"))
            .add(new SubMode("Speed Equivalent (Almost legit, Very hard to flag)"))
            .setDefault("Speed Equivalent (Almost legit, Very hard to flag)");
    private final BooleanValue cpuSpeedUpExploit = new BooleanValue("CPU SpeedUp Exploit", this, true);
    private final BooleanValue noJumpDelay = new BooleanValue("No Jump Delay", this, true);

    public LegitSpeed(String name, Speed parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<PreUpdateEvent> preUpdate = event -> {
        switch (rotationExploit.getValue().getName()) {
            case "Rotate (Fully Legit)":
                if (!mc.thePlayer.onGround) {
                    RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw + 45, mc.thePlayer.rotationPitch), 10, MovementFix.NORMAL);
                }
                    break;

            case "Speed Equivalent (Almost legit, Very hard to flag)":
                MoveUtil.useDiagonalSpeed();
                break;
        }

        if (noJumpDelay.getValue()) {
            mc.thePlayer.jumpTicks = 0;
        }

        if (cpuSpeedUpExploit.getValue()) {
            mc.timer.timerSpeed = 1.004f;
        }
    };

    @EventLink(value = Priorities.VERY_HIGH)
    public final Listener<StrafeEvent> strafe = event -> {
        if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
            mc.thePlayer.jump();
        }
    };
}
