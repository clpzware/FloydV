package fr.ambient.module.impl.misc;

import com.mojang.authlib.GameProfile;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.font.Fonts;
import net.minecraft.entity.player.EntityPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MCF extends Module {
    public MCF() {
        super(83, "MCF", ModuleCategory.MISC);
        this.registerProperties(mode,doShow);
        this.setDraggable(true);
        this.setX(10);
        this.setY(100);
        this.setWidth(100);
        this.setHeight(50);
    }


    public TimeUtil timeUtil = new TimeUtil();

    public ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Friend", "Target"}, "Friend");
    public BooleanProperty doShow = BooleanProperty.newInstance("Do Show", true);

    public ArrayList<UUID> friendKeys = new ArrayList<>();
    public ArrayList<UUID> targetKeys = new ArrayList<>();
    public HashMap<UUID, String> uuidStringHashMap = new HashMap<>();

    public boolean pressBound = false;


    @SubscribeEvent
    private void onRender2D(Render2DEvent event){

        if(!doShow.getValue()){
            return;
        }

        RenderUtil.drawRoundedRect(this.getX(), this.getY(), this.getWidth(), this.getHeight(), 5, new Color(0xFF373743), new Color(0xFF373743), new Color(0xFF474761), new Color(0xFF474761));
        RenderUtil.drawRoundedRect(this.getX() + 0.5f, this.getY() + 0.5f, this.getWidth() - 1, this.getHeight() - 1, 4.5f, new Color(0xFF19191C), new Color(0xFF19191C), new Color(0xFF101010), new Color(0xFF101010));
        Fonts.getOpenSansBold(16).drawString("MCF - " + mode.getValue(), this.getX() + 5, this.getY() + 5, Color.WHITE.getRGB());

        int maxy = 0;

        if(mode.is("Friend")){
            maxy = friendKeys.size() * 13;

            int incr = 0;
            for(UUID uuid : friendKeys){
                Fonts.getOpenSansBold(14).drawString(uuidStringHashMap.get(uuid), this.getX() + 7, this.getY() + incr + 3 + 16, Color.WHITE.getRGB());
                incr+=13;
            }
        }else{
            maxy = targetKeys.size() * 13;

            int incr = 0;
            for(UUID uuid : targetKeys){
                Fonts.getOpenSansBold(14).drawString(uuidStringHashMap.get(uuid), this.getX() + 7, this.getY() + incr + 16, Color.WHITE.getRGB());
                incr+=13;
            }
        }




        this.setHeight(maxy + 18);




    }



    @SubscribeEvent
    private void onTick(PreMotionEvent event){


        if(mc.gameSettings.keyBindPickBlock.pressed || mc.gameSettings.keyBindAttack.pressed){
            if(pressBound){
                return;
            }else{
                pressBound = true;
            }
        }else{
            pressBound = false;
        }


        if(!timeUtil.finished(250)){
            return;
        }

        EntityPlayer targetPlayer = null;

        if(mc.pointedEntity instanceof EntityPlayer entityPlayer){
            targetPlayer = entityPlayer;
        }else{
            return;
        }

        if(mc.gameSettings.keyBindPickBlock.pressed && mode.is("Friend")){
            if(friendKeys.contains(targetPlayer.getGameProfile().getId())){
                friendKeys.remove(targetPlayer.getGameProfile().getId());
            }else{
                uuidStringHashMap.put(targetPlayer.getGameProfile().getId(), targetPlayer.getGameProfile().getName());
                friendKeys.add(targetPlayer.getGameProfile().getId());
            }
        }
        if(mc.gameSettings.keyBindAttack.pressed && mode.is("Target")){
            if(targetKeys.contains(targetPlayer.getGameProfile().getId())){
                targetKeys.remove(targetPlayer.getGameProfile().getId());
            }else{
                uuidStringHashMap.put(targetPlayer.getGameProfile().getId(), targetPlayer.getGameProfile().getName());
                targetKeys.add(targetPlayer.getGameProfile().getId());
            }
        }


    }


    public boolean isFriend(GameProfile profile){
        return friendKeys.contains(profile.getId()) && mode.is("Friend");
    }
    public boolean isTarget(GameProfile profile){
        return targetKeys.contains(profile.getId()) && mode.is("Target");
    }


}
