package fr.ambient.module.impl.render.widgets;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.util.render.RenderUtil;
import net.minecraft.item.ItemStack;

import java.awt.*;

import static fr.ambient.util.render.RenderUtil.drawItemStack;

public class InventoryDisplay extends Module {
    public InventoryDisplay() {
        super(102, ModuleCategory.RENDER);
        this.setDraggable(true);
        this.setHeight(64);
        this.setWidth(144);
        this.setX(200);
        this.setY(200);
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event){

        RenderUtil.drawRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), new Color(40,40,40,20));

        int fs = 0;
        int fy = 0;

        int fa = 0;

        for (ItemStack stack : mc.thePlayer.inventoryContainer.getInventory()) {
            fa++;

            if(fa == 10){
                drawItemStack(stack, this.getX(), this.getY());
            }
            if(fa > 10){
                fs++;


                if(fs % 9 == 0){
                    fy++;
                    fs = 0;
                }
                drawItemStack(stack, this.getX() + fs * 16, this.getY() + fy * 16);
            }
        }
    }

}
