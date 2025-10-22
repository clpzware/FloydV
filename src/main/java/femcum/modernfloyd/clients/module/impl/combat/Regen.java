package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.combat.regen.VanillaRegen;
import femcum.modernfloyd.clients.module.impl.combat.regen.VerusRegen;
import femcum.modernfloyd.clients.module.impl.combat.regen.WorldGuardRegen;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.combat.regen.name"}, description = "module.combat.regen.description", category = Category.COMBAT)
public final class Regen extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaRegen("Vanilla", this))
            .add(new VerusRegen("Verus Regen", this))
            .add(new WorldGuardRegen("World Guard", this))
            .setDefault("Vanilla");
}
