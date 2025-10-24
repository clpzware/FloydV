package femcum.modernfloyd.clients.util.file.config;

import femcum.modernfloyd.clients.util.file.FileManager;
import femcum.modernfloyd.clients.util.file.FileType;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Patrick
 * @since 10/19/2021
 */
public class ConfigManager extends ArrayList<ConfigFile> {

    public static final File CONFIG_DIRECTORY = new File(FileManager.DIRECTORY, "configs");
    private ConfigFile latestConfig;
    private boolean autoLoadEnabled = false; // Flag to control auto-loading

    public void init() {
        if (!CONFIG_DIRECTORY.exists()) {
            CONFIG_DIRECTORY.mkdir();
        }
        this.update();
        this.setupLatestConfig();
    }

    public void setupLatestConfig() {
        latestConfig = get("latest", true);

        // Don't automatically load the config during initialization
        // The config will only be loaded when explicitly requested by the player
    }

    /**
     * Call this method after the player has joined the world to enable auto-loading
     */
    public void enableAutoLoad() {
        this.autoLoadEnabled = true;
        // Optionally load the latest config here if desired
        // if (latestConfig != null && latestConfig.getFile().exists()) {
        //     latestConfig.read();
        // }
    }

    /**
     * Manually load the latest config (safe to call anytime)
     */
    public void loadLatestConfig() {
        if (latestConfig != null && latestConfig.getFile().exists()) {
            latestConfig.read();
        }
    }

    public ConfigFile getLatestConfig() {
        return latestConfig;
    }

    public ConfigFile get(final String config, final boolean allowKey) {
        final File file = new File(ConfigManager.CONFIG_DIRECTORY, config + ".json");
        final ConfigFile configFile = new ConfigFile(file, FileType.CONFIG, config);

        if (allowKey) {
            configFile.allowKeyCodeLoading();
        }

        return configFile;
    }

    public ConfigFile get(final String config) {
        final File file = new File(ConfigManager.CONFIG_DIRECTORY, config + ".json");
        final ConfigFile configFile = new ConfigFile(file, FileType.CONFIG, config);
        configFile.allowKeyCodeLoading();
        return configFile;
    }

    public void set(final String config) {
        set(config, true);
    }

    public void set(final String config, final boolean allowKey) {
        final File file = new File(CONFIG_DIRECTORY, config + ".json");
        ConfigFile configFile = null;

        // Check if config already exists in the list
        for (ConfigFile existing : this) {
            if (existing.getName().equals(config)) {
                configFile = existing;
                break;
            }
        }

        if (configFile == null) {
            configFile = new ConfigFile(file, FileType.CONFIG, config);
            add(configFile);
            System.out.println("Creating new config...");
        } else {
            System.out.println("Overwriting existing config...");
        }

        if (allowKey) {
            configFile.allowKeyCodeLoading();
        }

        // Write is what actually saves the keybinds, so we need to ensure allowKeyCodeLoading is set first
        configFile.write();
        System.out.println("Config saved to files.");
    }

    public boolean update() {
        clear();

        final File[] files = CONFIG_DIRECTORY.listFiles();
        if (files == null) {
            return false;
        }

        for (final File file : files) {
            if (file.getName().endsWith(".json")) {
                String configName = file.getName().replace(".json", "");
                add(new ConfigFile(file, FileType.CONFIG, configName));
            }
        }

        return true;
    }

    public boolean delete(final String config) {
        ConfigFile configFile = null;

        // Find the config in the list
        for (ConfigFile existing : this) {
            if (existing.getName().equals(config)) {
                configFile = existing;
                break;
            }
        }

        if (configFile == null) {
            return false;
        }

        remove(configFile);
        return configFile.getFile().delete();
    }

    public void loadFromString(final String configName, final String jsonData, final boolean allowKey) {
        final File file = new File(CONFIG_DIRECTORY, configName + ".json");
        final ConfigFile configFile = new ConfigFile(file, FileType.CONFIG, configName);

        if (allowKey) {
            configFile.allowKeyCodeLoading();
        }

        configFile.readFromString(jsonData);
    }

    public boolean isAutoLoadEnabled() {
        return autoLoadEnabled;
    }
}