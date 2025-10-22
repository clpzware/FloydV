package femcum.modernfloyd.clients.module.impl.movement.noslow;

import femcum.modernfloyd.clients.component.impl.player.BlinkComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.SlowDownEvent;
import femcum.modernfloyd.clients.module.impl.movement.NoSlow;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class IntaveNoSlow extends Mode<NoSlow> {

    boolean usingItem;

    public IntaveNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.thePlayer.getCurrentEquippedItem() == null) return;

        final Item item = mc.thePlayer.getCurrentEquippedItem().getItem();

        if (mc.thePlayer.isUsingItem()) {
            if (item instanceof ItemSword && getParent().swordValue.getValue()) {
                BlinkComponent.blinking = true;

                if (mc.thePlayer.ticksExisted % 5 == 0) {
                    PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    BlinkComponent.dispatch();
                    mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getCurrentEquippedItem()));
                }

            } else if (item instanceof ItemFood && getParent().foodValue.getValue() || item instanceof ItemBow && getParent().bowValue.getValue()) {
                BlinkComponent.blinking = true;
            }

            usingItem = true;
        } else if (usingItem) {
            usingItem = false;

            BlinkComponent.blinking = false;
        }
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if (getParent().foodValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
            event.setCancelled();
        }
        if (getParent().potionValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
            event.setCancelled();
        }
        if (getParent().swordValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            event.setCancelled();
        }
        if (getParent().bowValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            event.setCancelled();
        }
    };
}
