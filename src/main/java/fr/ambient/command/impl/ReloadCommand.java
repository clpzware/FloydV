package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.component.impl.player.HypixelComponent;
import fr.ambient.module.impl.render.hud.Overlay;
import fr.ambient.util.Reconnect;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;
import org.lwjglx.input.Mouse;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "r");
    }

    @Override
    @SneakyThrows
    public void execute(String[] args, String message) {
        ChatUtil.display("Reloading...");
        long start = System.currentTimeMillis();
        Ambient.getInstance().getModuleManager().getModule(Overlay.class).playerData.clear();
        System.gc();
        Mouse.scrollEvents = new double[999999];
        Ambient.getInstance().setToken(null);
        Reconnect.reco();
        long stop = System.currentTimeMillis();
        ChatUtil.display("Done... Took " + (stop - start) + " ms to reload.");
    }
}
