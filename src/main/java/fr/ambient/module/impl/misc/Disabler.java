package fr.ambient.module.impl.misc;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.misc.disabler.*;
import fr.ambient.module.impl.player.nofall.VerusNofall;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;


public class Disabler extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog",
            new TestDisabler("Miniblox C0C", this),
            new BasicPacket("Basic Packet", this),
            new VerusCombat("Verus Combat", this),
            new NegativityDisabler("Negativity", this),
            new WatchdogDisabler("Watchdog", this),
            new MinibloxDisabler("Miniblox", this),
            new RinaorcDisabler("Rinaorc", this),
            new VulcanDisabler("Vulcan", this),
            new MushmcDisabler("Mushmc", this),
            new VerusDisabler("Verus", this),
            new LumosDisabler("Lumos", this)
    );


    //watchdog
    public MultiProperty watchdog = MultiProperty.newInstance("Watchdog Checks", new String[]{"Motion Y"}, () -> mode.getModeProperty().is("Watchdog"));
    public BooleanProperty progressbar = BooleanProperty.newInstance("Progress Bar", false, () -> mode.getModeProperty().is("Watchdog"));
    public NumberProperty WDLagback = NumberProperty.newInstance("Disabler Lagback", 23f, 25f, 35f, 1f, () -> mode.getModeProperty().is("Watchdog"));
    //rinaorc
    public MultiProperty rinaorc = MultiProperty.newInstance("Checks & Qol", new String[]{"Auto Unlink"}, () -> mode.getModeProperty().is("Rinaorc"));

    //mushmc
    public BooleanProperty timerBalance = BooleanProperty.newInstance("Balance", false, () -> mode.getModeProperty().is("MushMC"));

    //basic Packet
    public MultiProperty basicpacket = MultiProperty.newInstance("Basic Packets Check", new String[]{"Transactions (C -> S)", "KeepAlive (C -> S)", "Start Sprint (C -> S)", "Stop Sprint (C -> S)"}, () -> mode.getModeProperty().is("Basic Packet"));

    public MultiProperty verus = MultiProperty.newInstance("Verus Checks", new String[]{"Transaction Spam", "Sprint Spam","Teleport Spam"}, () -> mode.getModeProperty().is("Verus"));

    //vulcan
    public MultiProperty vulcan = MultiProperty.newInstance("Vulcan Checks", new String[]{"Sprint", "Modulo", "AutoClicker", "Velocity",}, () -> mode.getModeProperty().is("Vulcan"));


    public Disabler() {
        super(7, "Disables certain server checks or protections to avoid detection.", ModuleCategory.MISC);
        this.registerProperties(mode.getModeProperty(),
                verus,
                watchdog,
                rinaorc,
                timerBalance,
                vulcan,
                progressbar,
                basicpacket,
                WDLagback
        );
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }
}
