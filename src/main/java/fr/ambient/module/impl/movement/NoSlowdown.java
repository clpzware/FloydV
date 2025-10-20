package fr.ambient.module.impl.movement;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.SlowDownEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.noslow.*;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import net.minecraft.item.*;

public class NoSlowdown extends Module {


    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Vanilla",
            new WatchdogPredNoslow("Watchdog Pred", this),
            new OldRinaorcNoslow("Old Rinaorc", this),
            new Grim117Noslow("Grim 1.17", this),
            new OldIntaveNoslow("Old Intave", this),
            new WatchdogNoslow("Watchdog", this),
            new NewIntaveNoslow("Intave", this),
            new VanillaNoslow("Vanilla", this),
            new BlinkNoslow("Blink", this)
    );

    public MultiProperty items = MultiProperty.newInstance("Allowed Items", new String[]{"Sword", "Consumables", "Bow", "Potion"});
    public MultiProperty wdoption = MultiProperty.newInstance("Options", new String[]{"Faster On Ground", "Slowdown On Slab"}, () -> mode.getModeProperty().is("Watchdog"));
    public BooleanProperty sneak = BooleanProperty.newInstance("Sneak", false, () -> !mode.getModeProperty().is("Watchdog"));


    public boolean doSend = false;
    public boolean slab = false;
    public boolean using;

    public NoSlowdown() {
        super(14, "Prevents your movement from slowing down while using items.", ModuleCategory.MOVEMENT);
        this.registerProperties(mode.getModeProperty(),items,wdoption,sneak);
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }

    @SubscribeEvent
    private void onSlowDown(SlowDownEvent event) {
        if (isAllowed() && !(wdoption.isSelected("Slowdown On Slab") && slab)) {
            event.setForward(1f);
            event.setStrafe(1f);
        } else {
            event.setForward(0.2f);
            event.setStrafe(0.2f);
        }
    }


    public boolean isAllowed() {
        if (mc.thePlayer.getHeldItem() == null) return false;

        if (items.isSelected("Sword") && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) return true;
        if (items.isSelected("Consumables") && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) return true;
        if (items.isSelected("Bow") && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) return true;
        return (items.isSelected("Potion") && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion && !canThrowPotion(mc.thePlayer.getHeldItem()));
    }

    public static boolean canThrowPotion(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemPotion) {
            return ItemPotion.isSplash(itemStack.getMetadata());
        }
        return false;
    }
}
