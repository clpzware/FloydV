package fr.ambient.module.impl.player;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.math.TimeUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;

public class LegitScaffold extends Module {
    public LegitScaffold() {
        super(34,"Automatically sneaks on the edge of blocks for you.", ModuleCategory.PLAYER);
        this.registerProperties(numberProperty,faster);
    }


    private BooleanProperty faster = BooleanProperty.newInstance("Faster", false);
    private NumberProperty numberProperty = NumberProperty.newInstance("Delay", 50f, 50f, 500f, 50f);

    private TimeUtil timerUtil = new TimeUtil();

    @SubscribeEvent
    public void moveInputEvent(MoveInputEvent event){
        if (event.isSneaking()) {
            return;
        }

        if ((mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).down()).getBlock() instanceof BlockAir && mc.thePlayer.onGround)) {
            timerUtil.reset();
        }
        if (event.getForward() < 0 && !timerUtil.finished(numberProperty.getValue().longValue())) {
            event.setSneaking(true);
            if (faster.getValue()) {
                event.setSneakMultiplier(0.34);
            }
        }
    }

}
