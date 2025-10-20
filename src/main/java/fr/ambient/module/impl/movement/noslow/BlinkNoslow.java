package fr.ambient.module.impl.movement.noslow;

import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.movement.NoSlowdown;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;




public class BlinkNoslow extends ModuleMode {


    private final NoSlowdown noslow = (NoSlowdown) this.getSuperModule();

    public BlinkNoslow(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onNetworkUpdate(PreMotionEvent event) {
        if (mc.thePlayer.getItemInUseDuration() == 2 && noslow.isAllowed()) {
            BlinkComponent.onEnable();
        } else if (mc.thePlayer.getItemInUseDuration() == 29) {
            PacketUtil.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 1, null, 0.0f, 0.0f, 0.0f));
            BlinkComponent.onDisable();
        }
    }
}