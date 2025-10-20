package fr.ambient.module.impl.combat;

import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.ReceivePacketEventFirst;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.GameRenderLoopEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.event.impl.world.TickEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.Property;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.CompositeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.TimePacket;
import fr.ambient.util.player.PlayerUtil;
import fr.ambient.util.render.model.ESPUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;


import java.awt.*;
import java.util.ArrayList;

public class BackTrack extends Module {
    public BackTrack() {
        super(76, "Allows you to hit a players prior position.", ModuleCategory.COMBAT);
        this.registerProperties(delay,smooth,smoothed);
    }

    private final NumberProperty delay = NumberProperty.newInstance("Delay", 0f, 200f, 5000f, 1f);

    private BooleanProperty smooth = BooleanProperty.newInstance("Smoothing", false);


    private final BooleanProperty maxAdvantageMode = BooleanProperty.newInstance("Distance Relative Ping", false);
    private final NumberProperty maxDelay = NumberProperty.newInstance("Max Delay", 0f, 200f, 5000f, 1f);
    private final NumberProperty minDelay = NumberProperty.newInstance("Min Delay", 0f, 20f, 5000f, 1f);

    private final NumberProperty maxPingVariation = NumberProperty.newInstance("Max Ping Variation", 0f, 20f, 5000f, 1f);


    private CompositeProperty smoothed = CompositeProperty.newInstance("Smoothies", new Property[]{maxAdvantageMode,maxDelay, minDelay, maxPingVariation}, ()->smooth.getValue());

    private final ArrayList<TimePacket> packets = new ArrayList<>();

    private EntityLivingBase target = null;
    private int delayVal = 0;


    private boolean canBackTrack = false;
    public void onEnable(){
        packets.clear();
    }
    public void onDisable(){
        packets.forEach(e->{
            onHandle(e.getPacket());
        });
        packets.clear();
    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        target = null;

        if(mc.pointedEntity instanceof EntityLivingBase livingBase){
            target = livingBase;
        }
        if(KillAura.target != null){
            target = KillAura.target;
        }
        canBackTrack = target != null && mc.currentScreen == null && mc.thePlayer.ticksExisted > 20 && !mc.isSingleplayer();

        if(!smooth.getValue()){
            if(canBackTrack){
                delayVal = delay.getValue().intValue();
            }else{
                delayVal = 0;
            }
            return;
        }

        if(!canBackTrack){
            if(delayVal > maxPingVariation.getValue()){
                delayVal = (int) Math.clamp(delayVal - maxPingVariation.getValue(), 0, delayVal);
            }else{
                delayVal = 0;
            }
        }else{
            if(maxAdvantageMode.getValue()){
                AxisAlignedBB maxHB =new AxisAlignedBB(
                        target.realPos.xCoord - target.width / 2,
                        target.realPos.yCoord,
                        target.realPos.zCoord - target.width / 2,

                        target.realPos.xCoord + target.width / 2,
                        target.realPos.yCoord + target.height,
                        target.realPos.zCoord + target.width / 2
                );

                if(PlayerUtil.getClosestPointToBoundingBox(maxHB).distanceTo(new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ)) > 3){
                    if(delayVal + 50 <= maxDelay.getValue()){
                        delayVal+=50;
                    }
                }else{
                    if(delayVal - maxPingVariation.getValue() > minDelay.getValue()){
                        delayVal -= maxPingVariation.getValue();
                    }
                }

            }else{

            }
        }

    }

    @SubscribeEvent
    private void gameLoopEvent(TickEvent event) {
        synchronized (packets) {
            if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
                packets.clear(); // Only clear packets on world load, don't process them
                return;
            }

            if (!packets.isEmpty()) {
                if (canBackTrack) {
                    ArrayList<TimePacket> toRem = new ArrayList<>();
                    for (TimePacket packet : packets) {
                        if ((packet.getTime() + delayVal) < System.currentTimeMillis()) {
                            toRem.add(packet);
                            onHandle(packet.getPacket());
                        }
                    }
                    packets.removeAll(toRem);
                } else {
                    packets.forEach(e -> onHandle(e.getPacket()));
                    packets.clear();
                }
            }
        }
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent event){
        if(target != null){
            ESPUtil.drawFilledHitbox(ESPUtil.getESPFromVec3(target.realPos.interpolateWith(target.lastRealPos, mc.timer.renderPartialTicks), target.width / 2, target.height), this.target != null ? Color.RED : Color.GREEN, 0.2f);
        }
    }


    private void onHandle(Packet packet){
        try {
            PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(packet);
            Ambient.getInstance().getEventBus().post(packetReceiveEvent);
            if(packetReceiveEvent.isCancelled()){
                return;
            }
            packet.processPacket(mc.getNetHandler());
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @SubscribeEvent(value = EventPriority.HIGH)
    public void onEvent(ReceivePacketEventFirst event) {
        if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 10) {
            return;
        }

        event.setCancelled(true);
        packets.add(new TimePacket(System.currentTimeMillis(), event.getPacket()));

        if (event.getPacket() instanceof S18PacketEntityTeleport entityTeleport) {
            handleEntityTeleportVisual(entityTeleport);
        }
        if (event.getPacket() instanceof S14PacketEntity s14PacketEntity) {
            handleEntityMovementVisual(s14PacketEntity);
        }
    }


    public void handleEntityTeleportVisual(S18PacketEntityTeleport packetIn) {
        Entity entity = mc.theWorld.getEntityByID(packetIn.getEntityId());
        if (entity != null) {
            entity.realServerPos = new Vec3(packetIn.getX(), packetIn.getY(), packetIn.getZ());
            double d0 = entity.realServerPos.xCoord / 32.0D;
            double d1 = entity.realServerPos.yCoord / 32.0D;
            double d2 = entity.realServerPos.zCoord / 32.0D;
            entity.lastRealPos = entity.realPos;
            entity.realPos = new Vec3(d0, d1, d2);

        }
    }
    public void handleEntityMovementVisual(S14PacketEntity packetIn) {
        Entity entity = packetIn.getEntity(mc.theWorld);
        if (entity != null) {
            entity.realServerPos.xCoord += packetIn.func_149062_c();
            entity.realServerPos.yCoord += packetIn.func_149061_d();
            entity.realServerPos.zCoord += packetIn.func_149064_e();
            double d0 = entity.realServerPos.xCoord / 32.0D;
            double d1 = entity.realServerPos.yCoord / 32.0D;
            double d2 = entity.realServerPos.zCoord / 32.0D;
            entity.lastRealPos = entity.realPos;
            entity.realPos = new Vec3(d0, d1, d2);
        }
    }

}
