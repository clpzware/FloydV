package fr.ambient.component.impl.player;

import fr.ambient.component.Component;
import fr.ambient.util.math.TimeUtil;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class ClickPatternComponent extends Component {

    public boolean isRecording = false;

    public ArrayList<Integer> clickTimeMS = new ArrayList<>();

    public TimeUtil timeUtil = new TimeUtil();




    @SneakyThrows
    public void save(String name){


        new File(mc.mcDataDir, "/ambient/clickpatterns").mkdirs();

        final File d = new File(mc.mcDataDir, "/ambient/clickpatterns/" + name + ".clickpattern");

        if(d.exists()){
            ChatUtil.display("Overriding Existing One...");
        }
        int vals = 0;

        FileWriter writer = new FileWriter(d);
        for(Integer i : clickTimeMS){
            writer.write(i + "\n");
            vals++;

        }
        writer.close();
        // done

        ChatUtil.display("Wrote " + vals + " values into " + name + ".");
    }
    @SneakyThrows
    public void load(String name){
        final File d = new File(mc.mcDataDir, "/ambient/clickpatterns/" + name + ".clickpattern");
        if(d.exists()){
            int vals = 0;
            Scanner rd = new Scanner(d);
            while (rd.hasNextLine()) {
                String data = rd.nextLine();
                clickTimeMS.add(Integer.valueOf(data));
                vals++;
            }
            rd.close();
            ChatUtil.display("Loaded " + vals + " values.");
        }else{
            ChatUtil.display("Save Data does not exist");
        }
    }


    public void startRecording(){
        isRecording = true;
        timeUtil.reset();
    }
    public void stopRecording(){
        isRecording = false;
        timeUtil.reset();
    }
    public void onClick(){
        if(isRecording){
            if(timeUtil.getTime() > 1000){
                ChatUtil.display("Click will not be registered. " + timeUtil.getTime() + " > 1000");
                timeUtil.reset();
            }else{
                clickTimeMS.add((int) timeUtil.getTime());
                timeUtil.reset();
            }
        }
    }
}
