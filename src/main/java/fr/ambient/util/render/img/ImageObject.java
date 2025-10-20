package fr.ambient.util.render.img;

import cc.polymorphism.annot.ExcludeConstant;
import cc.polymorphism.annot.ExcludeFlow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.opengl.GL11.glColor4f;

@ExcludeFlow
@ExcludeConstant
public class ImageObject {

    public BufferedImage img = null;
    public int textureID = 0;
    private int width, height = 0;
    public boolean loading = false;
    public boolean isLoaded = false;
    private final ResourceLocation resourceLocation;
    private final File file;

    public ImageObject(File file) {
        this.file = file;
        this.resourceLocation = null;
    }

    public ImageObject(ResourceLocation resourceLocation) {
        this.file = null;
        this.resourceLocation = resourceLocation;
    }

    private void loadImage() throws IOException {
        if (resourceLocation != null) {
            loadImageFromResource();
        } else if (file != null) {
            loadImageFromDisk();
        }
    }

    private void loadImageFromDisk() throws IOException {
        img = ImageIO.read(file);
    }

    private void loadImageFromResource() throws IOException {
        InputStream inputStream = Minecraft.getMinecraft().getResourceManager().getResource(resourceLocation).getInputStream();
        img = ImageIO.read(inputStream);
    }

    public CompletableFuture<Void> loadAsync() {
        if (loading || isLoaded) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.runAsync(() -> {
            try {
                loading = true;
                loadImage();
                width = img.getWidth();
                height = img.getHeight();
                int[] pixels = new int[width * height];
                img.getRGB(0, 0, width, height, pixels, 0, width);

                ByteBuffer buffer = ByteBuffer.allocateDirect(4 * width * height);

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = pixels[y * width + x];
                        buffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
                        buffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
                        buffer.put((byte) (pixel & 0xFF));         // Blue
                        buffer.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
                    }
                }

                buffer.flip();

                Minecraft.getMinecraft().addScheduledTask(() -> {
                    textureID = GL11.glGenTextures();
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                    GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);

                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                    if (width > 64 || height > 32) {
                        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
                    }

                    if (GL11.glGetString(GL11.GL_EXTENSIONS).contains("GL_EXT_texture_filter_anisotropic")) {
                        float maxAnisotropy = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
                        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
                    }

                    isLoaded = true;
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void unload() {
        if (!isLoaded)
            return;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GlStateManager.deleteTexture(textureID);
    }

    public void drawImg(float x, float y, float width, float height) {
        if(!isLoaded)
            return;

        enableTexture2D();
        enableBlend();
        glColor4f(1, 1, 1, 1);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        resetColor();


    }

    public void drawImg(float x, float y, float width, float height, Color color) {
        if(!isLoaded)
            return;

        enableTexture2D();
        enableBlend();
        glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);

        resetColor();
    }

    public void drawImg(float x, float y) {
        if(!isLoaded)
            return;

        enableTexture2D();
        enableBlend();
        glColor4f(1, 1, 1, 1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height);
        resetColor();
    }
}
