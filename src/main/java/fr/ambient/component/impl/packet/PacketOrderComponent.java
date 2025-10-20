package fr.ambient.component.impl.packet;

import fr.ambient.component.Component;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.network.PacketSendEventFinal;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

public class PacketOrderComponent extends Component {

    public static boolean canSendSprint = false;
    public static boolean packetSideSprinting = false;

    public static boolean hasSentC09 = false;


    @SubscribeEvent(EventPriority.HIGH)
    private void oNPacketRecieveEvent(PacketReceiveEvent event) {
        if(event.getPacket() instanceof S32PacketConfirmTransaction){
            for(Entity entity : mc.theWorld.loadedEntityList){
                entity.playerTransactionHitboxes.add(entity.getEntityBoundingBox());
            }
        }

    }

    @SubscribeEvent
    private void onPacketSendEvent(PacketSendEventFinal event) {
        if (mc.isIntegratedServerRunning())
            return;

        if(event.packet instanceof C0BPacketEntityAction c0BPacketEntityAction){
            canSendSprint = false;


            if(c0BPacketEntityAction.getAction() == C0BPacketEntityAction.Action.START_SPRINTING || c0BPacketEntityAction.getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING){
                boolean bbs = niggerMethod(c0BPacketEntityAction);

                if(bbs != packetSideSprinting){
                    packetSideSprinting = bbs;
                }else {
                    event.setCancelled(true);
                }
            }
        }
        if(event.getPacket() instanceof C09PacketHeldItemChange){
            hasSentC09 = true;
        }
        if(event.getPacket() instanceof C03PacketPlayer){
            canSendSprint = true;
            hasSentC09 = false;
        }

    }

    public boolean niggerMethod(C0BPacketEntityAction c0BPacketEntityAction){
        switch (c0BPacketEntityAction.getAction()){
            case START_SPRINTING -> {
                return true;
            }
            case STOP_SPRINTING -> {
                return false;
            }
        }


        return false;
    }


}
