package fr.ambient.module.impl.combat;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.util.player.RotationUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;

import java.util.Comparator;
import java.util.List;

public class SumoBot extends Module {
    public EntityPlayer target = null;
    public boolean hasGameStarted = false;
    public SumoBot() {
        super(5,"Automates movement and fighting within sumo duels.", ModuleCategory.COMBAT);
    }
    @SubscribeEvent
    public void onMovementInput(MoveInputEvent event){
        if(hasGameStarted){
            if(target != null){
                if(target.getDistanceToEntity(mc.thePlayer) > 2){
                    event.setForward(1f);
                }
            }
        }
    }
    @SubscribeEvent
    public void onEventUpdate(UpdateEvent event){
        if(hasGameStarted){
            if(mc.thePlayer.ticksExisted % 2 == 0 && target.getDistanceToEntity(target) < 4){
                mc.clickMouse();
            }
            if(mc.thePlayer.hurtTime == 8){
                mc.thePlayer.motionX *= 0.6f;
                mc.thePlayer.motionZ *= 0.6f;
            }
        }
    }
    @SubscribeEvent
    public void onEventPre(PreMotionEvent event){
        if(mc.thePlayer.ticksExisted < 5){
            hasGameStarted = false;
        }
        if(!hasGameStarted){
            return;
        }
        List<EntityPlayer>possibilities = mc.theWorld.playerEntities
                .stream()
                .filter(entity -> {
                    if (entity.isDead || entity.deathTime != 0)
                        return false;
                    return mc.thePlayer != entity;
                })
                .filter(entity -> mc.thePlayer.getDistanceToEntity(entity) <= 16)
                .sorted(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)))
                .toList();
        target = possibilities.stream()
                .findFirst()
                .orElse(null);
        if(target != null){
            float[] rota = RotationUtil.getRotation(target);
            mc.thePlayer.rotationYaw = MathHelper.wrapAngleTo180_float(rota[0]);
            mc.thePlayer.rotationPitch = rota[1];
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketReceiveEvent event){
        if(event.getPacket() instanceof S02PacketChat c01PacketChatMessage){
            if(EnumChatFormatting.getTextWithoutFormattingCodes(c01PacketChatMessage.getChatComponent().getUnformattedText()).contains("Eliminate your")){
                hasGameStarted = true;
            }
            if(c01PacketChatMessage.getChatComponent().getUnformattedText().toLowerCase().contains("melee accuracy")){
                mc.thePlayer.sendChatMessage("/play duels_sumo_duel");
                hasGameStarted = false;
            }
        }
    }
}
