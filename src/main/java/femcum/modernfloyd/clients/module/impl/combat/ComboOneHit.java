package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.AttackEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.play.client.C02PacketUseEntity;

@ModuleInfo(aliases = {"module.combat.comboonehit.name", "Insta Kill", "Instant Kill"}, description = "module.combat.comboonehit.description", category = Category.COMBAT)
public final class ComboOneHit extends Module {

    public final NumberValue packets = new NumberValue("Attack Packets", this, 50, 1, 1000, 1);

    @EventLink
    public final Listener<AttackEvent> onAttack = event -> {
        for (int i = 0; i < packets.getValue().intValue(); i++) {
            PacketUtil.send(new C02PacketUseEntity(event.getTarget(), C02PacketUseEntity.Action.ATTACK));
        }
    };
}
