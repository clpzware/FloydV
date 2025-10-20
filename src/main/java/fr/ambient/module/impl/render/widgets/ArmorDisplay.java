package fr.ambient.module.impl.render.widgets;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.util.render.RenderUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class ArmorDisplay extends Module {
    public ArmorDisplay() {
        super(104, ModuleCategory.RENDER);
        this.setDraggable(true);
        this.setWidth(16);
        this.setHeight(64);
        this.setX(10);
        this.setY(300);
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event){
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.thePlayer.inventory.armorInventory[i];
            if (stack != null) {
                RenderUtil.drawItemStack(stack, this.getX(), this.getY() + (48 - (16 * i)));
            }
        }
        if (mc.currentScreen instanceof GuiChat) {
            Gui.drawRect(this.getX(), this.getY(), (int) (this.getX() + this.getWidth()), (int) (this.getY() + this.getHeight()), new Color(60, 60, 60, 60).getRGB());
        }
    }
}
