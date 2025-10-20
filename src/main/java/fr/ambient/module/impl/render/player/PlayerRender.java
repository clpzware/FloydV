package fr.ambient.module.impl.render.player;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.render.animation.Animation;
import fr.ambient.util.render.animation.Easing;
import fr.ambient.util.render.model.ESPUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class PlayerRender extends Module {
    public PlayerRender() {
        super(66, "Alters the size or appearance of players on your screen.", ModuleCategory.RENDER);
        this.registerProperties(damageIndicators,hitmarker,noarmor, scale, xMult, yMult, zMult);
    }

    public BooleanProperty damageIndicators = BooleanProperty.newInstance("Damage Indicators", false);

    public BooleanProperty hitmarker = BooleanProperty.newInstance("Hit Markers", false);


    public ArrayList<Indicator> indicators = new ArrayList<>();
    public ArrayList<HitMarker> hitmarkers = new ArrayList<>();

    public BooleanProperty scale = BooleanProperty.newInstance("Scale Players", false);
    public BooleanProperty noarmor = BooleanProperty.newInstance("No Armor", false);
    public final NumberProperty xMult = NumberProperty.newInstance("X Multiplier", 0.1f, 1f, 10f, 0.1f, ()->scale.getValue());
    public final NumberProperty yMult = NumberProperty.newInstance("Y Multiplier", 0.1f, 1f, 10f, 0.1f, ()->scale.getValue());
    public final NumberProperty zMult = NumberProperty.newInstance("Z Multiplier", 0.1f, 1f, 10f, 0.1f, ()->scale.getValue());

    @SubscribeEvent
    private void onRender2D(Render3DEvent event) {
        if(damageIndicators.getValue()){
            for(Indicator indicator : indicators){
                indicator.render();
            }
        }
        if(hitmarker.getValue()){
            for(HitMarker hitMarker : hitmarkers){
                hitMarker.render();
            }
        }
    }



    private float yaw = 0, pitch = 0;


    @SubscribeEvent
    private void sendPacket(PacketSendEvent event){
        if(event.getPacket() instanceof C03PacketPlayer c03PacketPlayer){
            if(c03PacketPlayer.getRotating()){
                yaw = c03PacketPlayer.getYaw();
                pitch = c03PacketPlayer.getPitch();
            }
        }


        if(hitmarker.getValue()){


            if(event.getPacket() instanceof C02PacketUseEntity c02PacketUseEntity){

                if(c02PacketUseEntity.getAction() == C02PacketUseEntity.Action.ATTACK){
                    try {
                        float distance = PlayerUtil.getBiblicallyAccurateDistanceToEntity(c02PacketUseEntity.getEntityFromWorld(mc.theWorld));
                        Vec3 vec31 = mc.thePlayer.getVectorForRotation(pitch, yaw);
                        Vec3 vec3 = mc.thePlayer.getPositionEyes(mc.timer.renderPartialTicks);
                        Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
                        hitmarkers.add(new HitMarker(vec32.xCoord,
                                vec32.yCoord,
                                vec32.zCoord,
                                new Animation(Easing.EASE_IN_OUT_QUAD, 500)));
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @SubscribeEvent
    private void onUpdate(UpdateEvent event) {
        if (damageIndicators.getValue()){
            for(Entity entity : mc.theWorld.loadedEntityList){
                if(entity instanceof EntityLivingBase player){
                    if(player.lastHealth > player.getHealth()){
                        indicators.add(new Indicator(new DecimalFormat("-#.##").format(player.lastHealth - player.getHealth()), (float) ((float) player.posX + player.motionX), (float) (player.posY + 2), (float) ((float) player.posZ + player.motionZ),new Animation(Easing.EASE_IN_OUT_QUAD, 500)));
                    }
                    player.lastHealth = player.getHealth();
                }

            }
            ArrayList<Indicator> toRem = new ArrayList<>();
            for(Indicator indicator : indicators){
                if(indicator.animation.isFinished()){
                    toRem.add(indicator);
                }
            }
            for(Indicator indicator : toRem){
                indicators.remove(indicator);
            }
        }


        if(hitmarker.getValue()){
            ArrayList<HitMarker> toRemove = new ArrayList<>();
            for(HitMarker hitMarker : hitmarkers){
                if(hitMarker.animation.isFinished()){
                    toRemove.add(hitMarker);
                }
            }
            for(HitMarker hitMarker : toRemove){
                hitmarkers.remove(hitMarker);
            }
        }



    }

    @AllArgsConstructor
    @Getter
    @Setter
    public class Indicator {
        private String text;
        private float x,y,z;

        public Animation animation;
        public void render(){
            animation.run(1);


            float x = (float) (this.x - RenderManager.viewerPosX);
            float y = (float) (this.y + (float) animation.getValue() - RenderManager.viewerPosY);
            float z = (float) (this.z - RenderManager.viewerPosZ);

            GlStateManager.pushMatrix();
            GlStateManager.translate((float) x, (float) y, (float) z);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            GlStateManager.scale(-0.022f, -0.022f, -0.022f);
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            mc.fontRendererObj.drawString(text, (float) (-mc.fontRendererObj.getStringWidth(text) / 2), -3.0f, -1, true);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public class HitMarker{
        private double x,y,z;
        public Animation animation;
        public void render(){
            animation.run(1);
            ESPUtil.point(new Vec3(x,y,z), new Color(
                    Ambient.getInstance().getHud().getCurrentTheme().color1.getRed(),
                    Ambient.getInstance().getHud().getCurrentTheme().color1.getGreen(),
                    Ambient.getInstance().getHud().getCurrentTheme().color1.getBlue(),
                    255 - (int) (animation.getValue() * 255)
            ), 0.05f);
        }

    }

}
