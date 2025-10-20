package fr.ambient.module.impl.player;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.world.TickEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import net.minecraft.item.ItemBlock;

public class FastPlace extends Module {
    public FastPlace() {
        super(32,"Allows you to place blocks faster than normal.", ModuleCategory.PLAYER);
        this.registerProperties(blocksOnly);
    }

    private final BooleanProperty blocksOnly = BooleanProperty.newInstance("Blocks Only", true);

    @SubscribeEvent
    private void onUpdateNigger(TickEvent event){
        if(mc.thePlayer != null){

            if(blocksOnly.getValue() && (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock))){
                return;
            }

            mc.rightClickDelayTimer = 0;
        }

    }
}
