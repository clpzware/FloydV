package femcum.modernfloyd.clients.module.impl.ghost;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.ghost.wtap.LegitWTap;
import femcum.modernfloyd.clients.module.impl.ghost.wtap.SilentWTap;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;

@ModuleInfo(aliases = {"module.ghost.wtap.name", "Extra Knock Back", "Super Knock Back", "Knock Back", "Sprint Reset"}, description = "module.ghost.wtap.description", category = Category.GHOST)
public class WTap extends Module {
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new LegitWTap("Legit", this))
            .add(new SilentWTap("Silent", this))
            .setDefault("Legit");

    public final NumberValue chance = new NumberValue("WTap Chance", this, 100, 0, 100, 1);
}