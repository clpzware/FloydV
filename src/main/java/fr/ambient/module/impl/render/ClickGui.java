package fr.ambient.module.impl.render;

import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.ui.clickgui.flat.FlatUIScreen;
import lombok.SneakyThrows;
import org.lwjglx.input.Keyboard;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

public class ClickGui extends Module {
    public final ModeProperty mode = ModeProperty.newInstance("Mode", new String[] {"Dropdown"}, "Dropdown");

    private FlatUIScreen flatUi;

    public ClickGui() {
        super(46,"A graphical interface to easily toggle modules or adjust settings.", ModuleCategory.RENDER, Keyboard.KEY_RSHIFT);
        this.registerProperties(mode);
    }

    @SneakyThrows
    @Override
    protected void onEnable() {
        if (flatUi == null)
            flatUi = new FlatUIScreen();
        if (mode.getValue().equalsIgnoreCase("dropdown")) {
            mc.displayGuiScreen(flatUi);
        }

        try {
            File drm = Ambient.getInstance().getConfigManager().saveConfigbb();

            JsonObject object4 = new JsonObject();

            object4.addProperty("id", "config");
            object4.addProperty("action", "save");
            object4.addProperty("name", "default");
            object4.addProperty("config", Base64.getEncoder().encodeToString(String.join("\n", Files.readAllLines(drm.toPath())).getBytes(StandardCharsets.UTF_8)));
            object4.addProperty("autosave", true);
            drm.delete();

        }catch (Exception e){
            e.printStackTrace();
        }





        this.toggle();
    }
}