package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;

import java.io.File;
import java.util.Objects;

public class RotationPatternCommand extends Command {
    public RotationPatternCommand() {
        super("rotationpattern", "rp");
    }
    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length == 1) {
            ChatUtil.display(".rotationpattern reset/save <name>/load <name>/startrec/stoprec/list");
            return;
        }

        switch (words[1].toLowerCase()){
            case "reset" -> {
                Ambient.getInstance().getRotationPatternComponent().analysisData.clear();
                ChatUtil.display("Reset.");
            }
            case "save" -> {
                if(words.length > 2){
                    Ambient.getInstance().getRotationPatternComponent().save(words[2]);
                }else{
                    ChatUtil.display(".rotationpattern save <name>");
                }
            }
            case "load" -> {
                if(words.length > 2){
                    Ambient.getInstance().getRotationPatternComponent().load(words[2]);
                }else{
                    ChatUtil.display(".rotationpattern load <name>");
                }
            }
            case "startrec" -> Ambient.getInstance().getRotationPatternComponent().startRecording();
            case "stoprec" -> Ambient.getInstance().getRotationPatternComponent().stopRecording();
            case "list" -> {
                ChatUtil.display("Saved Rotation Pattern Lists");
                final File d = new File(mc.mcDataDir, "dog/rotationpattern");d.mkdirs();
                if(d.listFiles() != null){
                    for(File f : Objects.requireNonNull(d.listFiles())){
                        ChatUtil.display(f.getName() + " - " + f.length() + "b");
                    }
                }else{
                    ChatUtil.display("Empty ;(");
                }


            }
        }
    }
}
