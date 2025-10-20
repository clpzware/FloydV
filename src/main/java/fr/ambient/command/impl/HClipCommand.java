package fr.ambient.command.impl;

import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;

public class HClipCommand extends Command {

    public HClipCommand(){
        super("hclip", "hc");
    }


    @Override
    public void execute(String[] args, String message) {
        ChatUtil.display("HCLIPPED");
    }
}
