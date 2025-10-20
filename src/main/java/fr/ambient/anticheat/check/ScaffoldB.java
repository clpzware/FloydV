package fr.ambient.anticheat.check;

import fr.ambient.anticheat.Check;
import fr.ambient.module.impl.misc.Anticheat;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class ScaffoldB extends Check {
    public ScaffoldB(Anticheat anticheat) {
        super("Scaffold", anticheat);
    }

    @Override
    public void onBlockMod(BlockPos pos, IBlockState state) {
        if (!anticheat.isEnabled() || !anticheat.checks.isSelected("Scaffold")) {
            return;
        }

        if(state.getBlock() instanceof BlockLiquid){
            return; // tends to false
        }

        mc.theWorld.playerEntities.forEach(player -> {
            if (player.hurtTime == 0) {
                if (player.getDistance(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) < 2) {
                    if (player.onGround) {
                        if (MoveUtil.getSpeed(player) > 0.1 && player.rotationPitch > 60 && player.sneakTicks < 2 && checkDir(player)) {
                            flagPlayer(player, 1);
                        }
                    }
                }
            }
        });
    }

    public boolean checkDir(EntityPlayer player) {
        double deltaX = player.posX - player.lastTickPosX;
        double deltaZ = player.posZ - player.lastTickPosZ;
        float yaw = (float) Math.toDegrees(-Math.atan(deltaX / deltaZ));
        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if (deltaX < 0 && deltaZ < 0) {
            yaw = (float) (90 + v);
        } else if (deltaX > 0 && deltaZ < 0) {
            yaw = (float) (-90 + v);
        }
        float diff = Math.abs(MathHelper.wrapAngleTo180_float(yaw) - MathHelper.wrapAngleTo180_float(player.rotationYaw));

        return diff > 20;
    }
}
