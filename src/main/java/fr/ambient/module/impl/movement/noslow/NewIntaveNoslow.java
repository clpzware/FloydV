package fr.ambient.module.impl.movement.noslow;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import static net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM;


public class NewIntaveNoslow extends ModuleMode {

    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public NewIntaveNoslow(String modeName, Module module) {
        super(modeName, module);
    }



    @SubscribeEvent
    private void onSendPacket(PacketSendEvent event) {
        if (noslow.isAllowed() && mc.thePlayer.isUsingItem()) {
            PacketUtil.sendPacketNoEvent(new C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
        }
    }
}