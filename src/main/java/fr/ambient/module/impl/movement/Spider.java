package fr.ambient.module.impl.movement;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.spider.VanillaSpider;
import fr.ambient.module.impl.movement.spider.VerusSpider;
import fr.ambient.module.impl.movement.spider.VulcanSpider;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;

public class Spider extends Module {


    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Verus",
            new VerusSpider("Verus", this),
            new VulcanSpider("Vulcan", this),
            new VanillaSpider("Vanilla", this)

    );

    public Spider() {
        super(94, "Allows you to walk on Wall", ModuleCategory.MOVEMENT);
        registerProperties(mode.getModeProperty());
        this.moduleModeProperties.add(mode);
    }
}
