package cheadleware.util.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

public final class TTFUtils {

    private TTFUtils() {

    }

    public static Font getFontFromLocation(String fileName, int size) {
        try {
            // Correct ResourceLocation format: domain is "minecraft", path is "fonts/filename"
            // Minecraft automatically looks in assets/minecraft/ directory
            ResourceLocation location = new ResourceLocation("fonts/" + fileName);

            Font font = Font.createFont(
                    Font.TRUETYPE_FONT,
                    Minecraft.getMinecraft().getResourceManager()
                            .getResource(location)
                            .getInputStream()
            ).deriveFont(Font.PLAIN, size);

            return font;

        } catch (FontFormatException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getWidth(Font font, String text) {
        if (font == null) return 0;

        // Create a temporary image to get font metrics
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics();
        int width = metrics.stringWidth(text);
        g2d.dispose();

        return width;
    }

    public static void drawString(Font font, String text, float x, float y, int color) {
        if (font == null) {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, false);
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);

        float scaledX = x * 2;
        float scaledY = y * 2;

        drawStringInternal(font, text, scaledX, scaledY, color);

        GlStateManager.popMatrix();
    }

    public static void drawStringWithShadow(Font font, String text, float x, float y, int color) {
        if (font == null) {
            Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, true);
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.5, 0.5, 0.5);

        float scaledX = x * 2;
        float scaledY = y * 2;

        // Draw shadow
        int shadowColor = (color & 0xFF000000) != 0 ? (color & 0x00FCFCFC) >> 2 | color & 0xFF000000 : 0xFF000000;
        drawStringInternal(font, text, scaledX + 2, scaledY + 2, shadowColor);

        // Draw main text
        drawStringInternal(font, text, scaledX, scaledY, color);

        GlStateManager.popMatrix();
    }

    private static void drawStringInternal(Font font, String text, float x, float y, int color) {
        // Extract color components
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        // Set color
        GlStateManager.color(red, green, blue, alpha);

        // Use Minecraft's font renderer as fallback for actual rendering
        // You would need a proper font texture system for true TTF rendering
        // This is a simplified version that uses AWT metrics but MC rendering
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, color, false);
    }
}