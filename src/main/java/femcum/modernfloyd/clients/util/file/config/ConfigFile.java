// File: ConfigFile.java

package femcum.modernfloyd.clients.util.file.config;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.bindable.Bindable;
import femcum.modernfloyd.clients.component.impl.render.NotificationComponent;
import femcum.modernfloyd.clients.event.impl.other.ConfigLoadEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.impl.render.ClickGUI;
import femcum.modernfloyd.clients.ui.theme.Themes;
import femcum.modernfloyd.clients.util.file.FileType;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.Value;
import femcum.modernfloyd.clients.value.impl.*;
import com.google.gson.JsonObject;
import femcum.modernfloyd.clients.util.file.File;
import lombok.Setter;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConfigFile extends File implements Bindable {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd.MM.yyyy");

    private boolean saveKeyCodes;
    private String name;
    @Setter
    private int key;

    public ConfigFile(final java.io.File file, final FileType fileType, final String name) {
        super(file, fileType);
        this.name = name;
    }

    public ConfigFile(final java.io.File file, final FileType fileType) {
        super(file, fileType);
    }

    @Override
    public boolean read() {
        if (!this.getFile().exists()) {
            return false;
        }

        try {
            final FileReader fileReader = new FileReader(getFile());
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            final JsonObject jsonObject = getGSON().fromJson(bufferedReader, JsonObject.class);

            bufferedReader.close();
            fileReader.close();

            if (jsonObject == null) {
                return false;
            }

            applyConfigData(jsonObject);

        } catch (final IOException ignored) {
            return false;
        }

        Floyd.INSTANCE.getEventBus().handle(new ConfigLoadEvent());

        if (name != null) {
            NotificationComponent.post("Config", "Loaded " + name + " config");
        }

        return true;
    }

    @Override
    public boolean write() {
        try {
            this.getFile().createNewFile();

            final JsonObject jsonObject = new JsonObject();

            final JsonObject metadataJsonObject = new JsonObject();
            metadataJsonObject.addProperty("version", Floyd.VERSION);
            metadataJsonObject.addProperty("creationDate", DATE_FORMATTER.format(new Date()));
            jsonObject.add("Metadata", metadataJsonObject);

            for (final Module module : Floyd.INSTANCE.getModuleManager().getAll()) {
                final JsonObject moduleJsonObject = new JsonObject();

                if (!(module instanceof ClickGUI)) {
                    moduleJsonObject.addProperty("state", module.isEnabled());
                }

                if (saveKeyCodes) {
                    moduleJsonObject.addProperty("keyCode", module.getKey());
                }

                for (final Value<?> value : module.getAllValues()) {
                    final JsonObject valueJsonObject = new JsonObject();

                    if (value instanceof ModeValue modeValue) {
                        Object val = modeValue.getValue();

                        if (val instanceof Mode mode) {
                            valueJsonObject.addProperty("value", mode.getName());
                        } else if (val != null) {
                            valueJsonObject.addProperty("value", val.toString());
                        }
                    } else if (value instanceof BooleanValue booleanValue) {
                        valueJsonObject.addProperty("value", booleanValue.getValue());
                    } else if (value instanceof NumberValue numberValue) {
                        valueJsonObject.addProperty("value", numberValue.getValue().doubleValue());
                    } else if (value instanceof StringValue stringValue) {
                        String save = stringValue.getValue();
                        save = save.replace("%", "<percentsign>");
                        valueJsonObject.addProperty("value", save);
                    } else if (value instanceof BoundsNumberValue boundsNumberValue) {
                        valueJsonObject.addProperty("first", boundsNumberValue.getValue().doubleValue());
                        valueJsonObject.addProperty("second", boundsNumberValue.getSecondValue().doubleValue());
                    } else if (value instanceof ColorValue colorValue) {
                        valueJsonObject.addProperty("red", colorValue.getValue().getRed());
                        valueJsonObject.addProperty("green", colorValue.getValue().getGreen());
                        valueJsonObject.addProperty("blue", colorValue.getValue().getBlue());
                        valueJsonObject.addProperty("alpha", colorValue.getValue().getAlpha());
                    } else if (value instanceof DragValue dragValue) {
                        valueJsonObject.addProperty("positionX", dragValue.position.x);
                        valueJsonObject.addProperty("positionY", dragValue.position.y);
                        valueJsonObject.addProperty("scaleX", dragValue.scale.x);
                        valueJsonObject.addProperty("scaleY", dragValue.scale.y);
                    } else if (value instanceof ListValue<?> listValue) {
                        valueJsonObject.addProperty("value", listValue.getValue().toString());
                    }

                    String parentName = value.getParent() != null ?
                            (value.getParent() instanceof Module ?
                                    ((Module) value.getParent()).getModuleInfo().aliases()[0] + " Module" :
                                    ((Mode<?>) value.getParent()).getName() + " Mode") :
                            "Unknown";

                    moduleJsonObject.add(value.getName() + " in " + parentName, valueJsonObject);
                }

                jsonObject.add(module.getModuleInfo().aliases()[0], moduleJsonObject);
            }

            jsonObject.addProperty("theme", Floyd.INSTANCE.getThemeManager().getTheme().name());

            final FileWriter fileWriter = new FileWriter(getFile());
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            getGSON().toJson(jsonObject, bufferedWriter);

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (final IOException exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    public void allowKeyCodeLoading() {
        this.saveKeyCodes = true;
    }

    @Override
    public int getKey() {
        return key;
    }

    @Override
    public void onKey() {
        this.read();
    }

    @Override
    public String[] getAliases() {
        return new String[]{getName()};
    }

    @Override
    public String getName() {
        return name;
    }

    public void readFromString(String json) {
        try {
            final JsonObject jsonObject = getGSON().fromJson(json, JsonObject.class);

            if (jsonObject == null) {
                System.out.println("Config JSON was empty or invalid.");
                return;
            }

            applyConfigData(jsonObject);

            Floyd.INSTANCE.getEventBus().handle(new ConfigLoadEvent());

            if (name != null) {
                NotificationComponent.post("Config", "Loaded " + name + " config (from memory)");
            }

        } catch (Exception e) {
            System.err.println("Failed to load config from string:");
            e.printStackTrace();
        }
    }

    private void applyConfigData(JsonObject jsonObject) {
        for (final Module module : Floyd.INSTANCE.getModuleManager().getAll()) {
            final String alias = module.getModuleInfo().aliases()[0];
            if (!jsonObject.has(alias)) continue;

            final JsonObject moduleJson = jsonObject.getAsJsonObject(alias);

            if (moduleJson.has("state") && !(module instanceof ClickGUI)) {
                module.setEnabled(moduleJson.get("state").getAsBoolean());
            }

            if (moduleJson.has("keyCode")) {
                module.setKey(moduleJson.get("keyCode").getAsInt());
            }

            for (final Value<?> value : module.getAllValues()) {
                for (String key : moduleJson.keySet()) {
                    if (!key.startsWith(value.getName())) continue;

                    final var element = moduleJson.get(key);
                    if (element == null || element.isJsonNull()) continue;

                    if (!element.isJsonObject()) {
                        if (value instanceof BooleanValue booleanValue) {
                            booleanValue.setValue(element.getAsBoolean());
                        } else if (value instanceof NumberValue numberValue) {
                            numberValue.setValue(parseNumberSafely(element));
                        } else if (value instanceof StringValue stringValue) {
                            stringValue.setValue(element.getAsString().replace("<percentsign>", "%"));
                        } else if (value instanceof ModeValue modeValue) {
                            String modeName = element.getAsString();
                            for (Mode<?> mode : modeValue.getModes()) {
                                if (mode.getName().equalsIgnoreCase(modeName)) {
                                    modeValue.setValue(mode);
                                    break;
                                }
                            }
                        }
                        continue;
                    }

                    final JsonObject valueJson = element.getAsJsonObject();

                    // Handle ModeValue stored in JsonObject format (old configs)
                    if (value instanceof ModeValue modeValue && valueJson.has("value")) {
                        try {
                            String modeName = valueJson.get("value").getAsString();
                            for (Mode<?> mode : modeValue.getModes()) {
                                if (mode.getName().equalsIgnoreCase(modeName)) {
                                    modeValue.setValue(mode);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to load ModeValue: " + value.getName());
                        }
                        continue;
                    }

                    // Handle ListValue stored in JsonObject format
                    if (value instanceof ListValue<?> listValue && valueJson.has("value")) {
                        try {
                            String valueName = valueJson.get("value").getAsString();
                            listValue.setValueAsObject(valueName);
                        } catch (Exception e) {
                            System.err.println("Failed to load ListValue: " + value.getName());
                        }
                        continue;
                    }

                    if (value instanceof BoundsNumberValue boundsNumberValue) {
                        double first = getSafeDouble(valueJson, "first", boundsNumberValue.getValue().doubleValue());
                        double second = getSafeDouble(valueJson, "second", boundsNumberValue.getSecondValue().doubleValue());
                        boundsNumberValue.setValue(first);
                        boundsNumberValue.setSecondValue(second);
                    } else if (value instanceof DragValue dragValue) {
                        dragValue.position.x = getSafeFloat(valueJson, "positionX", (float) dragValue.position.x);
                        dragValue.position.y = getSafeFloat(valueJson, "positionY", (float) dragValue.position.y);
                        dragValue.scale.x = getSafeFloat(valueJson, "scaleX", (float) dragValue.scale.x);
                        dragValue.scale.y = getSafeFloat(valueJson, "scaleY", (float) dragValue.scale.y);
                    } else if (value instanceof ColorValue colorValue) {
                        if (valueJson.has("red") && valueJson.has("green") &&
                                valueJson.has("blue") && valueJson.has("alpha")) {
                            colorValue.setValue(new Color(
                                    valueJson.get("red").getAsInt(),
                                    valueJson.get("green").getAsInt(),
                                    valueJson.get("blue").getAsInt(),
                                    valueJson.get("alpha").getAsInt()
                            ));
                        }
                    } else if (value instanceof ListValue<?> listValue) {
                        if (valueJson.has("value")) {
                            listValue.setValueAsObject(valueJson.get("value").getAsString());
                        }
                    }
                }
            }
        }

        if (jsonObject.has("theme")) {
            try {
                String themeName = jsonObject.get("theme").getAsString();
                // Try uppercase first (standard enum format)
                Themes theme = Themes.valueOf(themeName.toUpperCase(Locale.ROOT));
                Floyd.INSTANCE.getThemeManager().setTheme(theme);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid theme name in config, using default theme");
            }
        }

        System.out.println("Successfully loaded config data.");
    }

    private double getSafeDouble(JsonObject obj, String key, double fallback) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            try {
                return obj.get(key).getAsDouble();
            } catch (Exception e) {
                String s = obj.get(key).getAsString();
                if ("true".equalsIgnoreCase(s)) return 1.0;
                if ("false".equalsIgnoreCase(s)) return 0.0;
                return Double.parseDouble(s);
            }
        }
        return fallback;
    }

    private float getSafeFloat(JsonObject obj, String key, float fallback) {
        return (float) getSafeDouble(obj, key, fallback);
    }

    private double parseNumberSafely(com.google.gson.JsonElement element) {
        try {
            return element.getAsDouble();
        } catch (Exception e) {
            String s = element.getAsString();
            if ("true".equalsIgnoreCase(s)) return 1.0;
            if ("false".equalsIgnoreCase(s)) return 0.0;
            return Double.parseDouble(s);
        }
    }
}