package cheadleware.util.font;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class TrueTypeFontRenderer implements FontRenderer {

    private static final char COLOR_INVOKER = '\247';
    private static final Random RANDOM = new Random();
    private final Font font;
    public final CharData[] charData = new CharData[256];
    private final int[] colorCodes = new int[32];
    private final int margin;
    private final boolean antiAlias;
    private final boolean fracMetrics;

    public TrueTypeFontRenderer(Font font, boolean antiAlias, boolean fracMetrics) {
        generateColors();
        this.font = font;
        this.margin = 6;
        this.antiAlias = antiAlias;
        this.fracMetrics = fracMetrics;
    }

    @Override
    public int drawString(String text, float x, float y, int color) {
        renderString(text, x, y, color, false);
        return 0;
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        double s = 0.5;

        glPushMatrix();
        glTranslated(s, s, 0);
        renderString(text, x, y, color, true);
        glPopMatrix();

        renderString(text, x, y, color, false);
        return 0;
    }

    @Override
    public float getWidth(String text) {
        if (text == null || text.isEmpty()) return 0;

        float width = 0;
        CharData[] characterData = charData;
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            if (character == COLOR_INVOKER || (i > 0 ? text.charAt(i - 1) : '.') == COLOR_INVOKER || !isValid(character))
                continue;

            CharData data = characterData[character];
            width += (data.width - (2 * margin)) / 2;
        }
        return width;
    }

    @Override
    public float getHeight(String text) {
        float height = 0;
        CharData[] characterData = charData;
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            if ((i > 0 ? text.charAt(i - 1) : '.') == COLOR_INVOKER || character == COLOR_INVOKER || !isValid(character))
                continue;

            CharData data = characterData[character];
            height = Math.max(height, data.height);
        }
        return (height - margin) / 2;
    }

    public void generateTextures() {
        for (int i = 0; i < 256; i++) {
            char c = (char) i;
            if (isValid(c)) setup(c);
        }
    }

    private void setup(char character) {
        BufferedImage utilityImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();
        utilityGraphics.setFont(font);
        FontMetrics fontMetrics = utilityGraphics.getFontMetrics();
        Rectangle2D characterBounds = fontMetrics.getStringBounds(String.valueOf(character), utilityGraphics);

        BufferedImage characterImage = new BufferedImage(
                (int) StrictMath.ceil(characterBounds.getWidth() + (2 * margin)),
                (int) StrictMath.ceil(characterBounds.getHeight()),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphics = (Graphics2D) characterImage.getGraphics();
        graphics.setFont(font);
        graphics.setColor(new Color(255, 255, 255, 0));
        graphics.fillRect(0, 0, characterImage.getWidth(), characterImage.getHeight());
        graphics.setColor(Color.WHITE);

        if (antiAlias)
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        if (fracMetrics)
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        graphics.drawString(String.valueOf(character), margin, fontMetrics.getAscent());

        int textureId = glGenTextures();
        createTexture(textureId, characterImage);
        charData[character] = new CharData(characterImage.getWidth(), characterImage.getHeight(), textureId);
    }

    private void createTexture(int textureId, BufferedImage image) {
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    }

    private void renderString(CharSequence text, float x, float y, int color, boolean shadow) {
        if (text == null || text.length() == 0) return;

        // Save GL state
        glPushMatrix();
        glPushAttrib(GL_ALL_ATTRIB_BITS);

        // Setup GL state
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);

        if ((color & 0xFC000000) == 0) color |= 0xFF000000;
        if (color == 0x20FFFFFF) color = 0xFFAFAFAF;

        glScaled(0.5, 0.5, 1);
        x -= margin / 2f;
        y -= 2;
        x *= 2;
        y *= 2;

        CharData[] characterData = charData;
        boolean underlined = false;
        boolean strikethrough = false;
        boolean obfuscated = false;
        int length = text.length();
        float multiplier = (shadow ? 4 : 1);

        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        glColor4f(r / multiplier, g / multiplier, b / multiplier, a);

        for (int i = 0; i < length; i++) {
            char character = text.charAt(i);
            char previous = i > 0 ? text.charAt(i - 1) : '.';

            if (previous == COLOR_INVOKER) continue;

            if (character == COLOR_INVOKER) {
                int index = "0123456789ABCDEFKLMNOR".indexOf(text.charAt(i + 1));
                if (index < 16) {
                    obfuscated = false;
                    strikethrough = false;
                    underlined = false;
                    characterData = charData;
                    if (index < 0) index = 15;
                    if (shadow) index += 16;
                    int textColor = this.colorCodes[index];
                    glColor4f((textColor >> 16) / 255.0F, (textColor >> 8 & 255) / 255.0F, (textColor & 255) / 255.0F, a);
                } else if (index == 16) {
                    obfuscated = true;
                } else if (index == 18) {
                    strikethrough = true;
                } else if (index == 19) {
                    underlined = true;
                } else {
                    obfuscated = false;
                    strikethrough = false;
                    underlined = false;
                    characterData = charData;
                    glColor4d(1 / multiplier, 1 / multiplier, 1 / multiplier, a);
                }
            } else {
                if (!isValid(character)) continue;

                if (obfuscated) character += (char) (RANDOM.nextInt(Math.max(0, 256 - character)));

                final CharData data = characterData[character];
                drawChar(data, x, y);

                if (strikethrough) drawLine(0, data.height / 2f, data.width, data.height / 2f, 3);
                if (underlined) drawLine(0, data.height - 15, data.width, data.height - 15, 3);

                x += data.width - (2 * margin);
            }
        }

        // Restore GL state
        glPopAttrib();
        glPopMatrix();
    }

    private boolean isValid(char c) {
        return c > 10 && c < 256 && c != 127;
    }

    private void drawChar(CharData data, float x, float y) {
        data.bind();
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2d(x, y);
        glTexCoord2f(0, 1);
        glVertex2d(x, y + data.height);
        glTexCoord2f(1, 1);
        glVertex2d(x + data.width, y + data.height);
        glTexCoord2f(1, 0);
        glVertex2d(x + data.width, y);
        glEnd();
    }

    private void drawLine(float x, float y, float x2, float y2, float width) {
        glDisable(GL_TEXTURE_2D);
        glLineWidth(width);
        glBegin(GL_LINES);
        glVertex2f(x, y);
        glVertex2f(x2, y2);
        glEnd();
        glEnable(GL_TEXTURE_2D);
    }

    private void generateColors() {
        for (int i = 0; i < 32; i++) {
            int thingy = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + thingy;
            int green = (i >> 1 & 1) * 170 + thingy;
            int blue = (i & 1) * 170 + thingy;
            if (i == 6) red += 85;
            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCodes[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    public static class CharData {
        private final int textureId;
        public float width;
        public float height;

        private CharData(float width, float height, int textureId) {
            this.width = width;
            this.height = height;
            this.textureId = textureId;
        }

        public void bind() {
            glBindTexture(GL_TEXTURE_2D, textureId);
        }
    }
}