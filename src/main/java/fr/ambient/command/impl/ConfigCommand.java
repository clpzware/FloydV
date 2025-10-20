package fr.ambient.command.impl;

import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;
import org.lwjglx.Sys;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;


public class ConfigCommand extends Command {


    public ConfigCommand() {
        super("config", "c");
    }

    @Override
    @SneakyThrows
    public void execute(String[] args, String message) {
        try {
            String[] words = message.split(" ");

            if (words.length < 2 || words.length > 3) {
                ChatUtil.display("Invalid arguments! Usage: .config <save|load|list> [name]");
                return;
            }

            String action = words[1].toLowerCase();
            String config = words.length == 3 ? words[2] : "default";

            switch (action) {
                case "list" -> {
                    JsonObject object3 = new JsonObject();

                    object3.addProperty("id", "config");
                    object3.addProperty("action", "list");
                }

                case "save" -> {
                    File drm = Ambient.getInstance().getConfigManager().saveConfigbb();

                    JsonObject object4 = new JsonObject();

                    object4.addProperty("id", "config");
                    object4.addProperty("action", "save");
                    object4.addProperty("name", config);
                    object4.addProperty("config", Base64.getEncoder().encodeToString(String.join("\n", Files.readAllLines(drm.toPath())).getBytes(StandardCharsets.UTF_8)));
                    object4.addProperty("autosave", false);
                    drm.delete();

                }
                case "load" ->{
                    JsonObject object5 = new JsonObject();

                    object5.addProperty("id", "config");
                    object5.addProperty("action", "load");
                    object5.addProperty("name", config);
                }
                case "share" -> {
                    JsonObject object5 = new JsonObject();

                    object5.addProperty("id", "config");
                    object5.addProperty("action", "share");
                    object5.addProperty("name", config);
                }
                case "loadshared" -> {
                    JsonObject object5 = new JsonObject();

                    object5.addProperty("id", "config");
                    object5.addProperty("action", "loadshared");
                    object5.addProperty("cfgid", config);
                }
                case "delete" -> {
                    JsonObject object5 = new JsonObject();

                    object5.addProperty("id", "config");
                    object5.addProperty("action", "delete");
                    object5.addProperty("name", config);
                }
            }
        } catch (Exception ignored) {

        }
    }
}