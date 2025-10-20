package fr.ambient.module.impl.movement;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.item.ItemBlock;

public class SafeWalk extends Module {
    private final ModeProperty m = ModeProperty.newInstance("Mode", new String[]{"Hypixel", "Vanilla"}, "Hypixel");

    public SafeWalk() {
        super(15,"Prevents you from falling off edges while walking.", ModuleCategory.MOVEMENT);
        this.registerProperties(m);
    }

    public boolean doSafeWalk(){
        if(!this.isEnabled()){
            return false;
        }
        if (mc.thePlayer.onGround && !mc.thePlayer.isSneaking() && mc.thePlayer.rotationPitch > 70 && mc.thePlayer.movementInput.moveForward < 0) {
            return mc.thePlayer.inventory.getCurrentItem() != null && mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock;
        }
        return false;
    }

    @SubscribeEvent
    private void onEventMovement(MovementEvent event){
        if(doSafeWalk()){
            if(m.is("Hypixel")){
                MoveUtil.strafeWithEvent(event, 0.21f);
            }
        }
    }
}
