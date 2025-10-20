package fr.ambient.module.impl.combat.velocity;

import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;


public class DelayVelocity extends ModuleMode {
    public DelayVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    public ArrayList<Packet> delayPackets = new ArrayList<>();
    public boolean shouldDelay = false;

    @SubscribeEvent(EventPriority.HIGH)
    private void onPacketReceiveEvent(PacketReceiveEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity s12PacketEntityVelocity) {
            if (s12PacketEntityVelocity.getEntityID() == mc.thePlayer.getEntityId()) {

                if (!mc.thePlayer.onGround) {
                    event.setCancelled(true);
                    delayPackets = new ArrayList<>();
                    delayPackets.add(event.getPacket());
                    shouldDelay= true;
                }else{
                    event.setCancelled(false);
                }
            }
        }
        if(event.getPacket() instanceof S32PacketConfirmTransaction s32PacketConfirmTransaction){
            if(shouldDelay){
                event.setCancelled(true);
                delayPackets.add(event.getPacket());
            }
        }

    }

    @SubscribeEvent
    private void onTickEvent(UpdateEvent event){
        if(mc.thePlayer.onGround){
            shouldDelay = false;

            delayPackets.forEach(e->{
                try {
                    e.processPacket(mc.getNetHandler());
                }catch (Exception ef){

                }
            });

            delayPackets.clear();
        }



    }
}