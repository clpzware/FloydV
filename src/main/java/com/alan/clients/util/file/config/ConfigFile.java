package com.alan.clients.util.file.config;

import com.alan.clients.Client;
import com.alan.clients.bindable.Bindable;
import com.alan.clients.component.impl.render.NotificationComponent;
import com.alan.clients.event.impl.other.ConfigLoadEvent;
import com.alan.clients.module.Module;
import com.alan.clients.module.impl.render.ClickGUI;
import com.alan.clients.ui.theme.Themes;
import com.alan.clients.util.file.FileType;
import com.alan.clients.value.Mode;
import com.alan.clients.value.Value;
import com.alan.clients.value.impl.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Setter;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class ConfigFile extends com.alan.clients.util.file.File implements Bindable {

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
            // reads file to a json object
            final FileReader fileReader = new FileReader(getFile());
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            final JsonObject jsonObject = getGSON().fromJson(bufferedReader, JsonObject.class);

            // closes both readers
            bufferedReader.close();
            fileReader.close();

            // checks if there was data read
            if (jsonObject == null) {
                return false;
            }

            if (Client.INSTANCE.getConfigManager().getStopWatch().finished(1000L)) {
                //TODO: Nigga
                //Network.getInstance().getClient().sendMessage(new C2SPacketConvertConfig(jsonObject.toString()).export());
                Client.INSTANCE.getConfigManager().getStopWatch().reset();
            }
        } catch (final IOException ignored) {
            return false;
        }

        Client.INSTANCE.getEventBus().handle(new ConfigLoadEvent());

        if (name != null) NotificationComponent.post("Config", "Loaded " + name + " config");

        return true;
    }

    @Override
    public boolean write() {
        try {
            // creates the file
            this.getFile().createNewFile();
            // creates a new json object where all data is stored in
            final JsonObject jsonObject = new JsonObject();

            // Add some extra information to the config
            final JsonObject metadataJsonObject = new JsonObject();
            metadataJsonObject.addProperty("version", Client.VERSION);
            metadataJsonObject.addProperty("creationDate", DATE_FORMATTER.format(new Date()));
            jsonObject.add("Metadata", metadataJsonObject);

            // loops through all modules to save their data
            for (final Module module : Client.INSTANCE.getModuleManager().getAll()) {
                // creates an own module json object
                final JsonObject moduleJsonObject = new JsonObject();

                // adds data to the module json object
                if (!(module instanceof ClickGUI)) {
                    moduleJsonObject.addProperty("state", module.isEnabled());
                }

                if (saveKeyCodes) moduleJsonObject.addProperty("keyCode", module.getKey());

                int index = 0;
                for (final Value<?> value : module.getAllValues()) {
                    index++;
                    final JsonObject valueJsonObject = new JsonObject();

                    if (value instanceof ModeValue) {
                        final ModeValue enumValue = (ModeValue) value;
                        Object val = enumValue.getValue();

                        // If the stored value is a Mode, get the name
                        if (val instanceof Mode) {
                            valueJsonObject.addProperty("value", ((Mode) val).getName());
                        }
                        // If it's a String, just save it directly (fallback)
                        else if (val instanceof String) {
                            valueJsonObject.addProperty("value", (String) val);
                        }
                        // Otherwise, fallback to toString()
                        else if (val != null) {
                            valueJsonObject.addProperty("value", val.toString());
                        }
                    } else if (value instanceof BooleanValue) {
                        final BooleanValue booleanValue = (BooleanValue) value;
                        valueJsonObject.addProperty("value", booleanValue.getValue());
                    } else if (value instanceof NumberValue) {
                        final NumberValue numberValue = (NumberValue) value;
                        valueJsonObject.addProperty("value", numberValue.getValue().doubleValue());
                    } else if (value instanceof StringValue) {
                        final StringValue stringValue = (StringValue) value;

                        String save = stringValue.getValue();
                        save = save.replace("%", "<percentsign>");

                        valueJsonObject.addProperty("value", save);
                    } else if (value instanceof BoundsNumberValue) {
                        final BoundsNumberValue boundsNumberValue = (BoundsNumberValue) value;
                        valueJsonObject.addProperty("first", boundsNumberValue.getValue().doubleValue());
                        valueJsonObject.addProperty("second", boundsNumberValue.getSecondValue().doubleValue());
                    } else if (value instanceof ColorValue) {
                        final ColorValue colorValue = (ColorValue) value;

                        valueJsonObject.addProperty("red", colorValue.getValue().getRed());
                        valueJsonObject.addProperty("green", colorValue.getValue().getGreen());
                        valueJsonObject.addProperty("blue", colorValue.getValue().getBlue());
                        valueJsonObject.addProperty("alpha", colorValue.getValue().getAlpha());
                    } else if (value instanceof DragValue) {
                        final DragValue positionValue = (DragValue) value;

                        valueJsonObject.addProperty("positionX", positionValue.position.x);
                        valueJsonObject.addProperty("positionY", positionValue.position.y);

                        valueJsonObject.addProperty("scaleX", positionValue.scale.x);
                        valueJsonObject.addProperty("scaleY", positionValue.scale.y);
                    } else if (value instanceof ListValue) {
                        final ListValue<?> enumValue = (ListValue<?>) value;
                        valueJsonObject.addProperty("value", enumValue.getValue().toString());
                    }

                    String name = value.getParent() != null ? (value.getParent() instanceof Module ? ((Module) value.getParent()).getModuleInfo().aliases()[0] + " Module" :
                            ((Mode<?>) value.getParent()).getName() + " Mode") : "Unknown";

                    moduleJsonObject.add(value.getName() + " in " + name, valueJsonObject);
                }

                // updates json object which contains all data
                jsonObject.add(module.getModuleInfo().aliases()[0], moduleJsonObject);
            }

            jsonObject.addProperty("theme", Client.INSTANCE.getThemeManager().getTheme().name());

            // writes json object data to a file
            final FileWriter fileWriter = new FileWriter(getFile());
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            getGSON().toJson(jsonObject, bufferedWriter);

            // closes the writer
            bufferedWriter.flush();
            bufferedWriter.close();
//            fileWriter.flush();
//            fileWriter.close();
        } catch (final IOException exception) {
            exception.printStackTrace();
            return false;
        }

        return true;
    }

    public void saveKeyCodes() {
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
            // Parse JSON string
            final JsonObject jsonObject = getGSON().fromJson(json, JsonObject.class);

            if (jsonObject == null) {
                System.out.println("Config JSON was empty or invalid.");
                return;
            }

            // Optional: mimic behavior of read() (like network or stopwatch reset)
            if (Client.INSTANCE.getConfigManager().getStopWatch().finished(1000L)) {
                // Normally this would send config data to the network, omitted here
                Client.INSTANCE.getConfigManager().getStopWatch().reset();
            }

            // Simulate config load event and notifications like in read()
            Client.INSTANCE.getEventBus().handle(new ConfigLoadEvent());

            if (name != null) {
                NotificationComponent.post("Config", "Loaded " + name + " config (from memory)");
            }

            // --- Now actually apply settings to modules ---
            for (final Module module : Client.INSTANCE.getModuleManager().getAll()) {
                final String alias = module.getModuleInfo().aliases()[0];
                if (!jsonObject.has(alias)) continue;

                final JsonObject moduleJson = jsonObject.getAsJsonObject(alias);

                // Restore module enabled state
                if (moduleJson.has("state") && !(module instanceof com.alan.clients.module.impl.render.ClickGUI)) {
                    module.setEnabled(moduleJson.get("state").getAsBoolean());
                }

                // Restore key binding if present
                if (moduleJson.has("keyCode")) {
                    module.setKey(moduleJson.get("keyCode").getAsInt());
                }

                for (final Value<?> value : module.getAllValues()) {
                    for (String key : moduleJson.keySet()) {
                        if (!key.startsWith(value.getName())) continue;

                        final var element = moduleJson.get(key);
                        if (element == null || element.isJsonNull()) continue;

                        if (!element.isJsonObject()) {
                            // primitive value: boolean, number, string
                            if (value instanceof BooleanValue) {
                                ((BooleanValue) value).setValue(element.getAsBoolean());
                            } else if (value instanceof NumberValue) {
                                ((NumberValue) value).setValue(parseNumberSafely(element));
                            } else if (value instanceof StringValue) {
                                ((StringValue) value).setValue(element.getAsString().replace("<percentsign>", "%"));
                            }
                            continue;
                        }

                        // complex object
                        final JsonObject valueJson = element.getAsJsonObject();

                        if (value instanceof BoundsNumberValue) {
                            double first = getSafeDouble(valueJson, "first", (Double) ((BoundsNumberValue) value).getValue());
                            double second = getSafeDouble(valueJson, "second", (Double) ((BoundsNumberValue) value).getSecondValue());
                            ((BoundsNumberValue) value).setValue(first);
                            ((BoundsNumberValue) value).setSecondValue(second);
                        } else if (value instanceof DragValue) {
                            ((DragValue) value).position.x = getSafeFloat(valueJson, "positionX", (float) ((DragValue) value).position.x);
                            ((DragValue) value).position.y = getSafeFloat(valueJson, "positionY", (float) ((DragValue) value).position.y);
                            ((DragValue) value).scale.x = getSafeFloat(valueJson, "scaleX", (float) ((DragValue) value).scale.x);
                            ((DragValue) value).scale.y = getSafeFloat(valueJson, "scaleY", (float) ((DragValue) value).scale.y);
                        } else if (value instanceof ColorValue) {
                            if (valueJson.has("red") && valueJson.has("green") && valueJson.has("blue") && valueJson.has("alpha")) {
                                ((ColorValue) value).setValue(new Color(
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

            // Restore theme if present
            if (jsonObject.has("theme")) {
                String themeName = jsonObject.get("theme").getAsString();
                Client.INSTANCE.getThemeManager().setTheme(Themes.valueOf(themeName.toLowerCase(Locale.ROOT)));
            }

            System.out.println("Successfully loaded config from string.");

        } catch (Exception e) {
            System.err.println("Failed to load config from string:");
            e.printStackTrace();
        }
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