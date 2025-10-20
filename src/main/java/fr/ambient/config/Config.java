package fr.ambient.config;

import fr.ambient.util.ConfigUtil;
import fr.ambient.util.InstanceAccess;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;

@Getter
@Setter
public class Config implements InstanceAccess {

    private final File directory = new File(System.getProperty("java.io.tmpdir"));

    private final String name;

    public Config(String name) {
        this.name = name;
    }

    public File getF(){
        return new File(directory, name);
    }

    @SneakyThrows
    public void write() {
        FileUtils.write(new File(directory, name), ConfigUtil.write());
    }

}