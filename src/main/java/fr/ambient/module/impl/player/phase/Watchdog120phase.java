package fr.ambient.module.impl.player.phase;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;


public class Watchdog120phase extends ModuleMode {


    public Watchdog120phase(String modeName, Module module) {
        super(modeName, module);
    }

    public void onEnable() {
        BlockPos blockPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ);
        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.DOWN));
        mc.thePlayer.swingItem();
        PacketUtil.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.DOWN));
        mc.playerController.onPlayerDestroyBlock(blockPos, EnumFacing.DOWN);
        double x = (double) blockPos.getX() + 0.5;
        double z = (double) blockPos.getZ() + 0.5;
        mc.thePlayer.setPosition(x, mc.thePlayer.posY, z);

    }
}