package fr.ambient.module.impl.player;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.KillAura;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.player.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.MathHelper;

public class AntiFireBall extends Module {
    public AntiFireBall() {
        super(20,"Automatically knocks incoming fireballs away from you.", ModuleCategory.PLAYER);
        this.registerProperty(rotate);
    }


    private BooleanProperty rotate = BooleanProperty.newInstance("Rotate", true);

    @SubscribeEvent
    private void onEventPre(PreMotionEvent event) {
        if(rotate.getValue()){
            return;
        }
        if(KillAura.target != null){
            return;
        }
        if(Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()){
            return;
        }
        if(Ambient.getInstance().getModuleManager().getModule(Breaker.class).isEnabled()){
            return;
        }


        for (Entity e : mc.theWorld.loadedEntityList) {
            if (e instanceof EntityLargeFireball && PlayerUtil.getBiblicallyAccurateDistanceToEntity(e) <= 6 && !Ambient.getInstance().getModuleManager().getModule(LongJump.class).isEnabled()) {
                float[] rotations = RotationUtil.getRotation(e);
                event.setYaw(rotations[0]);
                event.setPitch(rotations[1]);
                mc.thePlayer.renderPitchHead = rotations[1];
                mc.thePlayer.rotationYawHead = MathHelper.wrapAngleTo180_float(rotations[0]);
                mc.thePlayer.renderYawOffset = MathHelper.wrapAngleTo180_float(rotations[0]);
                mc.thePlayer.prevRenderYawOffset = mc.thePlayer.renderYawOffset;

            }
        }
    }

    @SubscribeEvent
    private void onEventPre(UpdateEvent event) {
        if(KillAura.target != null){
            return;
        }
        for (Entity e : mc.theWorld.loadedEntityList) {
            if (e instanceof EntityLargeFireball && PlayerUtil.getBiblicallyAccurateDistanceToEntity(e) <= 6) {
                PacketUtil.sendPacket(new C02PacketUseEntity(e, C02PacketUseEntity.Action.ATTACK));
            }
        }
    }

}
