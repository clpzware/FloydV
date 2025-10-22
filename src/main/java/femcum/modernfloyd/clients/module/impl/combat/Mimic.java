package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.combat.mimic.ClickerMimic;
import femcum.modernfloyd.clients.module.impl.combat.mimic.RotationsMimic;
import femcum.modernfloyd.clients.value.impl.BooleanValue;

@ModuleInfo(aliases = {"Mimic"}, description = "", category = Category.COMBAT)
public final class Mimic extends Module {

    private BooleanValue rotations = new BooleanValue("Rotations", this, true, new RotationsMimic("Rotations", this));
    private BooleanValue clicker = new BooleanValue("Clicker", this, true, new ClickerMimic("Clicker", this));

}
