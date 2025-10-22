package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.component.impl.player.BadPacketsComponent;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.math.MathUtil;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.value.impl.BoundsNumberValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import rip.vantage.commons.util.time.StopWatch;

@ModuleInfo(aliases = {"module.player.autohead.name"}, description = "module.player.autohead.description", category = Category.PLAYER)
public class AutoHead extends Module {

    private final NumberValue health = new NumberValue("Health", this, 15, 1, 20, 1);
    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 500, 1000, 50, 5000, 50);

    private final StopWatch stopWatch = new StopWatch();

    private long nextUse;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (!stopWatch.finished(nextUse) || mc.thePlayer.getAbsorptionAmount() > 0 ||
                this.getModule(Scaffold.class).isEnabled()) {
            return;
        }

        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

            if (stack == null) {
                continue;
            }

            final Item item = stack.getItem();

            if (item instanceof ItemSkull) {
                if (mc.thePlayer.getHealth() > this.health.getValue().floatValue()) {
                    continue;
                }

                getComponent(Slot.class).setSlot(i);

                if (!BadPacketsComponent.bad()) {
                    mc.playerController.syncCurrentPlayItem();
                    PacketUtil.send(new C08PacketPlayerBlockPlacement(getComponent(Slot.class).getItemStack()));

                    nextUse = Math.round(MathUtil.getRandom(delay.getValue().longValue(), delay.getSecondValue().longValue()));
                    stopWatch.reset();
                }
            }
        }
    };
}