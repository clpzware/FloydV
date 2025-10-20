package fr.ambient.config;

import fr.ambient.Ambient;
import fr.ambient.util.InstanceAccess;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ConfigManager implements InstanceAccess {
    public static final Map<String, Config> configs = new HashMap<>();
    private Config activeConfig;


    public HashMap<String, String> userConfigList = new HashMap<>(); // name
    public HashMap<String, String> onlineConfigList = new HashMap<>();

    public void init() {

    }

    public void stop() {
        if (getConfig("default") == null) {
            Config config = new Config("default");
            config.write();
        } else getConfig("default").write();
    }

    public Config getConfig(String name) {
        return configs.keySet().stream().filter(key -> key.equalsIgnoreCase(name)).findFirst().map(configs::get).orElse(null);
    }

    public String saveConfig() {
        String rdm = UUID.randomUUID().toString();
        Config config = new Config(rdm);
        config.write();
        return rdm;
    }
    public File saveConfigbb() {
        String rdm = UUID.randomUUID().toString();
        Config config = new Config(rdm);
        config.write();
        return config.getF();
    }

    @SneakyThrows
    public String uploadConfig(String name, File file){


        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("multipart/form-data");

        // Create the multipart request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "file.txt",
                        RequestBody.create(mediaType, file))
                .build();

        return "NoConfigUploaded";
    }
}
