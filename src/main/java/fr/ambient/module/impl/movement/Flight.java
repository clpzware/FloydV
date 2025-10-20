package fr.ambient.module.impl.movement;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.Fly.*;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;


public class Flight extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Vanilla",
            new HypixelPredMovementDisabler("Hypixel", this),
            new VulcanFly("Vulcan Glide", this),
            new WdTntFly("Watchdog Ctw", this),
            new MinibloxFly("Miniblox", this),
            new VanillaFly("Vanilla", this),
            new CreativeFly("Creative", this),
            new VerusFly("Verus", this),
            new AirWalkFly("Air Walk", this),
            new AirJumpFly("Air Jump", this),
            new KoderaSlowFly("Kodera Slow", this),
            new KoderaFastFly("Kodera Damage", this)
            );

    public ModeProperty minibloxmode = ModeProperty.newInstance("Miniblox Mode", new String[]{"CobWeb", "Blinkless"}, "CobWeb", () -> mode.getModeProperty().is("Miniblox"));
    public ModeProperty verusmode = ModeProperty.newInstance("Verus Mode", new String[]{"Damage", "Vanilla","AirWalk"}, "Damage", () -> mode.getModeProperty().is("Verus"));
    public NumberProperty Speed = NumberProperty.newInstance("Fly Speed", 0.1f, 1f, 9.5f, 0.1f, () -> (mode.getModeProperty().is("Vanilla") || mode.getModeProperty().is("Verus")) && verusmode.is("Damage"));


    public Flight() {
        super(78, "Allows you to fly freely in survival mode.", ModuleCategory.MOVEMENT);
        this.registerProperties(mode.getModeProperty(),Speed,minibloxmode,verusmode);
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }
}