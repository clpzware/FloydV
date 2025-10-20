package fr.ambient.module.impl.player;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.math.TimeUtil;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class Refill extends Module {
    public Refill() {
        super(91, ModuleCategory.PLAYER);
        registerProperties(delaySinceInvOpen, delay, soup, potions, onlyInInventory);
    }

    // directly pasted from legitclient v2
    // i love myself fr

    public NumberProperty delaySinceInvOpen = NumberProperty.newInstance("Pre Refill Delay", 0f, 100f, 1000f, 10f);
    public NumberProperty delay = NumberProperty.newInstance("Delay", 0f, 100f, 1000f, 10f);
    public BooleanProperty soup = BooleanProperty.newInstance("Soup", true);
    public BooleanProperty potions = BooleanProperty.newInstance("Potions", true);
    public BooleanProperty onlyInInventory = BooleanProperty.newInstance("Only in inventory", true);
    public TimeUtil timerUtil = new TimeUtil();
    public TimeUtil timerUtil2 = new TimeUtil();

    @SubscribeEvent
    public void onUpdate(PreMotionEvent event){
        if (!(mc.currentScreen instanceof GuiInventory) && onlyInInventory.getValue()) {
            timerUtil2.reset();
            return;
        }
        int need = getNeededPots();

        boolean can = timerUtil2.finished(delaySinceInvOpen.getValue().longValue());

        if (timerUtil.finished(delay.getValue().longValue()) && need != 0 && can) {
            int slot = getPot();
            if(slot != -1){
                shiftClick(slot);
                timerUtil.reset();
            }

        }
    }

    public void shiftClick(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);

    }

    public int getNeededPots() {
        int needed = 0;

        for (int i = 0; i < 9; i++) {
            if (mc.thePlayer.inventory.getStackInSlot(i) == null) {
                needed++;
            }
        }
        return needed;
    }

    public int getPot() {
        for (int i = 9; i < 36; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack stack = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                if (stack == null) {
                    continue;
                }
                if (stack.getItem() instanceof ItemPotion && potions.getValue()) {
                    ItemPotion potionItem = (ItemPotion) stack.getItem();
                    PotionEffect effect = potionItem.getEffects(stack).get(0);
                    if (effect.getPotionID() == Potion.heal.id) {
                        return i;
                    }
                }
                if (stack.getItem() instanceof ItemSoup && soup.getValue()) {
                    return i;
                }
            }
        }
        return -1;
    }

}
