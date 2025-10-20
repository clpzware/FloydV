package fr.ambient.component.impl.player;

import fr.ambient.component.Component;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.util.RotationAnalysisData;
import fr.ambient.util.Situation;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.PlayerUtil;
import lombok.SneakyThrows;
import net.minecraft.util.MovingObjectPosition;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class RotationPatternComponent extends Component {
    public boolean isRecording = false;

    public ArrayList<RotationAnalysisData> analysisData = new ArrayList<>();

    public float lastYaw, lastPitch;

    public HashMap<Situation, ArrayList<float[]>> usableInProd = new HashMap<>();

    private File rotationDataFolder = new File(mc.mcDataDir, "/ambient/rotationpattern");


    @SneakyThrows
    public void save(String name){

        if(isRecording){
            ChatUtil.display("You are still recording !");
            return;
        }


        rotationDataFolder.mkdir();
        if(analysisData.isEmpty()){
            ChatUtil.display("Data Empty");
            return;
        }


        String rotationData = "";

        for(RotationAnalysisData analysisData1 : analysisData){
            if(analysisData1.getYawDelta() != 0){
                rotationData += analysisData1.getYawDelta() + ":" + analysisData1.getPitchDelta() + ":" + analysisData1.getPlayerHitVecY() + ";";
            }


        }


        FileUtils.write(new File(rotationDataFolder, name + ".rts"), rotationData);
    }

    @SneakyThrows
    public void load(String name){

        if(isRecording){
            ChatUtil.display("You are still recording !");
            return;
        }

        usableInProd.clear();
        analysisData.clear();

        File file = new File(rotationDataFolder, name + ".rts");

        if(!file.exists()){
            ChatUtil.display("Rotation File doesnt Exist");
            return;
        }
        int put = 0;
        String[] splitted = FileUtils.readFileToString(file).split(";");
        ChatUtil.display("Trying to load...");
        usableInProd.put(Situation.ON_TARGET, new ArrayList<>());
        usableInProd.put(Situation.PRE_AIM, new ArrayList<>());
        for(String s : splitted){
            String[] spl21 = s.split(":");
            Situation situation = Situation.ON_TARGET;
            if(spl21[2].equals("0.0")){
                situation = Situation.PRE_AIM;
            }
            usableInProd.get(situation).add(new float[]{Float.parseFloat(spl21[0]),Float.parseFloat(spl21[1]),Float.parseFloat(spl21[2])});
            put++;
        }
        ChatUtil.display(put + " vals loaded. " + usableInProd.size()  + " situations");
    }



    public void startRecording(){
        isRecording = true;
        analysisData.clear();
        ChatUtil.display("Start Rec");
    }
    public void stopRecording(){
        isRecording = false;
        ChatUtil.display("Stop Rec");
    }


    @SubscribeEvent
    public void onRotation(UpdateEvent event){
        if(isRecording && mc.thePlayer != null){
            try {



                float deltaYaw = Math.abs(lastYaw - mc.thePlayer.rotationYaw);
                float deltaPitch = Math.abs(lastPitch - mc.thePlayer.rotationPitch);

                MovingObjectPosition movingObjectPosition = PlayerUtil.getMouseMOP(1f, 3.5f, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);

                float vecYDep = 0;

                if(movingObjectPosition != null && movingObjectPosition.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY){
                    vecYDep = (float) (movingObjectPosition.hitVec.yCoord - movingObjectPosition.entityHit.posY);
                    ChatUtil.display(vecYDep);
                }
                lastYaw = mc.thePlayer.rotationYaw;
                lastPitch = mc.thePlayer.rotationPitch;


                analysisData.add(new RotationAnalysisData(deltaYaw, deltaPitch, 0, 0, vecYDep));
            }catch (Exception e){
                e.printStackTrace();
            }

        }else{
            isRecording = false;
        }


    }





}
