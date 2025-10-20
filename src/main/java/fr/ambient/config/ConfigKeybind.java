package fr.ambient.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.system.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static fr.ambient.util.InstanceAccess.mc;

public class ConfigKeybind {


    public HashMap<Integer, String> loadedKeys = new HashMap<>();

    public void onKeyPressed(int key){
        if(loadedKeys.containsKey(key)){
            Ambient.getInstance().getConfigManager().getConfig(loadedKeys.get(key));
        }
    }

    public void addKeyConfig(int key, String config){
        loadedKeys.put(key, config);
        ChatUtil.display("Added " + config + " bind to key " + key);
    }
    public void removeKeyConfig(int key){
        loadedKeys.remove(key);

        ChatUtil.display("Removed bound configs from " + key);
    }


    public void loadKeyConfig(){
        loadedKeys.clear();
        new File(mc.mcDataDir, "/ambient/cfgbinds").mkdir();
        final File d = new File(mc.mcDataDir, "/ambient/cfgbinds/kc.json");

        if(!d.exists()){
            return;
        }

        JsonObject config = FileUtil.readJsonFromFile(d.getAbsolutePath());
        for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
            int bb = Integer.parseInt(entry.getKey());
            String cfgName = entry.getValue().getAsString();
            loadedKeys.put(bb, cfgName);
        }

    }

    public void saveKeyConfig(){
        JsonObject jsonObject = new JsonObject();
        for(int a : loadedKeys.keySet()){
            jsonObject.addProperty(a + "", loadedKeys.get(a));
        }
        FileUtil.writeJsonToFile(jsonObject, new File(mc.mcDataDir, "/ambient/cfgbinds/kc.json").getAbsolutePath());
    }



}
