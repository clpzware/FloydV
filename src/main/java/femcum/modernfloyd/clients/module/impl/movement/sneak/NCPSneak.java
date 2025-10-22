package femcum.modernfloyd.clients.module.impl.movement.sneak;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.movement.Sneak;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class NCPSneak extends Mode<Sneak> {

    public NCPSneak(String name, Sneak parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.thePlayer.movementInput.sneak = mc.thePlayer.sendQueue.doneLoadingTerrain;

        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
    };

    @Override
    public void onDisable() {
        PacketUtil.send(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    }
}