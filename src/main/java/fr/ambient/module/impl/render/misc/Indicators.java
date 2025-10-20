package fr.ambient.module.impl.render.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.render.RenderUtil;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Indicators extends Module {

    private NumberProperty radiusSetting = NumberProperty.newInstance("Radius", 30f, 100f, 200f, 5f);
    private BooleanProperty directionCheck = BooleanProperty.newInstance("Direction Check", true);

    public Indicators() {
        super(84, "Renders incoming projectiles on your screen", ModuleCategory.RENDER);
        registerProperties(radiusSetting, directionCheck);
    }

    private final List<Indicator> indicators = new ArrayList<>();

    @Override
    protected void onEnable() {
        indicators.clear();
        super.onEnable();
    }

    @SubscribeEvent
    public void onUpdate(UpdateEvent e) {
        List<Entity> entities = mc.theWorld.loadedEntityList;
        double playerX = mc.thePlayer.posX, playerZ = mc.thePlayer.posZ;

        for (Entity entity : entities) {
            double distance = mc.thePlayer.getDistanceToEntity(entity);
            String entityName = entity.getName();

            if (distance != 0 && (entity instanceof EntityEnderPearl || entity instanceof EntityArrow || entity instanceof EntityFireball)) {
                double x = entity.posX;
                double z = entity.posZ;
                double prevX = entity.prevPosX;
                double prevZ = entity.prevPosZ;
                double motionX = x - prevX;
                double motionZ = z - prevZ;
                double direction = motionX * (playerX - x) + motionZ * (playerZ - z);

                if (direction > 0 || !directionCheck.getValue()) {
                    int dist = (int) Math.floor(Math.sqrt(distance));
                    boolean isDuplicate = false;

                    for (Indicator indicator : indicators) {
                        if (indicator.entity() == entity) {
                            isDuplicate = true;
                            break;
                        }
                    }

                    if (!isDuplicate && dist < 50) {
                        indicators.add(new Indicator(entity, entityName, dist));
                    }
                }
            }
        }

        Iterator<Indicator> iterator = indicators.iterator();
        while (iterator.hasNext()) {
            Indicator indicator = iterator.next();
            Entity entity = indicator.entity();
            double x = entity.posX;
            double z = entity.posZ;
            double prevX = entity.prevPosX;
            double prevZ = entity.prevPosZ;
            double distance = mc.thePlayer.getDistanceToEntity(entity);

            if (distance > 75 || distance == 0 || x - prevX == 0 || z - prevZ == 0 || entity.isDead) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public void onRender2D(Render2DEvent event) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        indicators.sort(Comparator.comparingDouble(indicator -> mc.thePlayer.getDistanceToEntity(indicator.entity())));

        double playerPosX = mc.thePlayer.posX;
        double playerPosZ = mc.thePlayer.posZ;
        float playerYaw = mc.thePlayer.rotationYaw;

        float radius = radiusSetting.getValue();
        float centerX = scaledResolution.getScaledWidth() / 2f;
        float centerY = scaledResolution.getScaledHeight() / 2f;
        float textOffset = 18f;

        for (Indicator indicator : indicators) {
            Entity entity = indicator.entity();
            double entityPosX = entity.posX;
            double entityPosZ = entity.posZ;

            float yawRadians = (float) Math.toRadians(getRotations(entityPosX, entityPosZ, playerPosX, playerPosZ) - playerYaw);
            int distance = (int) Math.floor(mc.thePlayer.getDistanceToEntity(entity));

            float[] itemPosition = getPosition((float) Math.toDegrees(yawRadians), radius);
            float itemX = centerX + itemPosition[0];
            float itemY = centerY + itemPosition[1];

            float[] textPosition = getPosition((float) Math.toDegrees(yawRadians), radius + textOffset);
            float textX = centerX + textPosition[0];
            float textY = centerY + textPosition[1];

            String distanceText = distance + "m";
            drawText(textX, textY, distanceText);

            if (entity instanceof EntityFireball)
                drawItem(itemX, itemY, new ItemStack(Items.fire_charge));
            if (entity instanceof EntityEnderPearl)
                drawItem(itemX, itemY, new ItemStack(Items.ender_pearl));
            if (entity instanceof EntityArrow)
                drawItem(itemX, itemY, new ItemStack(Items.arrow));

            drawArrow(centerX, centerY, (float) Math.toDegrees(yawRadians));
        }
    }

    private void drawText(float x, float y, String text) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0.0);
        int textHalfWidth = mc.fontRendererObj.getStringWidth(text) / 2;
        mc.fontRendererObj.drawString(text, -textHalfWidth, -mc.fontRendererObj.FONT_HEIGHT / 2f, -1);
        GL11.glPopMatrix();
    }

    private void drawItem(float x, float y, ItemStack itemStack) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0.0);
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, -8, -8);
        GL11.glPopMatrix();
    }

    private void drawArrow(float x, float y, float yaw) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, 0.0);
        GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
        RenderUtil.renderArrow(-5f, -radiusSetting.getValue() - 33, Color.WHITE, 2, 10);
        GL11.glPopMatrix();
    }

    public float[] getPosition(float angle, double radius) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }

        double angleRadians = Math.toRadians(angle - 90);

        float x = (float) (Math.cos(angleRadians) * radius);
        float y = (float) (Math.sin(angleRadians) * radius);

        return new float[]{x, y};
    }

    record Indicator(Entity entity, String name, int distance) {}

    public float getRotations(double eX, double eZ, double x, double z) {
        double deltaX = eX - x;
        double deltaZ = eZ - z;
        double yaw = -(Math.atan2(deltaX, deltaZ) * 57.29577951308232);
        return (float) yaw;
    }
}
