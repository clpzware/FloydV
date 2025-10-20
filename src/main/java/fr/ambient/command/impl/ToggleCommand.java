package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.module.Module;
import fr.ambient.util.player.ChatUtil;



public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle", "t");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 1 || words.length > 2) {
            ChatUtil.display("Invalid arguments! Usage: .toggle <module>");
            return;
        }
        String mds = words[1].toLowerCase();
        Module module = Ambient.getInstance().getModuleManager().getModule(mds);
        if(module != null){
            module.setEnabled(!module.isEnabled());
            ChatUtil.display("Toggled module " + module.getName() + " to : " + module.isEnabled());
        }
    }
}