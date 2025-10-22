package femcum.modernfloyd.clients.module.impl.ghost;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.EvictingList;
import femcum.modernfloyd.clients.util.RayCastUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(aliases = {"module.ghost.aimbacktrack.name"}, description = "module.ghost.aimbacktract.description", category = Category.GHOST)
public class AimBacktrack extends Module {

    private final NumberValue backtrack = new NumberValue("Rotation Backtrack Amount", this, 1, 1, 20, 1);
    private EvictingList<Vector2f> previousRotations = new EvictingList<>(1);
    private boolean attacked;
    private int lastSize;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (lastSize != backtrack.getValue().intValue()) {
            previousRotations = new EvictingList<>(backtrack.getValue().intValue());
            lastSize = backtrack.getValue().intValue();
        }

        previousRotations.add(new Vector2f(event.getYaw(), event.getPitch()));

        attacked = false;
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        final Packet<?> packet = event.getPacket();

        if (packet instanceof C0APacketAnimation && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
            for (final Vector2f rotation : previousRotations) {
                final Reach reach = this.getModule(Reach.class);
                final MovingObjectPosition movingObjectPosition = RayCastUtil.rayCast(rotation, reach.isEnabled() ? 3.0D + reach.range.getValue().doubleValue() : 3.0D);

                if (movingObjectPosition.entityHit != null && !attacked && movingObjectPosition.entityHit instanceof EntityLivingBase) {
                    final AttackEvent e = new AttackEvent((EntityLivingBase) movingObjectPosition.entityHit);
                    Floyd.INSTANCE.getEventBus().handle(e);

                    if (e.isCancelled()) return;
                    mc.playerController.attackEntity(mc.thePlayer, movingObjectPosition.entityHit);
                }
            }
        }
    };

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        if (attacked) {
            event.setCancelled();
        }
        attacked = true;
    };
}