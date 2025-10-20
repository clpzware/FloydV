package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.io.File;

public class ScriptCommand extends Command {
    public ScriptCommand(){
        super("script");
    }
    @Override
    public void execute(String[] args, String message) {

        File scriptFolder = new File(Minecraft.getMinecraft().mcDataDir, "/ambient/scripts/");
        scriptFolder.mkdirs();

        try {
            String[] words = message.split(" ");
            String action = words[1].toLowerCase();

            switch (action) {
                case "load" -> {

                }
                case "unload" -> {

                }
                case "loaded" -> {

                }
                case "folder" ->{
                    Desktop.getDesktop().browseFileDirectory(scriptFolder);
                }
                case "settings" -> {

                }

            }
        }catch (Exception e){
            ChatUtil.display("Issue while command execution");
            e.printStackTrace();
        }
    }
}
