package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public class ChunkLoadNoFall extends Mode<NoFall> {

    private boolean fakeUnloaded;

    public ChunkLoadNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (fakeUnloaded) {
            mc.thePlayer.motionY = 0.0D;

            event.setOnGround(false);
            event.setPosY(event.getPosY() - 0.098F);
            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, event.getPosY(), mc.thePlayer.posZ);

            return;
        }

        if (mc.thePlayer.motionY > 0.0D || FallDistanceComponent.distance <= 3.0F) {
            return;
        }

        final Block nextBlock = PlayerUtil.block(new BlockPos(
                event.getPosX(),
                event.getPosY() + mc.thePlayer.motionY,
                event.getPosZ()
        ));

        if (nextBlock.getMaterial().isSolid()) {
            FallDistanceComponent.distance = 0.0F;
            fakeUnloaded = true;
        }
    };

    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        fakeUnloaded = false;
    };
}
