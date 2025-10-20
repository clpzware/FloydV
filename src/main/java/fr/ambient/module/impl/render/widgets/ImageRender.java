package fr.ambient.module.impl.render.widgets;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class ImageRender extends Module {
    public ImageRender() {
        super(110, ModuleCategory.RENDER);
        registerProperties(men, women, width, height,onlyInGUI,animation);
        this.setDraggable(true);
        this.setX(500);
        this.setY(200);
        this.setWidth(100);
        this.setHeight(100);
    }

    private Animation animatioFn = new Animation(Easing.EASE_IN_OUT_SINE, 1000);

    private boolean bs = false;

    private final ModeProperty men = ModeProperty.newInstance("Men", new String[]{"No Men :(", "Astolfo :3", "Venti :3","Litican", "No Men but maybe women ?"}, "No Men :(");
    private final ModeProperty women = ModeProperty.newInstance("Women", new String[]{"Hutao :3", "Slight Scales Reference :3"}, "Hutao :3", () -> men.is("No Men but maybe women ?"));

    private final NumberProperty width = NumberProperty.newInstance("Width", 10f, 300f, 1000f, 10f);
    private final NumberProperty height = NumberProperty.newInstance("Height", 10f, 300f, 1000f, 10f);

    private BooleanProperty onlyInGUI = BooleanProperty.newInstance("Only in GUI", false);

    private BooleanProperty animation = BooleanProperty.newInstance("Animation", false);

    private int chc = 0;


    public void onEnable(){
        chc++;
    }

    @SubscribeEvent(EventPriority.VERY_HIGH)
    private void onRender2D(Render2DEvent event){

        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        GlStateManager.enableBlend();

        this.setHeight(height.getValue());
        this.setWidth(width.getValue());


        float hV = height.getValue();

        if(animation.getValue()){
            animatioFn.run(bs ? 1f : 0f);

            if(animatioFn.isFinished()){
                bs = !bs;
            }

            hV = (float) (hV - (height.getValue() * (animatioFn.getValue() * 0.8f)));

        }

        if(onlyInGUI.getValue() && mc.currentScreen == null){
            return;
        }

        switch (men.getValue()){
            case "Venti :3" -> {
                RenderUtil.drawImage(new ResourceLocation("dogclient/men/badaiim2.png"), getX(), getY(), getWidth(), hV);
            }
            case "Astolfo :3" -> {
                if(chc % 2 == 0){
                    RenderUtil.drawImage(new ResourceLocation("dogclient/men/badaiim.png"),  getX(), getY(), getWidth(), hV);
                }else {
                    RenderUtil.drawImage(new ResourceLocation("dogclient/men/badaiim3.png"),  getX(), getY(), getWidth(), hV);
                }

            }
            case "Litican" -> {
                RenderUtil.drawImage(new ResourceLocation("dogclient/men/tittycane.png"),  getX(), getY(), getWidth(), hV);
            }
            case "No Men but maybe women ?" -> {
                switch (women.getValue()){
                    case "Hutao :3" -> {
                        RenderUtil.drawImage(new ResourceLocation("dogclient/women/notbadaiim.png"),  getX(), getY(), getWidth(), hV);
                    }
                    case "Slight Scales Reference :3" -> {
                        RenderUtil.drawImage(new ResourceLocation("dogclient/women/scales.png"),  getX(), getY(), getWidth(), hV    );
                    }
                }
            }
        }
        GlStateManager.disableBlend();

    }
}
