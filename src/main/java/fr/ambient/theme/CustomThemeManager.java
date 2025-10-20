package fr.ambient.theme;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class CustomThemeManager {
    private static final File fileLocation = new File(Minecraft.getMinecraft().mcDataDir, "/ambient/customtheme.json");

    private static final Gson gson = new Gson();

    public Theme customTheme = new Theme("Custom", EnumChatFormatting.WHITE,
            new Color(255, 255, 255),
            new Color(255, 255, 255),
            false
    );

    @SneakyThrows
    public void save() {
        if (!fileLocation.exists()) {
            return;
        }

        try (FileWriter writer = new FileWriter(fileLocation)) {
            JsonObject object = new JsonObject();
            JsonObject c1 = new JsonObject();
            c1.addProperty("red", customTheme.color1.getRed());
            c1.addProperty("green", customTheme.color1.getGreen());
            c1.addProperty("blue", customTheme.color1.getBlue());
            object.add("color1", c1);
            JsonObject c2 = new JsonObject();
            c2.addProperty("red", customTheme.color2.getRed());
            c2.addProperty("green", customTheme.color2.getGreen());
            c2.addProperty("blue", customTheme.color2.getBlue());
            object.add("color2", c2);
            gson.toJson(object, writer);
        }
    }

    @SneakyThrows
    public void load() {
        if (!fileLocation.exists()) {
            return;
        }

        JsonObject from = gson.fromJson(Files.readString(fileLocation.toPath()), JsonObject.class);
        JsonObject c1 = from.getAsJsonObject("color1");
        JsonObject c2 = from.getAsJsonObject("color2");
        Color c1c = new Color(c1.get("red").getAsInt(), c1.get("green").getAsInt(), c1.get("blue").getAsInt());
        Color c2c = new Color(c2.get("red").getAsInt(), c2.get("green").getAsInt(), c2.get("blue").getAsInt());
        customTheme.color1 = c1c;
        customTheme.color2 = c2c;
    }


}
