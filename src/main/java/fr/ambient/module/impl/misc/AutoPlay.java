package fr.ambient.module.impl.misc;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.misc.autoplay.Hypixel;
import fr.ambient.module.impl.misc.autoplay.Miniblox;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;


public class AutoPlay extends Module {

    public AutoPlay() {
        super(24, "Automatically joins a new game of your choice.", ModuleCategory.MISC);
        this.registerProperties(mode.getModeProperty(), autoplay, delayAP, hypixel,miniblox, autogg, delayAGG, gg, autowho);
        this.moduleModeProperties.add(mode);
    }

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Hypixel",
            new Hypixel("Hypixel", this),
            new Miniblox("Miniblox", this)
    );


    public final BooleanProperty autoplay = BooleanProperty.newInstance("Auto Play", false);
    public NumberProperty delayAP = NumberProperty.newInstance("AutoPlay Delay", 0f, 0f, 5000f, 50f, autoplay::getValue);


    public ModeProperty miniblox = ModeProperty.newInstance("MiniBlox Gamemode", new String[]{"Skywars", "Eggwars"}, "Skywars", () -> mode.getModeProperty().is("Miniblox") && autoplay.getValue());


    //Hypixel
    public ModeProperty hypixel = ModeProperty.newInstance("Hypixel Gamemode", new String[]{"Solo Insane", "Solo Normal", "BedWars Solo", "BedWars Duo",
            "BedWars Trio", "BedWars 4s", "Classic Duel"}, "Solo Insane", () -> mode.getModeProperty().is("Hypixel") && autoplay.getValue());
    public BooleanProperty autowho = BooleanProperty.newInstance("Auto Who", false, () -> mode.getModeProperty().is("Hypixel"));
    public BooleanProperty autogg = BooleanProperty.newInstance("Auto GG", false, () -> mode.getModeProperty().is("Hypixel"));
    public ModeProperty gg = ModeProperty.newInstance("Mode", new String[]{"GG", "How to play BW", "gg ez"}, "GG", () -> mode.getModeProperty().is("Hypixel") && autogg.getValue());
    public NumberProperty delayAGG = NumberProperty.newInstance("AutoGG Delay", 0f, 0f, 5000f, 50f, () -> mode.getModeProperty().is("Hypixel") && autogg.getValue());
}