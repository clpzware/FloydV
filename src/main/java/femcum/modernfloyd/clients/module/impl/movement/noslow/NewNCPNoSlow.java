package femcum.modernfloyd.clients.module.impl.movement.noslow;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.player.BadPacketsComponent;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.component.impl.render.NotificationComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.SlowDownEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.module.impl.combat.KillAura;
import femcum.modernfloyd.clients.module.impl.movement.NoSlow;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

public class NewNCPNoSlow extends Mode<NoSlow> {

    private int disable;

    public NewNCPNoSlow(String name, NoSlow parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        this.disable++;
        this.isUsingFood();
        this.isUsingPotion();
        this.isUsingSword();
    };

    @EventLink
    public final Listener<SlowDownEvent> onSlowDown = event -> {
        if (!(Floyd.INSTANCE.getModuleManager().get(KillAura.class).target == null)) {
            return;
        }
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

    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        this.disable = 0;
    };

    @Override
    public void onEnable() {
        NotificationComponent.post("Credit", "Thanks Auth for this No Slow", 5000);
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

    private void performBypass() {
        if (this.disable > 10 && !BadPacketsComponent.bad(false, true, true, false, false) && Floyd.INSTANCE.getModuleManager().get(KillAura.class).target == null) {
            PacketUtil.send(new C09PacketHeldItemChange(getComponent(Slot.class).getItemIndex() % 8 + 1));
            PacketUtil.send(new C09PacketHeldItemChange(getComponent(Slot.class).getItemIndex()));
            PacketUtil.send(new C08PacketPlayerBlockPlacement(getComponent(Slot.class).getItemStack()));
        }
    }
}