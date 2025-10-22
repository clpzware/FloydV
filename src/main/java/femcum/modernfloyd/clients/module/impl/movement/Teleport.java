package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.teleport.WatchdogBedWarsTeleport;
import femcum.modernfloyd.clients.module.impl.movement.teleport.WatchdogTeleport;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.teleport.name"}, description = "module.movement.teleport.description", category = Category.MOVEMENT)
public class Teleport extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new WatchdogTeleport("Watchdog (Deprecated)", this))
            .add(new WatchdogBedWarsTeleport("Watchdog BedWars", this))
            .setDefault("Vanilla");

}