package fr.ambient.command.impl;

import fr.ambient.command.Command;
import fr.ambient.component.impl.player.HypixelComponent;
import fr.ambient.util.player.ChatUtil;

public class HypixelAPICommand extends Command {
    public HypixelAPICommand() {
        super("setkey", "sk");
    }
    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if(words.length < 1){
            ChatUtil.display(".setkey <APIKEY>");
            ChatUtil.display("You can get a hypixel API key at developer.hypixel.net");
            return;
        }


        if(words[1].equalsIgnoreCase("reset")){
            return;

        }


        HypixelComponent.HYPIXEL_APIKEY = words[1];
        ChatUtil.display("Hypixel API key is now set to : " + HypixelComponent.HYPIXEL_APIKEY);

    }
}
