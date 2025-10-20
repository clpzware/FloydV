package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.module.Module;
import fr.ambient.module.impl.render.hud.HUD;
import fr.ambient.util.player.ChatUtil;

public class HideCommand extends Command {
    public HideCommand() {
        super("hide", "h");
    }

    @Override
    public void execute(final String[] args, final String message) {
        try {
            Module module = Ambient.getInstance().getModuleManager().getModule(args[1]);
            module.setShown(!module.isShown());
            ChatUtil.display(module.getName() + " is now " + (module.isShown() ? "Shown" : "Hidden"));
            HUD hud = Ambient.getInstance().getModuleManager().getModule(HUD.class);
            //hud.setArraylist();
        } catch (Throwable throwable) {
            ChatUtil.display("Usage: .hide [module]");
        }
    }
}