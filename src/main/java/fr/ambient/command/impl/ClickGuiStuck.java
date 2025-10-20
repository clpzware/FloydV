package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.module.impl.render.ClickGui;
import fr.ambient.util.player.ChatUtil;

public class ClickGuiStuck extends Command {
    public ClickGuiStuck() {
        super("cguistuck", "clickguistuck");
    }

    @Override
    public void execute(String[] args, String message) {
        ClickGui clickGui = Ambient.getInstance().getModuleManager().getModule(ClickGui.class);
        clickGui.mode.setValue("Dropdown");
        ChatUtil.display("Reset ClickGui");
    }
}
