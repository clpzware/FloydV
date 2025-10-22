package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.other.BlockBreakEvent;
import femcum.modernfloyd.clients.event.impl.other.BlockDamageEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.player.SlotUtil;
import net.minecraft.util.BlockPos;

@ModuleInfo(aliases = {"module.player.autotool.name"}, description = "module.player.autotool.description", category = Category.PLAYER)
public class AutoTool extends Module {

    private int blockBreak;
    private BlockPos blockPos;

    @EventLink(Priorities.VERY_HIGH)
    public final Listener<BlockDamageEvent> onBlockDamage = event -> {
        if (event.getPlayer() != mc.thePlayer || mc.thePlayer.getDistanceSq(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ()) > 5 * 5)
            return;
        blockBreak = 15;
        blockPos = event.getBlockPos();
        this.update();
    };

    @EventLink()
    public final Listener<BlockBreakEvent> onBlockBreak = event ->
            blockBreak = 0;

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event ->
            this.update();

    public void update() {
        if (mc.objectMouseOver == null || blockBreak <= 0) {
            return;
        }

        blockBreak--;

        int index = SlotUtil.findTool(blockPos);
        getComponent(Slot.class).setSlot(index);
    }
}