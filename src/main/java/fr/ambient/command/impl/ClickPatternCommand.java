package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;

import java.io.File;
import java.util.Objects;

public class ClickPatternCommand extends Command {
    public ClickPatternCommand() {
        super("clickpattern", "cp");
    }
    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length == 1) {
            ChatUtil.display(".clickpattern reset/save <name>/load <name>/startrec/stoprec/list");
            return;
        }

        switch (words[1].toLowerCase()){
            case "reset" -> {
                Ambient.getInstance().getClickPatternComponent().clickTimeMS.clear();
                ChatUtil.display("Reset.");
            }
            case "save" -> {
                if(words.length > 2){
                    Ambient.getInstance().getClickPatternComponent().save(words[2]);
                }else{
                    ChatUtil.display(".clickpattern save <name>");
                }
            }
            case "load" -> {
                if(words.length > 2){
                    Ambient.getInstance().getClickPatternComponent().load(words[2]);
                }else{
                    ChatUtil.display(".clickpattern load <name>");
                }
            }
            case "startrec" -> Ambient.getInstance().getClickPatternComponent().startRecording();
            case "stoprec" -> Ambient.getInstance().getClickPatternComponent().stopRecording();
            case "list" -> {
                ChatUtil.display("Saved ClickPattern Lists");
                final File d = new File(mc.mcDataDir, "dog/clickpatterns");d.mkdirs();
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
