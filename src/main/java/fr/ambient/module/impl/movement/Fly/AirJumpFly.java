package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.world.BoundingBoxEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.AxisAlignedBB;

public class AirJumpFly extends ModuleMode {
    private double ypos;

    public AirJumpFly(String modeName, Module module) {
        super(modeName, module);
    }

    @Override
    public void onEnable() {
        ypos = Math.floor(mc.thePlayer.posY);
    }


    @SubscribeEvent(EventPriority.VERY_LOW)
    private void onMovementInputEvent(MoveInputEvent event) {
        if (MoveUtil.moving()) event.setJumping(false);
    }


    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }
    }

    @SubscribeEvent
    public void onBoundingBox(BoundingBoxEvent event) {
        if (event.getBlock() instanceof BlockAir && (mc.thePlayer.posY < ypos + 1.0)) {
            double x = event.getBlockPos().getX();
            double y = event.getBlockPos().getY();
            double z = event.getBlockPos().getZ();

            if (ypos < mc.thePlayer.posY) {
                event.setBoundingBox(new AxisAlignedBB(x - 15.0, y - 1.0, z - 15.0, x + 15.0, y + 1.0, z + 15.0));
            }
        }
    }
}