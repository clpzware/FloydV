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
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemSoup;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class AutoSoup extends Module {
    public AutoSoup() {
        super(92, ModuleCategory.PLAYER);
        registerProperties(mode, minhp, delay, dropOnUse);
    }

    public ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"HVH"}, "HVH");
    public NumberProperty minhp = NumberProperty.newInstance("Minimum HP", 1f, 13f, 20f, 1f);
    public NumberProperty delay = NumberProperty.newInstance("Delay", 0f, 500f, 5000f, 10f);
    public BooleanProperty dropOnUse = BooleanProperty.newInstance("Drop On Use", true);
    public TimeUtil timerUtil = new TimeUtil();


    // also pasted from legitclient

    @SubscribeEvent
    public void onEvent(PreMotionEvent event) {
        if (mc.thePlayer.getHealth() < minhp.getValue() && timerUtil.finished(delay.getValue().longValue())) {
            if (mode.is("HVH")) {
                eatHVH();
                timerUtil.reset();
            }
        }

    }

    public void eatHVH() {
        int currSlot = mc.thePlayer.inventory.currentItem;
        int gapSlot = getSoupSlot();

        if (gapSlot == -1) {
            return;
        }

        mc.thePlayer.inventory.currentItem = gapSlot;
        mc.playerController.syncCurrentPlayItem();
        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getStackInSlot(gapSlot)));
        if (dropOnUse.getValue()) {
            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, gapSlot + 36, 1, 4, mc.thePlayer);
        }
        mc.thePlayer.inventory.currentItem = currSlot;

    }



    public int getSoupSlot() {
        for (int i = 0; i < 9; i++) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i) == null) {
                continue;
            }
            if (Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(i).getItem() instanceof ItemSoup) {
                return i;
            }
        }
        return -1;
    }
}
