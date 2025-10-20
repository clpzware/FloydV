package fr.ambient.module.impl.player;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class AutoPot extends Module {
    public AutoPot() {
        super(106, ModuleCategory.PLAYER);
        registerProperties(mode, minhp, delay);
    }

    public ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Blatant", "Switch"}, "Blatant");
    public NumberProperty minhp = NumberProperty.newInstance("Minimum HP", 1f, 13f, 20f, 1f);
    public NumberProperty delay = NumberProperty.newInstance("Delay", 0f, 500f, 5000f, 10f);
    public BooleanProperty notmove = BooleanProperty.newInstance("While Not Moving", true);

    public TimeUtil timerUtil = new TimeUtil();

    @SubscribeEvent
    public void onEvent(PreMotionEvent event) {
        if (mc.thePlayer.getHealth() < minhp.getValue() && timerUtil.finished(delay.getValue().longValue())) {
            if (notmove.getValue() && !MoveUtil.moving()) {
                return;
            }
            switch (mode.getValue()) {
                case "Blatant" -> {
                    throwPotion(false);
                    timerUtil.reset();
                }
                case "Switch" -> {
                    throwPotion(true);
                    timerUtil.reset();
                }
            }
        }
    }


    public void throwPotion(boolean switchMode) {
        int currSlot = mc.thePlayer.inventory.currentItem;
        int potSlot = getPotionSlot();

        if (potSlot == -1) {
            return;
        }

        mc.thePlayer.inventory.currentItem = potSlot;
        mc.playerController.syncCurrentPlayItem();
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(potSlot)));

        if (!switchMode) {
            mc.thePlayer.inventory.currentItem = currSlot;
        }
    }

    public int getPotionSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i);
            if (stack == null) {
                continue;
            }
            if (stack.getItem() instanceof ItemPotion potion) {
                if (ItemPotion.isSplash(stack.getMetadata())) {
                    return i;
                }
            }
        }
        return -1;
    }
}