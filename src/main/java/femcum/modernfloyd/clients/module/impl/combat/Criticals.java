package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.combat.criticals.*;
import femcum.modernfloyd.clients.module.impl.combat.criticals.*;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.combat.criticals.name"}, description = "module.combat.criticals.description", category = Category.COMBAT)
public final class Criticals extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new PacketCriticals("Packet", this))
            .add(new EditCriticals("Edit", this))
            .add(new NoGroundCriticals("No Ground", this))
            .add(new VulcanCriticals("Vulcan", this))
            .add(new WatchdogCriticals("Watchdog", this))
            .add(new VerusCriticals("Verus", this))
            .setDefault("Packet");
}
