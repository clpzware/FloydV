package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.BlockAABBEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.block.BlockAir;
import net.minecraft.util.AxisAlignedBB;

public final class AirWalkFlight extends Mode<Flight> {

    public AirWalkFlight(String name, Flight parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<BlockAABBEvent> onBlockAABB = event -> {
        // Sets The Bounding Box To The Players Y Position.
        if (event.getBlock() instanceof BlockAir && !mc.thePlayer.isSneaking() && mc.thePlayer.ticksSinceTeleport > 2) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < mc.thePlayer.posY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            }
        }
    };
}
