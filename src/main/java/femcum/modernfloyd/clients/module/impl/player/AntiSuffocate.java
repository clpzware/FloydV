package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleInfo(aliases = {"module.player.antisuffocate.name"}, description = "module.player.antifsuffocate.description", category = Category.PLAYER)
public class AntiSuffocate extends Module {

    private final BooleanValue swing = new BooleanValue("Swing", this, true);

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.isEntityInsideOpaqueBlock()) {
            PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, new BlockPos(mc.thePlayer).down(), EnumFacing.UP));

            if (swing.getValue()) {
                mc.thePlayer.swingItem();
            }
        }
    };
}