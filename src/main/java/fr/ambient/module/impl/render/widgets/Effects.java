package fr.ambient.module.impl.render.widgets;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import fr.ambient.util.render.font.TTFFontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Effects extends Module {
    public Effects() {
        super(103, ModuleCategory.RENDER);

        this.setDraggable(true);
        this.setHeight(20);
        this.setWidth(50);
        this.setX(150);
        this.setY(200);
    }


    @SubscribeEvent
    private void onRender2D(Render2DEvent event){
        List<PotionEffect> activePotions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        TTFFontRenderer potionFont = Fonts.getNunito(16);
        TTFFontRenderer timeFont = Fonts.getNunito(14);
        activePotions.sort(Comparator.comparingDouble(e -> -potionFont.getWidth(I18n.format(e.getEffectName()))));
        ResourceLocation inventoryBackground = new ResourceLocation("textures/gui/container/inventory.png");
        if(activePotions.isEmpty()){
            this.setHeight(20);
            this.setWidth(50);
        }else{
            for (int count = 0; count < activePotions.size(); count++) {

                PotionEffect effect = activePotions.get(count);
                Potion potion = Potion.potionTypes[effect.getPotionID()];
                String str = I18n.format(potion.getName()) + (effect.getAmplifier() > 0 ? " " + (effect.getAmplifier() + 1) : "");

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                this.mc.getTextureManager().bindTexture(inventoryBackground);
                GlStateManager.enableBlend();
                if (potion.hasStatusIcon()) {
                    int i1 = potion.getStatusIconIndex();
                    RenderUtil.drawTexturedModalRect(this.getX(), this.getY() + count * 20, i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18);
                }
                GlStateManager.disableBlend();

                potionFont.drawStringWithShadow(str, this.getX() + 21, this.getY() + count * 20 + 1, Color.WHITE.getRGB());
                timeFont.drawStringWithShadow(Potion.getDurationString(effect), this.getX() + 21, this.getY() + count * 20 + 10, Color.WHITE.darker().getRGB());
            }
            this.setHeight(20 * activePotions.size());
            this.setWidth(80);
        }




    }
}
