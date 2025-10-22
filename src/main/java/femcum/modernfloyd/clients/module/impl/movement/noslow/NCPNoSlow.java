package femcum.modernfloyd.clients.module.impl.movement.noslow;

import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.SlowDownEvent;
import femcum.modernfloyd.clients.module.impl.movement.NoSlow;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class NCPNoSlow extends Mode<NoSlow> {

    public NCPNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (getParent().swordValue.getValue() && mc.thePlayer.isBlocking()) {
            PacketUtil.send(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
        }
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (getParent().swordValue.getValue() && mc.thePlayer.isBlocking()) {
            PacketUtil.send(new C08PacketPlayerBlockPlacement(getComponent(Slot.class).getItemStack()));
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