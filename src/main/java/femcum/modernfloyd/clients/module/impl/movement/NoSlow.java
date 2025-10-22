package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.noslow.*;
import femcum.modernfloyd.clients.module.impl.movement.noslow.*;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.noslow.name"}, description = "module.movement.noslow.description", category = Category.MOVEMENT)
public class NoSlow extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaNoSlow("Vanilla", this))
            .add(new NCPNoSlow("NCP", this))
            .add(new NewNCPNoSlow("New NCP", this))
            .add(new IntaveNoSlow("Intave", this))
            .add(new OldIntaveNoSlow("Old Intave", this))
            .add(new VariableNoSlow("Variable", this))
            .add(new PredictionNoSlow("Prediction", this))
            .add(new WatchdogNoSlow("Watchdog", this))
            .add(new GrimNoslow("Grim",this))
            .setDefault("Vanilla");

    public final BooleanValue foodValue = new BooleanValue("Food", this, false);
    public final BooleanValue potionValue = new BooleanValue("Potion", this, false);
    public final BooleanValue swordValue = new BooleanValue("Sword", this, false);
    public final BooleanValue bowValue = new BooleanValue("Bow", this, false);

}