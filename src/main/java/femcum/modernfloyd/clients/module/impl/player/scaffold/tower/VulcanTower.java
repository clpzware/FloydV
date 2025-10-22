package femcum.modernfloyd.clients.module.impl.player.scaffold.tower;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class VulcanTower extends Mode<Scaffold> {

    public VulcanTower(String name, Scaffold parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.gameSettings.keyBindJump.isKeyDown() && PlayerUtil.blockNear(2) && mc.thePlayer.offGroundTicks > 3) {
            ItemStack itemStack = mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem];

            if (itemStack == null || (itemStack.stackSize > 2)) {
                PacketUtil.sendNoEvent(new C08PacketPlayerBlockPlacement(null));
            }
            mc.thePlayer.motionY = 0.42F;
        }
    };
}
