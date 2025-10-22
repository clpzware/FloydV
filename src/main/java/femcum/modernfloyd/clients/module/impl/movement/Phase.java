package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.phase.NormalPhase;
import femcum.modernfloyd.clients.module.impl.movement.phase.VulcanPhase;
import femcum.modernfloyd.clients.module.impl.movement.phase.WatchdogAutoPhase;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.phase.name"}, description = "module.movement.phase.description", category = Category.MOVEMENT)
public class Phase extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new NormalPhase("Normal", this))
            .add(new VulcanPhase("Vulcan", this))
            .add(new WatchdogAutoPhase("Watchdog Auto Phase", this))
            .setDefault("Normal");

}