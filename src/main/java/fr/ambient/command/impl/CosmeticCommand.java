package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.component.impl.misc.CosmeticComponent;
import fr.ambient.util.player.ChatUtil;

public class CosmeticCommand extends Command {

    public CosmeticCommand() {
        super("cosmetic", "cos");
    }

    @Override
    public void execute(String[] args, String message) {
        if (args.length < 2) {
            ChatUtil.display("Invalid arguments! Usage: .cos <cape/halo> <id>");
            return;
        }

        if(args[1].equals("cape")){
            CosmeticComponent.customCapeId = args[2];
            Ambient.getInstance().getConfig().setValue("capeId", args[2]);
            ChatUtil.display("Set custom cape id to : " + Ambient.getInstance().getConfig().getValue("capeId"));


        }
        if(args[1].equals("halo")){
            CosmeticComponent.customHaloId = args[2];
            Ambient.getInstance().getConfig().setValue("haloId", args[2]);
            ChatUtil.display("Set custom halo id to : " + Ambient.getInstance().getConfig().getValue("haloId"));
        }


    }
}
