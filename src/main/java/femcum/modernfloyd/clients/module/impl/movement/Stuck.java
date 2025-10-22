package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostStrafeEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(aliases = {"module.movement.stuck.name"}, description = "module.movement.stuck.description", category = Category.MOVEMENT)
public class Stuck extends Module {
    private Vector3d motion;

    @Override
    public void onEnable() {
        motion = new Vector3d(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
    }

    @Override
    public void onDisable() {
        mc.thePlayer.motionX = motion.x;
        mc.thePlayer.motionY = motion.y;
        mc.thePlayer.motionZ = motion.z;
    }

    @EventLink
    public final Listener<PostStrafeEvent> onPostStrafe = event -> {
        MoveUtil.stop();
        mc.thePlayer.motionY = 0;
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        Packet<?> packet = event.getPacket();

        if (packet instanceof C03PacketPlayer) {
            event.setCancelled();
        }
    };
}