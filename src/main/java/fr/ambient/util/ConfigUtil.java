package fr.ambient.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.theme.Theme;
import fr.ambient.util.player.ChatUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class ConfigUtil {


    public void read(JsonObject object){
        ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        String savedate = object.get("savedate").getAsString(); // if needed
        String version = object.get("version").getAsString(); // if needed
        String theme = object.get("theme").getAsString();


        if(!Objects.equals(version, Ambient.getInstance().getVersion())){
            ChatUtil.display("Loading outdated config...");
        }

        Theme dogTheme = Ambient.getInstance().getThemeManager().getThemeByName(theme);
        if (dogTheme == null) dogTheme = Ambient.getInstance().getThemeManager().getThemeByName("Aqua");
        Ambient.getInstance().getHud().setCurrentTheme(dogTheme);

        JsonObject modulesData = object.get("moduleData").getAsJsonObject();

        for (Map.Entry<String, JsonElement> entry : modulesData.entrySet()) {
            Module module = Ambient.getInstance().getModuleManager().getModule(entry.getKey());
            if(module == null){
                continue;
            }

            JsonObject moduleData = modulesData.get(entry.getKey()).getAsJsonObject();
            if(!module.getName().equals("ClickGUI")){
                module.setEnabled(moduleData.get("enabled").getAsBoolean());
            }
            module.setCustomName(moduleData.get("customname").getAsString());
            module.setKeyBind(moduleData.get("bind").getAsInt());

            try {
                module.setShown(moduleData.get("showModule").getAsBoolean());
            }catch (Exception e){

            }

            if(module.isDraggable() && moduleData.has("dragging")){
                JsonObject dragMoData = moduleData.get("dragging").getAsJsonObject();
                module.setX(dragMoData.get("x").getAsInt());
                module.setY(dragMoData.get("y").getAsInt());
                if(module.getX() > sr.getScaledWidth()){
                    module.setX((int) (Math.min(module.getX(), sr.getScaledWidth()) - module.getWidth()));
                }
                if(module.getY() > sr.getScaledHeight()){
                    module.setX((int) (Math.min(module.getY(), sr.getScaledHeight()) - module.getHeight()));
                }

            }

            JsonObject moduleSetting = moduleData.get("properties").getAsJsonObject();
            applySetting(module.getPropertyList(), moduleSetting);
        }
        // ya, should be ok now <3

    }


    public void applySetting(List<Property<?>> settings, JsonObject object){

        for(Property<?> property : settings){
            try {
                if(property instanceof ModeProperty modeProperty){
                    modeProperty.setValue(object.get(modeProperty.getLabel()).getAsString());
                    continue;
                }
                if(property instanceof NumberProperty numberProperty){
                    numberProperty.setValue(object.get(numberProperty.getLabel()).getAsFloat());
                    continue;
                }
                if(property instanceof BooleanProperty booleanProperty){
                    booleanProperty.setValue(object.get(booleanProperty.getLabel()).getAsBoolean());
                    continue;
                }
                if(property instanceof ColorProperty colorProperty){
                    JsonObject colorProp = object.get(colorProperty.getLabel()).getAsJsonObject();
                    colorProperty.setValue(new Color(colorProp.get("red").getAsInt(), colorProp.get("green").getAsInt(), colorProp.get("blue").getAsInt(), colorProp.get("alpha").getAsInt()));
                    continue;
                }
                if(property instanceof MultiProperty multiProperty){
                    JsonArray multiArray = object.get(multiProperty.getLabel()).getAsJsonArray();
                    multiProperty.clearAll();
                    for(JsonElement element : multiArray){
                        multiProperty.setValueOF(element.getAsString(), true);
                    }
                    continue;
                }
                if(property instanceof CompositeProperty compositeProperty){
                    JsonObject compObj = object.get(compositeProperty.getLabel()).getAsJsonObject();
                    applySetting(compositeProperty.getChildren(), compObj);
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public String write(){
        JsonObject object = new JsonObject();

        object.addProperty("savedate", System.currentTimeMillis());
        object.addProperty("version", Ambient.getInstance().getVersion());
        object.addProperty("author", Ambient.getInstance().getUid());
        object.addProperty("theme", Ambient.getInstance().getThemeManager().getCurrentTheme().getName());


        JsonObject modulesObject = new JsonObject();

        for(Module module : Ambient.getInstance().getModuleManager().getObjects()){
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("enabled", module.isEnabled());
            moduleObject.addProperty("customname", module.getCustomName());
            moduleObject.addProperty("bind", module.getKeyBind());
            moduleObject.addProperty("showModule", module.isShown());

            if(module.isDraggable()){
                JsonObject draggable = new JsonObject();

                draggable.addProperty("x", module.getX());
                draggable.addProperty("y", module.getY());

                moduleObject.add("dragging", draggable);
            }

            JsonObject settingObject = new JsonObject();
            addModuleSettingToJson(module.getPropertyList(), settingObject);

            moduleObject.add("properties", settingObject);

            modulesObject.add(module.getName(), moduleObject);
        }

        object.add("moduleData", modulesObject);

        return object.toString();
    }

    public void addModuleSettingToJson(List<Property<?>> properties, JsonObject object){
        for(Property<?> property : properties){
            if(property instanceof ModeProperty modeProperty){
                object.addProperty(modeProperty.getLabel(), modeProperty.getValue());
            }
            if(property instanceof NumberProperty numberProperty){
                object.addProperty(numberProperty.getLabel(), numberProperty.getValue());
            }
            if(property instanceof BooleanProperty booleanProperty){
                object.addProperty(booleanProperty.getLabel(), booleanProperty.getValue());
            }
            if(property instanceof ColorProperty colorProperty){
                JsonObject colObj = new JsonObject();

                colObj.addProperty("red", colorProperty.getValue().getRed());
                colObj.addProperty("green", colorProperty.getValue().getGreen());
                colObj.addProperty("blue", colorProperty.getValue().getBlue());
                colObj.addProperty("alpha", colorProperty.getValue().getAlpha());

                object.add(colorProperty.getLabel(), colObj);
            }
            if(property instanceof MultiProperty multiProperty){
                JsonArray multObj = new JsonArray();
                for(String s : multiProperty.getValue()){
                    multObj.add(s);
                }
                object.add(multiProperty.getLabel(), multObj);
            }
            if(property instanceof CompositeProperty compositeProperty){
                JsonObject compObj = new JsonObject();
                addModuleSettingToJson(compositeProperty.getChildren(), compObj);
                object.add(compositeProperty.getLabel(), compObj);
            }
        }
    }
}
