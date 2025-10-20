package fr.ambient.config.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import org.lwjglx.Sys;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ClientConfig {


    public ClientConfig(){
        load();
    }
    private HashMap<String,String> values = new HashMap<>();


    public String getValue(String valueName){
        if(!values.containsKey(valueName)){
            values.put(valueName, "none");
        }
        return values.getOrDefault(valueName, "none");
    }


    public void setValue(String key, String value){
        values.put(key, value);
        save();
        load();
    }


    public void load(){
        try {
            File f = new File(Minecraft.getMinecraft().mcDataDir, "ambient/config/client.json");
            if(!f.exists()){
                resetDefault();
                return;
            }
            JsonObject object = JsonParser.parseString(Files.readString(f.toPath())).getAsJsonObject();
            for(Map.Entry<String, JsonElement> elementEntry : object.entrySet()){
                values.put(elementEntry.getKey(), elementEntry.getValue().getAsString());
            }
        }catch (Exception e){
            System.err.println("failed to load client config.");
            e.printStackTrace();
        }
    }

    public void save(){
        try {
            File f = new File(Minecraft.getMinecraft().mcDataDir, "ambient/config/client.json");
            if(values.isEmpty()){
                resetDefault();
            }
            JsonObject object = new JsonObject();
            for(Map.Entry<String, String> booleanEntry : values.entrySet()){
                object.addProperty(booleanEntry.getKey(), booleanEntry.getValue());
            }
            Files.writeString(f.toPath(), object.toString());
        }catch (Exception e){
            System.err.println("failed to save client config.");
            e.printStackTrace();
        }
    }

    public void resetDefault(){
        values.put("irc-enabled", "true");
        values.put("wallpaper", "none");
        values.put("discord-rp", "true");
        values.put("auto-default-config", "true");
        values.put("skyblock", "false");
    }
}
