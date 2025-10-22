package femcum.modernfloyd.clients.module.impl.player.antivoid;

import femcum.modernfloyd.clients.component.impl.player.BlinkComponent;
import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.player.AntiVoid;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

public class WatchdogAntiVoid extends Mode<AntiVoid> {

    private Vector3d position, motion;

    private boolean wasVoid, setBack;
    private int overVoidTicks;

    public WatchdogAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }

    private final NumberValue distance = new NumberValue("Distance", this, 5, 0, 10, 1);

    @EventLink()
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (mc.thePlayer.ticksExisted <= 75) return;

        boolean overVoid = !mc.thePlayer.onGround && !PlayerUtil.isBlockUnder();

        if (overVoid) {
            overVoidTicks++;
        } else if (mc.thePlayer.onGround) {
            overVoidTicks = 0;
        }

        if (overVoid && position != null && motion != null && overVoidTicks < 30 + distance.getValue().doubleValue() * 20) {
            if (!setBack) {
                wasVoid = true;

                BlinkComponent.blinking = true;
                BlinkComponent.setExempt(C0FPacketConfirmTransaction.class, C00PacketKeepAlive.class, C01PacketChatMessage.class);

                if (FallDistanceComponent.distance > distance.getValue().doubleValue() || setBack) {
                    PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(position.x, position.y - 0.1 - Math.random(), position.z, false));

                    BlinkComponent.packets.clear();

                    FallDistanceComponent.distance = 0;

                    setBack = true;
                }
            } else {
                BlinkComponent.blinking = false;
            }
        } else {

            setBack = false;

            if (wasVoid) {
                BlinkComponent.blinking = false;
                wasVoid = false;
            }

            motion = new Vector3d(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
            position = new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }
    };
}


