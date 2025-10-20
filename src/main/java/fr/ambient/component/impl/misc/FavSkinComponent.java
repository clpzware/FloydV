package fr.ambient.component.impl.misc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import fr.ambient.component.Component;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FavSkinComponent extends Component {
    public static final ArrayList<URL> skins = new ArrayList<>();
    private static final Gson gson = new Gson();
    private static final File fileLocation = new File(Minecraft.getMinecraft().mcDataDir, "dog/favskins.json");

    public static void load(){
        if (!fileLocation.exists()) {
            System.out.println("No file found. Skins list will remain empty.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(fileLocation))) {
            Type listType = new TypeToken<List<URL>>() {}.getType();
            List<URL> loadedSkins = gson.fromJson(reader, listType);

            if (loadedSkins != null) {
                skins.clear();
                skins.addAll(loadedSkins);
                System.out.println("Loaded " + skins.size() + " skins from file.");
            }
        } catch (Exception e) {
            System.err.println("Error loading skins: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void save(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileLocation))) {
            gson.toJson(skins, writer);
            System.out.println("Saved " + skins.size() + " skins to file.");
        } catch (Exception e) {
            System.err.println("Error saving skins: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
