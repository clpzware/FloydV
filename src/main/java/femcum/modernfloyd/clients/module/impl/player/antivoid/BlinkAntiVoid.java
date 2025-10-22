package femcum.modernfloyd.clients.module.impl.player.antivoid;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.component.impl.player.PingSpoofComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.AntiVoid;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;

public class BlinkAntiVoid extends Mode<AntiVoid> {
    private Vector3d position, motion;
    private Vector2f rotation;

    public BlinkAntiVoid(String name, AntiVoid parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (mc.thePlayer.ticksExisted <= 60) return;

        if (position != null && motion != null && rotation != null && !PlayerUtil.isBlockUnder(50, true)) {
//            PingSpoofComponent.blink();
            //TODO BLINK

            if (FallDistanceComponent.distance > 4) {
                mc.thePlayer.setPosition(position.x, position.y, position.z);
                mc.thePlayer.motionX = 0;
                mc.thePlayer.motionY = MoveUtil.predictedMotion(motion.y);
                mc.thePlayer.motionZ = 0;
                mc.thePlayer.rotationYaw = rotation.x;
                mc.thePlayer.rotationPitch = rotation.y;
                FallDistanceComponent.distance = 0;
                PingSpoofComponent.packets.removeIf(timedPacket -> !(timedPacket.getPacket() instanceof C0FPacketConfirmTransaction || timedPacket.getPacket() instanceof C00PacketKeepAlive));
//                PingSpoofComponent.disable();
                PingSpoofComponent.dispatch();
            }
        } else {
            position = new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            motion = new Vector3d(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
            rotation = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        }
    };
}