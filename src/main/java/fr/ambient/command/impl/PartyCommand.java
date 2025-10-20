package fr.ambient.command.impl;

import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.component.impl.misc.CosmeticComponent;
import fr.ambient.util.player.ChatUtil;

public class PartyCommand extends Command {

    public PartyCommand(){
        super("p", "party");
    }

    @Override
    public void execute(String[] args, String message) {
        if(args.length < 2){
            ChatUtil.display(".p <uid>");
            return;
        }
        ChatUtil.display("Sending Party invite...");


        JsonObject object = new JsonObject();
        object.addProperty("id", "uid2username");
        object.addProperty("uid", args[1]);
        object.addProperty("action", "party");
    }
}
