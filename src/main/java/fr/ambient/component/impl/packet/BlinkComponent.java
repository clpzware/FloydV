package fr.ambient.component.impl.packet;

import fr.ambient.component.Component;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketSendEventFinal;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlinkComponent extends Component {

    private static final List<Packet<?>> packetList = new ArrayList<>();
    private static final Set<Class<?>> nonCancelablePackets = Set.of(
            C01PacketChatMessage.class,
            C14PacketTabComplete.class,
            C01PacketEncryptionResponse.class,
            C01PacketPing.class,
            C00PacketLoginStart.class,
            C00PacketServerQuery.class,
            C00Handshake.class
    );
    public static boolean isBlinking = false;
    public static boolean specialMode = false;
    public static boolean isResetBlink = false;

    public static void onEnable() {
        isBlinking = true;
    }

    public static void onDisable() {

        isBlinking = false;

        if (mc.isIntegratedServerRunning())
            return;

        isResetBlink = true;
        packetList.forEach(PacketUtil::sendPacketNoEvent);
        isResetBlink = false;
        packetList.clear();
    }

 //   @SubscribeEvent
  //  private void onRender2D(Render2DEvent event){
     //   if(isBlinking){
   //         mc.fontRendererObj.drawString("blink " + packetList.size(), 5, 150, Color.WHITE.getRGB());
   // //    }
  //  }

    @SubscribeEvent
    private void onPacketSendEvent(PacketSendEventFinal event) {
        if (mc.isIntegratedServerRunning())
            return;

        if (isBlinking && !event.isCancelled()) {
            if(nonCancelablePackets.contains(event.getPacket().getClass())){
                return;
            }
            event.setCancelled(true);
            packetList.add(event.getPacket());
        }

        if (mc.thePlayer == null) {
            onDisable();
        }else{
            if(event.getPacket() instanceof C0FPacketConfirmTransaction){
                for(Entity entity : mc.theWorld.loadedEntityList){
                    entity.playerTransactionHitboxes.remove(0);
                }
            }
        }
    }

}
