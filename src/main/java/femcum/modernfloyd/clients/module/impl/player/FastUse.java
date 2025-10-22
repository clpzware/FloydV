package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(aliases = {"module.player.fastuse.name"}, description = "module.player.fastuse.description", category = Category.PLAYER)
public class FastUse extends Module {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 1, 24, 1);

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.isUsingItem() && mc.thePlayer.itemInUseCount == 31) {
            for (int i = 0; i <= speed.getValue().intValue(); i++) {
                PacketUtil.send(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
            }
        }

    };
}