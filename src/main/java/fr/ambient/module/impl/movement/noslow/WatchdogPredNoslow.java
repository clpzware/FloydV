package fr.ambient.module.impl.movement.noslow;

import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;



public class WatchdogPredNoslow extends ModuleMode {

    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public WatchdogPredNoslow(String modeName, Module module) {
        super(modeName, module);
    }


    @SubscribeEvent
    private void onNetworkUpdate(PreMotionEvent event) {
        if (mc.thePlayer.isUsingItem()) {
            if ((mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && noslow.items.isSelected("Consumables") || (mc.thePlayer.getHeldItem().getItem() instanceof ItemBow && noslow.items.isSelected("Bow")))) {
                BlinkComponent.onEnable();
            }
            noslow.using = true;
        } else if (noslow.using) {
            noslow.using = false;
            BlinkComponent.onDisable();
        }
    }
}