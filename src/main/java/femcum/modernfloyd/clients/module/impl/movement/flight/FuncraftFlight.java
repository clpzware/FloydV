package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.MoveEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class FuncraftFlight extends Mode<Flight> {
    public FuncraftFlight(String name, Flight parent) {
        super(name, parent);
    }

    private final NumberValue speed = new NumberValue("Speed", this, 1.2, 0.8, 2, 0.05);
    private final BooleanValue vanillaKickBypass = new BooleanValue("Vanilla Kick Bypass", this, true);

    private double moveSpeed;
    private int stage, ticks;

    @Override
    public void onEnable() {
        moveSpeed = 0;
        stage = mc.thePlayer.onGround ? 0 : -1;
        ticks = 0;
    }

    @EventLink
    private final Listener<PreMotionEvent> preMotionEventListener = event -> {
        event.setOnGround(true);
    };

    @EventLink(Priorities.VERY_HIGH)
    private final Listener<MoveEvent> moveEventListener = event -> {
        if (!MoveUtil.isMoving() || mc.thePlayer.isCollidedHorizontally) {
            stage = -1;
        }

        // vanilla kick bypass
        if (vanillaKickBypass.getValue() && ticks > 125) {
            stage = -1;
            ticks = 0;
            PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 5, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 5, true));
            return;
        }

        switch (stage) {
            case -1:
                mc.thePlayer.motionY = 0;
                event.setPosY(-0.00001);
                return;
            case 0:
                moveSpeed = 0.3;
                break;
            case 1:
                if (mc.thePlayer.onGround) {
                    event.setPosY(mc.thePlayer.motionY = 0.3999);
                    moveSpeed *= 2.14;
                }
                break;
            case 2:
                moveSpeed = speed.getValue().doubleValue();
                break;
            default:
                moveSpeed -= moveSpeed / 109;
                mc.thePlayer.motionY = 0;
                event.setPosY(-0.00001);
                break;
        }

        mc.thePlayer.jumpMovementFactor = 0F;
        MoveUtil.setSpeedMoveEvent(event, Math.max(moveSpeed, MoveUtil.getAllowedHorizontalDistance()));
        stage++;
    };
}