package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.component.impl.player.GUIDetectionComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.math.MathUtil;
import femcum.modernfloyd.clients.util.player.ItemUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.BoundsNumberValue;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import rip.vantage.commons.util.time.StopWatch;
@ModuleInfo(aliases = {"module.player.stealer.name", "Stealer"}, description = "module.player.stealer.description", category = Category.PLAYER)
public class Stealer extends Module {

    private final BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 100, 150, 0, 500, 50);
    private final BooleanValue ignoreTrash = new BooleanValue("Ignore Trash", this, true);
    private final StopWatch stopwatch = new StopWatch();
    private long nextClick;
    private int lastClick;
    private int lastSteal;
    private int open;
    private boolean finished;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        if (mc.currentScreen instanceof GuiChest) {
            open++;
            this.finished = false;
            final ContainerChest container = (ContainerChest) mc.thePlayer.openContainer;

            if (GUIDetectionComponent.inGUI() || !this.stopwatch.finished(this.nextClick)) {
                return;
            }

            this.lastSteal++;

            for (int i = 0; i < container.inventorySlots.size(); i++) {
                final ItemStack stack = container.getLowerChestInventory().getStackInSlot(i);

                if (stack == null || this.lastSteal <= 1) {
                    continue;
                }

                if (this.ignoreTrash.getValue() && !ItemUtil.useful(stack)) {
                    continue;
                }

                this.nextClick = Math.round(MathUtil.getRandom(this.delay.getValue().intValue(), this.delay.getSecondValue().intValue()));
                mc.playerController.windowClick(container.windowId, i, 0, 1, mc.thePlayer);
                this.stopwatch.reset();
                this.lastClick = 0;
                if (this.nextClick > 0) return;
            }

            this.lastClick++;

            if (this.lastClick > 1 && open > 2 + (2 * Math.random())) {
                mc.thePlayer.closeScreen();
                this.finished = true;
            }
        } else {
            this.lastClick = 0;
            this.open = 0;
            this.lastSteal = 0;
        }
    };

    public boolean isFinished() {
        return this.finished;
    }
}