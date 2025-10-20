package fr.ambient.module.impl.player.scaffold.tower;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.util.BlockPos;

public class VanillaTower extends ModuleMode {

    public VanillaTower (String modeName, Module module) {
        super(modeName, module);
    }

    public boolean canTower = false;
    public boolean Tower = false;

    public void onEnable() {
        canTower = false;
        Tower = false;
    }

    public void onDisable() {
        Tower = false;
    }

    private boolean conditions() {
        return mc.gameSettings.keyBindJump.isKeyDown() && canTower;
    }

    @SubscribeEvent
    private void onPreMotion(PreMotionEvent event) {
        if (conditions()) {
            MoveUtil.strafe(MoveUtil.getSwiftnessSpeed(MoveUtil.speed(), 0.2));
            switch (mc.thePlayer.airTicks) {
                case 0 -> mc.thePlayer.motionY = 0.41999998688698f;
                case 1 -> mc.thePlayer.motionY = 0.33;
                case 2 -> {
                    mc.thePlayer.airTicks = -1;
                    mc.thePlayer.motionY = 1 - mc.thePlayer.posY % 1;
                }
            }
        } else {
            if (!canTower && mc.thePlayer.onGround) canTower = true;
            else if (!mc.thePlayer.onGround) canTower = false;
        }
    }
}
