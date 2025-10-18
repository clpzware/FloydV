package cheadleware.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Properties;

public class FontUtils {

    private FontUtils() {
    }

    /**
     * Read font properties from a .properties file
     * Changes .png extension to .properties and loads the file
     */
    public static Properties readFontProperties(ResourceLocation locationFontTexture) {
        String path = locationFontTexture.getResourcePath();
        Properties properties = new Properties();
        String pngExtension = ".png";

        if (!path.endsWith(pngExtension)) {
            return properties;
        }

        String propertiesPath = path.substring(0, path.length() - pngExtension.length()) + ".properties";
        ResourceLocation propertiesLocation = new ResourceLocation(locationFontTexture.getResourceDomain(), propertiesPath);

        try {
            InputStream inputStream = Minecraft.getMinecraft().getResourceManager()
                    .getResource(propertiesLocation)
                    .getInputStream();

            if (inputStream != null) {
                properties.load(inputStream);
                inputStream.close();
            }
        } catch (IOException e) {
            // Properties file doesn't exist or couldn't be loaded
            // Return empty properties
        }

        return properties;
    }

    /**
     * Read custom character widths from properties
     * Format: width.{charCode}={width}
     */
    public static void readCustomCharWidths(Properties props, float[] charWidth) {
        String prefix = "width.";

        for (String key : props.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                try {
                    int charCode = Integer.parseInt(key.substring(prefix.length()));

                    if (charCode >= 0 && charCode < charWidth.length) {
                        float width = Float.parseFloat(props.getProperty(key));

                        if (width >= 0.0F) {
                            charWidth[charCode] = width;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid number format, skip this entry
                }
            }
        }
    }

    /**
     * Read a float value from properties with a default fallback
     */
    public static float readFloat(Properties props, String key, float defaultValue) {
        String value = props.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid float value for " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Read a boolean value from properties with a default fallback
     * Accepts: true/false, on/off (case insensitive)
     */
    public static boolean readBoolean(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);

        if (value == null) {
            return defaultValue;
        }

        String normalizedValue = value.toLowerCase().trim();

        if (normalizedValue.equals("true") || normalizedValue.equals("on")) {
            return true;
        } else if (normalizedValue.equals("false") || normalizedValue.equals("off")) {
            return false;
        } else {
            System.err.println("Invalid boolean value for " + key + ": " + value);
            return defaultValue;
        }
    }

    /**
     * Check if a resource exists
     */
    public static boolean resourceExists(ResourceLocation location) {
        try {
            Minecraft.getMinecraft().getResourceManager().getResource(location);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get HD font location (for high-resolution texture packs)
     * Looks for fonts in mcpatcher/ directory
     */
    public static ResourceLocation getHdFontLocation(ResourceLocation fontLocation) {
        if (fontLocation == null) {
            return null;
        }

        String path = fontLocation.getResourcePath();
        String texturesPrefix = "textures/";
        String hdPrefix = "mcpatcher/";

        if (!path.startsWith(texturesPrefix)) {
            return fontLocation;
        }

        // Create HD path: mcpatcher/font/...
        String hdPath = hdPrefix + path.substring(texturesPrefix.length());
        ResourceLocation hdLocation = new ResourceLocation(fontLocation.getResourceDomain(), hdPath);

        // Return HD location if it exists, otherwise return original
        return resourceExists(hdLocation) ? hdLocation : fontLocation;
    }
}