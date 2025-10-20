package fr.ambient.command.impl;

import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;

public class HelpCommand extends Command {

    private static final String[] COMMANDS = {
            "Bind [KEY]: Bind a specific key to a module.",
            "Config [List/Load/Save/Share]: Manage config (list, load, save, or share).",
            "cs: Toggle the Counter-Strike Alike Visuals.",
            "setkey [API Key]: Set your API key.",
            "Queue/Q [Bw1/Bw2/Bw3/Bw4/Sw_si/Sw_sn]: Quick queue command.",
            "Theme [List/Load]: Manage themes (view list or load a theme).",
            "Toggle/T [Module]: Toggle a specific module on or off.",
            "IGN [Copy your IGN]: Copy your in-game name (IGN).",
            "CustomName [reset/list]: Set Custom Module Name"};


    public HelpCommand() {
        super("help");
    }

    @Override
    public void execute(String[] args, String message) {
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("Available commands:\n");
        for (String command : COMMANDS) {
            ChatUtil.display(command);
        }
    }
}
