package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.inventorymove.BufferAbuseInventoryMove;
import femcum.modernfloyd.clients.module.impl.movement.inventorymove.CancelInventoryMove;
import femcum.modernfloyd.clients.module.impl.movement.inventorymove.NormalInventoryMove;
import femcum.modernfloyd.clients.module.impl.movement.inventorymove.WatchdogInventoryMove;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.inventorymove.name"}, description = "module.movement.inventorymove.description", category = Category.MOVEMENT)
public class InventoryMove extends Module {

    private final ModeValue mode = new ModeValue("Bypass Mode", this)
            .add(new NormalInventoryMove("Normal", this))
            .add(new BufferAbuseInventoryMove("Buffer Abuse", this))
            .add(new CancelInventoryMove("Cancel", this))
            .add(new WatchdogInventoryMove("Watchdog", this))
            .setDefault("Normal");
}
