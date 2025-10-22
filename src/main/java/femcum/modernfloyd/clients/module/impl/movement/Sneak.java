package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.sneak.HoldSneak;
import femcum.modernfloyd.clients.module.impl.movement.sneak.NCPSneak;
import femcum.modernfloyd.clients.module.impl.movement.sneak.StandardSneak;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.sneak.name"}, description = "module.movement.sneak.description", category = Category.MOVEMENT)
public class Sneak extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new StandardSneak("Standard", this))
            .add(new HoldSneak("Hold", this))
            .add(new NCPSneak("NCP", this))
            .setDefault("Standard");
}