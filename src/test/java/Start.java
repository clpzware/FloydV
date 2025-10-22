import com.alan.clients.Client;
import net.minecraft.client.main.Main;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Start {
    private static void setupLWJGLNatives() {
        String osName = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        String basePath = "natives";
        String path;

        if (osName.contains("win")) {
            path = arch.contains("64") ? basePath + "/windows/64" : basePath + "/windows/32";
        } else if (osName.contains("mac")) {
            path = basePath + "/macos";
        } else if (osName.contains("linux")) {
            path = basePath + "/linux";
        } else {
            throw new UnsupportedOperationException("Unsupported OS: " + osName);
        }

        File nativesDir = new File(path);
        if (!nativesDir.exists()) {
            throw new RuntimeException("Natives directory not found: " + nativesDir.getAbsolutePath());
        }

        // Tell LWJGL where the natives are
        System.setProperty("org.lwjgl.librarypath", nativesDir.getAbsolutePath());
        System.setProperty("net.java.games.input.librarypath", nativesDir.getAbsolutePath());

        System.out.println("Configured LWJGL natives at: " + nativesDir.getAbsolutePath());
    }

    public static void main(final String[] args) {
        setupLWJGLNatives();

        Main.main(concat(new String[]{
                "--version", Client.NAME,
                "--accessToken", "0",
                "--assetsDir", "assets",
                "--assetIndex", "1.8",
                "--userProperties", "{}"
        }, args));
    }

    public static <T> T[] concat(final T[] first, final T[] second) {
        final T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
