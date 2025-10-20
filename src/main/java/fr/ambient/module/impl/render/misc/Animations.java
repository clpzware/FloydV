package fr.ambient.module.impl.render.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.EntityRenderEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import org.lwjglx.input.Mouse;

public class Animations extends Module {
    public static final ModeProperty mode = ModeProperty.newInstance("Block Mode", new String[]{"1.8","XIV","1.7", "Smooth", "Exhi", "Spin", "Spin2", "Spin3", "Spin4", "Shove", "Moof", "Legit", "Clean", "Sigma","Stab","Jello"}, "1.8");
    public static final NumberProperty animationSpeed = NumberProperty.newInstance("Animation Speed", 0.1f, 1f, 2f, 0.1f);
    public static final NumberProperty x = NumberProperty.newInstance("X", -1.0f, 0.0f, 1.0f, 0.05f);
    public static final NumberProperty y = NumberProperty.newInstance("Y", -1.0f, 0.0f, 1.0f, 0.05f);
    public static final NumberProperty z = NumberProperty.newInstance("Z", -1.0f, 0.0f, 1.0f, 0.05f);
    public static final NumberProperty scale = NumberProperty.newInstance("Scale", 0.2f, 1f, 1.5f, 0.05f);
    private final BooleanProperty animbs = BooleanProperty.newInstance("1.7", true);
    public final BooleanProperty anyItem = BooleanProperty.newInstance("Any Item AB", false);
    private final BooleanProperty anim = BooleanProperty.newInstance("Afk Animations", false);
    public final BooleanProperty removeEquip = BooleanProperty.newInstance("Remove equip animation", false);

    public Animations() {
        super(40, "Modifies the block animation and positioning.", ModuleCategory.RENDER);
        this.registerProperties(mode, animationSpeed, x, y, z, scale, anim, anyItem, animbs,removeEquip);
        this.setSuffix(mode::getValue);
    }

    @Override
    protected void onEnable() {
        super.onDisable();
        mode.setValue("1.8");
    }

    private int inactiveTicks = 0;
    private int lastTick = 0;
    private int prev = 0;

    @SubscribeEvent
    private void onPlayerNetworkTickEvent(PreMotionEvent event) {
        lastTick = inactiveTicks;

        if (anim.getValue()) {
            if (!MoveUtil.moving() && Mouse.getDX() == 0) {
                inactiveTicks++;
            } else {
                if (inactiveTicks > 10) {
                    mc.gameSettings.thirdPersonView = prev;
                }
                inactiveTicks = 0;
            }
        }
        if(animbs.getValue() && Mouse.isButtonDown(0) && mc.thePlayer.inventory.getCurrentItem() != null && (mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemFood || mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemPotion || (mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) || mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBow) && mc.thePlayer.isUsingItem()){
            mc.thePlayer.swingItemClientSide();
        }
    }

    @SubscribeEvent
    private void onEntityRenderEvent(EntityRenderEvent event) {
        if (anim.getValue() && inactiveTicks > 100) {
            float smooth = lastTick + (inactiveTicks - lastTick) * mc.timer.renderPartialTicks;
            double sinVal = Math.sin(Math.toRadians(smooth));
            mc.gameSettings.thirdPersonView = 2;

            event.setYaw(smooth);
            event.setPitch(sinVal * 20);
        } else {
            prev = mc.gameSettings.thirdPersonView;
        }
    }
}
