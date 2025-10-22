package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.component.impl.player.BlinkComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C03PacketPlayer;

public class MMCFlight extends Mode<Flight> {

    private boolean clipped;
    private int ticks;

    public MMCFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        clipped = false;
        ticks = 0;
    }

    @Override
    public void onDisable() {
        BlinkComponent.blinking = false;
        MoveUtil.stop();
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        ticks++;

        if (mc.thePlayer.onGround) {
            MoveUtil.stop();
        } else {
            return;
        }

        if (ticks == 1) {
            if (PlayerUtil.blockRelativeToPlayer(0, -2.5, 0).isFullBlock()) {
                mc.timer.timerSpeed = 0.1F;
                BlinkComponent.blinking = true;

                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, MoveUtil.roundToGround(mc.thePlayer.posY - (2.5 - (Math.random() / 100))), mc.thePlayer.posZ, false));
                PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));

                clipped = true;

                mc.thePlayer.jump();
                MoveUtil.strafe(7 - Math.random() / 10);
            }
        }
    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        MoveUtil.strafe();
    };

    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        if (clipped) {
            event.setCancelled();
            clipped = false;
        }
    };
}