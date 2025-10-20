package fr.ambient.component.impl.misc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.ambient.Ambient;
import fr.ambient.component.Component;
import fr.ambient.module.Module;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class BindComponent extends Component {

    public static final File location = new File(mc.mcDataDir, "/ambient/binds");
    @SneakyThrows
    public static void save(String name){
        location.mkdirs();
        JsonObject saveObj = new JsonObject();
        for(Module module : Ambient.getInstance().getModuleManager().getObjects()){
            saveObj.addProperty(module.getId() + "-bind", module.getKeyBind());
            saveObj.addProperty(module.getId() + "-held", module.getOnlyOnKeyHold().getValue());
        }
        FileUtils.write(new File(location, name + ".json"), saveObj.toString());
    }

    @SneakyThrows
    public static void load(String name){
        location.mkdirs();
        File f = new File(location, name + ".json");
        if(!f.exists()){
            ChatUtil.display("File does not exist. Please check i guess.");
            return;
        }
        JsonObject object = new JsonParser().parse(FileUtils.readFileToString(f)).getAsJsonObject();
        for(Module module : Ambient.getInstance().getModuleManager().getObjects()){
            if(object.has(module.getId() + "-bind")){
                module.setKeyBind(object.get(module.getId() + "-bind").getAsInt());
                module.getOnlyOnKeyHold().setValue(object.get(module.getId() + "-held").getAsBoolean());
            }
        }
    }
}
