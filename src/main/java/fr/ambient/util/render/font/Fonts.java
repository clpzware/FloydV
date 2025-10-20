package fr.ambient.util.render.font;

import fr.ambient.util.InstanceAccess;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

public class Fonts implements InstanceAccess {
    private static final HashMap<Integer, TTFFontRenderer> OPEN_SANS_REGULAR = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> OPEN_SANS_MEDIUM = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> OPEN_SANS_BOLD = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> SAN_FRANCISCO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> ROBOTO_REGULAR = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> ROBOTO_MEDIUM = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> ROBOTO_BOLD = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> URBANIST_BOLD = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> MINECRAFT = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> MOON = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> NUNITO = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> GREYCLIFF = new HashMap<>();
    private static final HashMap<Integer, TTFFontRenderer> TAHOMA = new HashMap<>();

    public static TTFFontRenderer getOpenSansRegular(final int size) {
        return get(OPEN_SANS_REGULAR, size, "OpenSans-Regular", true, true);
    }

    public static TTFFontRenderer getSanFrancisco(final int size){
        return get(SAN_FRANCISCO, size, "SF-Pro", true, true);
    }


    public static TTFFontRenderer getOpenSansMedium(final int size) {
        return get(OPEN_SANS_MEDIUM, size, "OpenSans-Medium", true, true);
    }

    public static TTFFontRenderer getOpenSansBold(final int size) {
        return get(OPEN_SANS_BOLD, size, "OpenSans-Bold", true, true);
    }

    public static TTFFontRenderer getRobotoRegular(final int size) {
        return get(ROBOTO_REGULAR, size, "Roboto-Regular", true, true);
    }

    public static TTFFontRenderer getRobotoMedium(final int size) {
        return get(ROBOTO_MEDIUM, size, "Roboto-Medium", true, true);
    }

    public static TTFFontRenderer getRobotoBold(final int size) {
        return get(ROBOTO_BOLD, size, "Roboto-Bold", true, true);
    }


    public static TTFFontRenderer getMinecraft(final int size) {
        return get(MINECRAFT, size, "MinecraftRegular", true, true);
    }


    public static TTFFontRenderer getTahoma(final int size) {
        return get(URBANIST_BOLD, size, "tahoma", true, true);
    }

    public static TTFFontRenderer getNunito(final int size) {
        return get(NUNITO, size, "Nunito", true, true);
    }
    public static TTFFontRenderer getGreycliff(final int size) {
        return get(GREYCLIFF, size, "greycliff", true, true);
    }

    public static TTFFontRenderer getMoon(final int size) {
        return get(MOON, size, "Moon", true, true);
    }

    private static TTFFontRenderer get(HashMap<Integer, TTFFontRenderer> map, int size, String name, boolean antialiasing, boolean fractionalMetrics) {
        if (!map.containsKey(size)) {
            final Font font;

            try {
                font = Font.createFont(Font.TRUETYPE_FONT, mc.getResourceManager().getResource(new ResourceLocation("dogclient/font/" + name + ".ttf")).getInputStream()).deriveFont((float) size);
            } catch (final FontFormatException | IOException ignored) {
                return null;
            }

            map.put(size, new TTFFontRenderer(font, antialiasing, fractionalMetrics));
        }

        return map.get(size);
    }
}