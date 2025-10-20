package fr.ambient.command.impl;

import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class ClipboardCommand extends Command {
    public ClipboardCommand() {
        super("clipboard", "cb");
    }

    @Override
    public void execute(final String[] args, final String message) {
        try {
            StringSelection selection = new StringSelection(args[1]);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }catch (Exception e){
            ChatUtil.display("Chat what the fuck did you attempt");
            for(int i = 0; i < 69; i++){
                ChatUtil.display("Long live hutao <3");
            }
        }
    }
}
