package fr.ambient.module.impl.movement.Fly;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.world.BoundingBoxEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.util.AxisAlignedBB;

public class AirWalkFly extends ModuleMode {
    public AirWalkFly(String modeName, Module module) {
        super(modeName, module);
    }


    @Override
    public void onDisable() {
        MoveUtil.strafe(0);
        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @SubscribeEvent
    private void onPlayerTick(PreMotionEvent ignoredEvent) {
        mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? 0.5 : mc.gameSettings.keyBindSneak.isKeyDown() ? -0.5 : mc.thePlayer.onGround ? 0.00001f : mc.thePlayer.motionY;

        if (!mc.gameSettings.keyBindSneak.isKeyDown() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            if (mc.thePlayer.motionY < 0.1) {
                MoveUtil.strafe(MoveUtil.getBaseMoveSpeed());
            }
        }
    }


    @SubscribeEvent
    public void onBound(BoundingBoxEvent event) {
        AxisAlignedBB axisAlignedBB = AxisAlignedBB.fromBounds(-5, -1, -5, 5, 1, 5).offset(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ());
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            event.setBoundingBox(axisAlignedBB);
        }
    }
}
