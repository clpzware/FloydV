package fr.ambient.component.impl.packet;

import fr.ambient.Ambient;
import fr.ambient.component.Component;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEventFinal;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.render.GameRenderLoopEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.util.PacketTick;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.render.model.ESPUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.Vec3;

import java.util.ArrayList;

public class OutgoingPacketComponent extends Component {


    private final ArrayList<PacketTick> packetTicks = new ArrayList<>();

    private boolean isEnabled = false;
    @Getter
    private Vec3 last, current;

    @Setter
    private int delayMs = 100;

    @Getter
    private final TimeUtil timeSinceLastReset = new TimeUtil();


    public boolean contains08(){
        synchronized (packetTicks){
            for(PacketTick packetTick : packetTicks){
                if(packetTick.getPacket() instanceof C08PacketPlayerBlockPlacement){
                    return true;
                }
            }
        }
        return false;
    }
    public boolean contains02(){
        synchronized (packetTicks){
            for(PacketTick packetTick : packetTicks){
                if(packetTick.getPacket() instanceof C02PacketUseEntity){
                    return true;
                }
            }
        }
        return false;
    }

    public void setEnabled(boolean enabled){
        if(enabled != isEnabled){
            if(enabled){
                last = current = new Vec3(mc.thePlayer.posX, -100000000, mc.thePlayer.posZ); // heheheha
                // nothing happens here ong
            }else{
                synchronized (packetTicks) {
                    packetTicks.forEach(p -> Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEventAbsolute(p.getPacket()));
                    packetTicks.clear();
                    timeSinceLastReset.reset();
                }
            }
            isEnabled = enabled;
        }
    }


    @SubscribeEvent(value = EventPriority.VERY_LOW)
    public void onSendPacketFinalThingy(PacketSendEventFinal eventFinal){
        if(mc.thePlayer == null ){
            return;
        }
        if(eventFinal.isCancelled()){
            return;
        }
        synchronized (packetTicks) {
            if ((isEnabled || !packetTicks.isEmpty()) && !mc.isSingleplayer()) {
                packetTicks.add(new PacketTick(System.currentTimeMillis(), eventFinal.getPacket()));
                eventFinal.setCancelled(true);

            }
        }
    }

    @SubscribeEvent
    private void onTick(UpdateEvent event){
        last = current;

    }

    @SubscribeEvent
    public void onRenderThing(GameRenderLoopEvent event){
        if(isEnabled || !packetTicks.isEmpty()){
            synchronized (packetTicks){
                ArrayList<PacketTick> toDelete = new ArrayList<>();
                for(PacketTick packetTick : packetTicks){
                    try {
                        if((packetTick.getTick() + delayMs) < System.currentTimeMillis()){
                            Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacketNoEventAbsolute(packetTick.getPacket());

                            if(packetTick.getPacket() instanceof C03PacketPlayer c03PacketPlayer){
                                if(c03PacketPlayer.isMoving()){
                                    current = new Vec3(c03PacketPlayer.x, c03PacketPlayer.y, c03PacketPlayer.z);
                                }
                            }


                            toDelete.add(packetTick);
                        }
                    }catch (Exception e){
                        packetTicks.clear();
                        return;
                    }

                }

                for(PacketTick packetTick : toDelete){
                    packetTicks.remove(packetTick);
                }
            }

        }
    }

    @SubscribeEvent
    private void onRender3D(Render3DEvent event){

        if(isEnabled){
            float height = mc.thePlayer.height;
            if(mc.gameSettings.thirdPersonView != 0){
                ESPUtil.drawFilledHitbox(ESPUtil.getESPFromVec3(current.interpolateWith(last, mc.timer.renderPartialTicks), mc.thePlayer.width / 2, height), Ambient.getInstance().getHud().getColor2(), 0.2f);
            }

        }

    }



}
