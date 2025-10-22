package femcum.modernfloyd.clients.util.file;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.util.Accessor;

import java.io.File;
public class FileManager {

    public static final File DIRECTORY = new File(Accessor.mc.mcDataDir, Floyd.NAME);

    public void init() {
        if (!DIRECTORY.exists()) {
            DIRECTORY.mkdir();
        }
    }
}