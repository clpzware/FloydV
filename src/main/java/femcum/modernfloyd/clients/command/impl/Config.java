package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.file.config.ConfigFile;
import femcum.modernfloyd.clients.util.file.config.ConfigManager;
import femcum.modernfloyd.clients.util.localization.Localization;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public final class Config extends Command {

    public Config() {
        super("command.config.description", "config", "configs", "cfg", "settings", "c");
    }

    @Override
    public void execute(final String[] args) {
        final ConfigManager configManager = getInstance().getConfigManager();

        if (args.length < 2) {
            ChatUtil.display("command.config.usage");
            return;
        }

        final String command = args[1].toLowerCase();

        switch (args.length) {
            case 3:
                final String name = args[2];

                switch (command) {
                    case "load":
                        configManager.update();

                        final ConfigFile config = configManager.get(name);

                        if (config != null && config.getFile().exists()) {
                            CompletableFuture.runAsync(() -> {
                                if (config.read()) {
                                    ChatUtil.display("command.config.loaded", name);
                                    if (!name.equalsIgnoreCase("latest")) {
                                        ChatUtil.display("command.config.accident");
                                    }
                                } else {
                                    ChatUtil.display("Failed to load config: " + name);
                                }
                            });
                        } else {
                            ChatUtil.display("Config not found: " + name);
                        }
                        break;

                    case "save":
                    case "create":
                        if (name.equalsIgnoreCase("latest")) {
                            ChatUtil.display("command.config.reserved");
                            return;
                        }

                        CompletableFuture.runAsync(() -> {
                            configManager.set(name, true); // Changed to true to save keybinds
                            ChatUtil.display("command.config.saved");
                            ChatUtil.display("command.config.reminder");
                        });
                        break;

                    case "delete":
                        if (name.equalsIgnoreCase("latest")) {
                            ChatUtil.display("Cannot delete the latest config!");
                            return;
                        }

                        CompletableFuture.runAsync(() -> {
                            if (configManager.delete(name)) {
                                ChatUtil.display("Deleted config: " + name);
                            } else {
                                ChatUtil.display("Failed to delete config: " + name);
                            }
                        });
                        break;

                    default:
                        ChatUtil.display("command.config.usage");
                        break;
                }
                break;

            case 2:
                switch (command) {
                    case "list":
                        ChatUtil.display("command.config.selectload");

                        configManager.update();

                        if (configManager.isEmpty()) {
                            ChatUtil.display("No configs found!");
                            break;
                        }

                        configManager.forEach(configFile -> {
                            final String configName = configFile.getFile().getName().replace(".json", "");
                            final String configCommand = ".config load " + configName;
                            final String color = getTheme().getChatAccentColor().toString();

                            final ChatComponentText chatText = new ChatComponentText(color + "> " + configName);
                            final ChatComponentText hoverText = new ChatComponentText(String.format(Localization.get("command.config.loadhover"), configName));

                            chatText.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, configCommand))
                                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

                            mc.thePlayer.addChatMessage(chatText);
                        });
                        break;

                    case "folder":
                        try {
                            Desktop desktop = Desktop.getDesktop();
                            File dirToOpen = new File(String.valueOf(ConfigManager.CONFIG_DIRECTORY));
                            desktop.open(dirToOpen);
                            ChatUtil.display("command.config.folder");
                        } catch (IllegalArgumentException | IOException iae) {
                            ChatUtil.display("command.config.notfound");
                        }
                        break;

                    case "loadlatest":
                        CompletableFuture.runAsync(() -> {
                            ConfigFile latestConfig = configManager.getLatestConfig();
                            if (latestConfig != null && latestConfig.getFile().exists()) {
                                if (latestConfig.read()) {
                                    ChatUtil.display("Loaded latest config");
                                } else {
                                    ChatUtil.display("Failed to load latest config");
                                }
                            } else {
                                ChatUtil.display("No latest config found");
                            }
                        });
                        break;

                    default:
                        ChatUtil.display("command.config.actions");
                        ChatUtil.display("Available commands: list, folder, loadlatest, load <name>, save <name>, delete <name>");
                        break;
                }
                break;

            default:
                ChatUtil.display("command.config.actions");
                break;
        }
    }
}