package femcum.modernfloyd.clients.module.impl.combat.velocity;

import femcum.modernfloyd.clients.component.impl.player.BadPacketsComponent;
import femcum.modernfloyd.clients.component.impl.player.TargetComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.combat.Velocity;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C0APacketAnimation;

import java.util.List;

public final class GrimReduceVelocity extends Mode<Velocity> {

    public GrimReduceVelocity(String name, Velocity parent) {
        super(name, parent);
    }

    @EventLink(value = Priorities.VERY_LOW)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (getParent().onSwing.getValue() && !mc.thePlayer.isSwingInProgress ||
                mc.thePlayer.ticksExisted <= 20) return;

        List<EntityLivingBase> targets = TargetComponent.getTargets(7);

        if (targets.isEmpty()) return;

        if (mc.thePlayer.ticksSinceVelocity <= 14 && !BadPacketsComponent.bad()) {
            PacketUtil.send(new C0APacketAnimation());
            mc.playerController.attackEntity(mc.thePlayer, targets.get(0));
        }
    };
}
