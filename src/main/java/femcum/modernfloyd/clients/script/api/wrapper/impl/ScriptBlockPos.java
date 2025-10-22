package femcum.modernfloyd.clients.script.api.wrapper.impl;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.script.api.wrapper.ScriptWrapper;
import femcum.modernfloyd.clients.script.api.wrapper.impl.vector.ScriptVector3d;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.player.SlotUtil;
import net.minecraft.util.BlockPos;

public class ScriptBlockPos extends ScriptWrapper<BlockPos> {

    public ScriptBlockPos(final BlockPos wrapped) {
        super(wrapped);
    }

    public ScriptVector3d getPosition() {
        return new ScriptVector3d(this.wrapped.getX(), this.wrapped.getY(), this.wrapped.getZ());
    }

    public float getHardness() {
        return SlotUtil.getPlayerRelativeBlockHardness(MC.thePlayer, MC.theWorld, this.wrapped, Floyd.INSTANCE.getComponentManager().get(Slot.class).getItemIndex());
    }

    public float getHardness(int hotBarSlot) {
        return SlotUtil.getPlayerRelativeBlockHardness(MC.thePlayer, MC.theWorld, this.wrapped, hotBarSlot);
    }

    public ScriptBlock getBlock() {
        return new ScriptBlock(PlayerUtil.block(this.wrapped));
    }
}
