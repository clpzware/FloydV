package fr.ambient.module.impl.movement;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.longjump.*;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;


public class LongJump extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "WatchdogFireball",
            new WatchdogFbLongjump("WatchdogFireball", this),
            new MinibloxLongJump("Miniblox Glide", this),
            new WatchdogBowLongJump("Watchdog Bow", this),
            new VanillaLongjump("Vanilla", this),
            new VulcanLongJump("Vulcan", this),
            new GhostlyLongjump("Ghostly", this),
            new AACLongjump("Vulcan Boat", this)
    );

    public BooleanProperty auto = BooleanProperty.newInstance("Automatic", true, () -> mode.getModeProperty().is("WatchdogFireball"));
    public BooleanProperty boost = BooleanProperty.newInstance("Boost", true, () -> mode.getModeProperty().is("Ghostly"));
    public NumberProperty Speed = NumberProperty.newInstance("Speed", 0.1f, 1f, 9.5f, 0.1f
            , () -> mode.getModeProperty().is("Vanilla") || mode.getModeProperty().is("Ghostly"));
    public NumberProperty height = NumberProperty.newInstance("Height", 0.1f, 1f, 9.5f, 0.1f
            , () -> mode.getModeProperty().is("Vanilla"));

    public LongJump() {
        super(13, "Allows you to jump much further than normal.", ModuleCategory.MOVEMENT);
        this.registerProperties(mode.getModeProperty(),auto,boost,Speed,height);
        this.moduleModeProperties.add(mode);
        this.setSuffix(mode.getModeProperty()::getValue);
    }
}