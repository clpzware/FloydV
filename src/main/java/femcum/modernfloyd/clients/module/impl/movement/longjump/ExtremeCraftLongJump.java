package femcum.modernfloyd.clients.module.impl.movement.longjump;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.movement.LongJump;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class ExtremeCraftLongJump extends Mode<LongJump> {

    private final NumberValue height = new NumberValue("Height", this, 1, 1, 2.5, 0.1);

    private boolean receivedDamage;

    public ExtremeCraftLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (!receivedDamage) {
            mc.timer.timerSpeed = 1.0F;
        }
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceiveEvent = event -> {
        final Packet<?> p = event.getPacket();

        if (p instanceof S08PacketPlayerPosLook && receivedDamage) {
            event.setCancelled();
            receivedDamage = false;
        }
    };

    @Override
    public void onEnable() {
        receivedDamage = false;

        if (mc.thePlayer.onGround) {
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true));
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.1F, mc.thePlayer.posZ, true));
            PacketUtil.send(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false));

            mc.timer.timerSpeed = 0.2F;

            MoveUtil.strafe(3F);

            mc.thePlayer.motionY = height.getValue().doubleValue();

            receivedDamage = true;
        }
    }

    @Override
    public void onDisable() {
        MoveUtil.stop();
    }
}