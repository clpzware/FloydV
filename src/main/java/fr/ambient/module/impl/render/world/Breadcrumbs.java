package fr.ambient.module.impl.render.world;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.render.model.ESPUtil;
import net.minecraft.util.Vec3;

import java.util.ArrayList;

public class Breadcrumbs extends Module {
    public Breadcrumbs() {
        super(86, "Breadcrumbs", ModuleCategory.RENDER);
        this.registerProperties(style, amount, width, spacing);
    }

    private final ModeProperty style = ModeProperty.newInstance("Style", new String[]{"Line", "Orbs"}, "Line");
    private final NumberProperty amount = NumberProperty.newInstance("Amount", 1f, 15f, 40f, 1f);
    private final NumberProperty width = NumberProperty.newInstance("Width", 1f, 1.5f, 3f, 0.1f);
    private final NumberProperty spacing = NumberProperty.newInstance("Spacing", 1f, 2f, 6f, 1f, () -> style.is("Orbs"));

    private final ArrayList<Vec3> cc = new ArrayList<>();

    @SubscribeEvent
    private void onEvent(PreMotionEvent e){
        if (!style.is("Orbs") || mc.thePlayer.ticksExisted % spacing.getValue().intValue() == 0) {
            cc.add(new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY + 0.1f, mc.thePlayer.lastTickPosZ));
            if (cc.size() > amount.getValue()) {
                cc.removeFirst();
            }
        }
    }

    @SubscribeEvent
    private void onRender(Render3DEvent event){
        if(!cc.isEmpty()){
            if (style.is("Line"))
                ESPUtil.drawPathLine(cc, width.getValue());
            else if (style.is("Orbs"))
                ESPUtil.drawPathOrbs(cc, width.getValue());
        }
    }

    public void onEnable(){
        cc.clear();
    }
}
