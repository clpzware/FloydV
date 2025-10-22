package femcum.modernfloyd.clients.module.impl.movement.noslow;

import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.SlowDownEvent;
import femcum.modernfloyd.clients.module.impl.movement.NoSlow;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class OldIntaveNoSlow extends Mode<NoSlow> {

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        this.isUsingFood();
        this.isUsingPotion();
        this.isUsingSword();
        this.isUsingBow();
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

    public OldIntaveNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    private void isUsingFood() {
        if (getParent().foodValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood) {
            this.performBypass();
        }
    }

    private void isUsingPotion() {
        if (getParent().potionValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
            this.performBypass();
        }
    }

    private void isUsingSword() {
        if (getParent().swordValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
            this.performBypass();

        }
    }

    private void isUsingBow() {
        if (getParent().bowValue.getValue() && mc.thePlayer.isUsingItem() && mc.thePlayer.getHeldItem().getItem() instanceof ItemBow) {
            this.performBypass();

        }
    }

    private void performBypass() {
        PacketUtil.send(new C09PacketHeldItemChange(getComponent(Slot.class).getItemIndex() % 8 + 1));
        PacketUtil.send(new C09PacketHeldItemChange(getComponent(Slot.class).getItemIndex()));
    }
}