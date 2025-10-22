package femcum.modernfloyd.clients.module.impl.player.nofall;

import femcum.modernfloyd.clients.component.impl.player.FallDistanceComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.NoFall;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

public class InvalidNoFall extends Mode<NoFall> {

    public InvalidNoFall(String name, NoFall parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.motionY > 0) {
            return;
        }

        float distance = FallDistanceComponent.distance;

        if (distance > 3) {
            final Block nextBlock = PlayerUtil.block(new BlockPos(event.getPosX(), event.getPosY() + mc.thePlayer.motionY, event.getPosZ()));

            if (nextBlock.getMaterial().isSolid()) {
                MoveUtil.strafe(0);
                event.setPosY(event.getPosY() - 1.5);

                distance = 0;
            }
        }

        FallDistanceComponent.distance = distance;
    };
}