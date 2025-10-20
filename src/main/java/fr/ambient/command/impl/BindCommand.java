package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.component.impl.misc.BindComponent;
import fr.ambient.module.Module;
import fr.ambient.util.player.ChatUtil;
import org.lwjglx.input.Keyboard;

import java.io.File;
import java.util.Objects;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "b", "blind");
    }

    @Override
    public void execute(final String[] args, final String message) {
        try {
            switch (args[1]) {
                case "list" -> {
                    ChatUtil.display("-- Binds --");
                    for (Module m : Ambient.getInstance().getModuleManager().getObjects()) {
                        if (m.getKeyBind() != Integer.MIN_VALUE && m.getKeyBind() != 0) {
                            ChatUtil.display(m.getName() + " > " + Keyboard.getKeyName(m.getKeyBind()) + " [" + m.getKeyBind() + "]");
                        }
                    }
                    return;
                }
                case "clear" -> {
                    ChatUtil.display("Clearing All Binds...");
                    for (Module m : Ambient.getInstance().getModuleManager().getObjects()) {
                        m.setKeyBind(Integer.MIN_VALUE);
                    }
                    return;
                }
                case "save" -> {
                    if (args.length > 2) {
                        BindComponent.save(args[2]);
                        ChatUtil.display("Saved binds as " + args[2]);
                    } else {
                        ChatUtil.display(".bind save <name>");
                    }
                    return;
                }
                case "load" -> {
                    if (args.length > 2) {
                        BindComponent.load(args[2]);
                        ChatUtil.display("Loaded binds from " + args[2]);

                    } else {
                        ChatUtil.display(".bind load <name>");
                    }
                    return;
                }
                case "listsaved" -> {
                    ChatUtil.display("-- Saved BindList --");
                    if (BindComponent.location.listFiles() != null) {
                        for (File f : Objects.requireNonNull(BindComponent.location.listFiles())) {
                            ChatUtil.display(f.getName() + " / " + f.length() + " bytes");
                        }
                    }
                    return;
                }
            }


            Module module = Ambient.getInstance().getModuleManager().getModule(args[1]);
            int keyCode = Keyboard.getKeyIndex(args[2].toUpperCase());

            module.setKeyBind(keyCode);

            ChatUtil.display(String.format("Module %s has been bound to %s", module.getName(), Keyboard.getKeyName(module.getKeyBind())));
        } catch (Throwable throwable) {
            ChatUtil.display("Usage: .bind [module or list] [key]");
        }
    }
}