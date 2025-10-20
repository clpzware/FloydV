package fr.ambient.util;

import fr.ambient.Ambient;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.player.ChatUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

import java.util.Random;


@Getter
public class ClickUtil {

    private TimeUtil timeSinceLastClick = new TimeUtil();

    @Setter private float maxCps = 16;
    @Setter private float minCps = 12;

    @Setter private boolean usePattern = false;

    @Setter private float sinLoopAmount = 3f;

    @Setter private double sinLoopDivisor = 500D;

    private int pat = 0;

    /*private float exhaustBuffer = 35;

    private float exhaustAmount = 0;*/

    private long delay = 0;


    public Random random = new Random(1337);

    public FastNoiseLite fastNoiseLite = new FastNoiseLite();


    public boolean tick(EntityLivingBase target){
        if(target == null){
            // no target, just clicking ig; dont care <3 :3





        }
        return false;
    }


    public int isAbleToClick(){
        int clickCount = 0;

        boolean canClick = timeSinceLastClick.finished(delay);

        if(!usePattern){

            float sin = (float) (Math.sin(System.currentTimeMillis() / sinLoopDivisor) * sinLoopAmount) + 0.5f;

            if(canClick){
                float fnl = fastNoiseLite.GetNoise(Minecraft.getMinecraft().thePlayer.ticksExisted, Minecraft.getMinecraft().thePlayer.ticksExisted);
                delay = (long) (750 / (minCps + (maxCps - minCps) * 1.5f * sin));
                clickCount++;
                timeSinceLastClick.reset();
            }
        }else{
            if(Ambient.getInstance().getClickPatternComponent().isRecording){
                ChatUtil.display("You are recording ! do .clickpattern stoprec to stop !");
                return 0;
            }

            if(Ambient.getInstance().getClickPatternComponent().clickTimeMS.isEmpty()){
                ChatUtil.display("You dont have any click patterns ! ( load one or record one ig )");
                return 0;
            }
            if(canClick){
                if(pat >= Ambient.getInstance().getClickPatternComponent().clickTimeMS.size()){
                    pat = 0;
                }
                delay = Ambient.getInstance().getClickPatternComponent().clickTimeMS.get(pat);
                timeSinceLastClick.reset();
                clickCount++;
                pat++;
                while (delay == 0){
                    if(pat >= Ambient.getInstance().getClickPatternComponent().clickTimeMS.size()){
                        pat = 0;
                    }
                    delay = Ambient.getInstance().getClickPatternComponent().clickTimeMS.get(pat);
                    timeSinceLastClick.reset();
                    clickCount++;
                    pat++;
                }

            }

        }


        return clickCount;
    }


}
