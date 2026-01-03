import femcum.modernfloyd.clients.Floyd;
import net.minecraft.client.main.Main;
import java.io.File;
import java.util.Arrays;

public class Start {

    private static void setupLWJGLNatives() {
        String osName = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();

        // Try multiple possible native locations
        String[] possiblePaths = {
                "natives",
                "natives-" + getOSName(),
                System.getProperty("user.dir") + File.separator + "natives",
                System.getProperty("java.io.tmpdir") + File.separator + "floyd-natives"
        };

        String nativePath = null;
        String specificPath = null;

        // Determine the specific OS path
        if (osName.contains("win")) {
            specificPath = arch.contains("64") ? "windows" + File.separator + "64" : "windows" + File.separator + "32";
        } else if (osName.contains("mac")) {
            specificPath = "macos";
        } else if (osName.contains("linux")) {
            specificPath = "linux";
        } else {
            System.err.println("Warning: Unsupported OS: " + osName + ", attempting to continue without custom natives path");
            return; // Let LWJGL try to find natives itself
        }

        // Try to find an existing natives directory
        for (String basePath : possiblePaths) {
            File testDir = new File(basePath + File.separator + specificPath);
            if (testDir.exists() && testDir.isDirectory()) {
                nativePath = testDir.getAbsolutePath();
                System.out.println("Found natives at: " + nativePath);
                break;
            }

            // Also try without the specific OS subfolder
            testDir = new File(basePath);
            if (testDir.exists() && testDir.isDirectory()) {
                File[] files = testDir.listFiles();
                if (files != null && files.length > 0) {
                    nativePath = testDir.getAbsolutePath();
                    System.out.println("Found natives at: " + nativePath);
                    break;
                }
            }
        }

        if (nativePath == null) {
            System.err.println("Warning: No natives directory found. LWJGL will use system libraries.");
            System.err.println("Searched in:");
            for (String path : possiblePaths) {
                System.err.println("  - " + path + File.separator + specificPath);
            }
            System.err.println("\nThis might work anyway - continuing launch...");
            return; // Don't throw exception, let it try
        }

        // Set the library path
        System.setProperty("org.lwjgl.librarypath", nativePath);
        System.setProperty("net.java.games.input.librarypath", nativePath);
        System.out.println("Configured LWJGL natives at: " + nativePath);
    }

    private static String getOSName() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) return "windows";
        if (osName.contains("mac")) return "macos";
        if (osName.contains("linux")) return "linux";
        return "unknown";
    }

    public static void main(final String[] args) {
        System.out.println("=================================");
        System.out.println("Floyd Client Launcher");
        System.out.println("Version: " + Floyd.VERSION_FULL);
        System.out.println("=================================");
        System.out.println("OS: " + System.getProperty("os.name"));
        System.out.println("Java: " + System.getProperty("java.version"));
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        System.out.println("=================================\n");

        setupLWJGLNatives();

        System.out.println("\nStarting Minecraft...\n");

        Main.main(concat(new String[]{
                "--version", Floyd.NAME,
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