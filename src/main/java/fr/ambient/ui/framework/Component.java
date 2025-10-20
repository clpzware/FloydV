package fr.ambient.ui.framework;

import fr.ambient.util.InstanceAccess;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.GlStateManager;

import java.io.IOException;

@Getter
@Setter
public class Component implements InstanceAccess {

    private float startX, startY, startWidth, startHeight;
    private float targetX, targetY, targetWidth, targetHeight;
    private long lerpStartTime;
    private final long lerpDuration = 125;

    protected float x, y, width, height;
    protected boolean expanded, hovered;

    public void init() { /* */ };
    public void render(final int mouseX, final int mouseY) { /* */ };

    public boolean click(final int mouseX, final int mouseY, final int button) {
        hovered = isHovered(mouseX, mouseY);
        return false;
    };

    public void release(final int mouseX, final int mouseY, final int state) { /* */ };
    public void type(final char typedChar, final int keyCode) { /* */ };
    public void drag(final int mouseX, final int mouseY, final int button, final long timeSinceLastClick) {}
    public void scroll() {};


    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY <= y + height;
    }

    public boolean isHoveredWithScale(int mouseX, int mouseY, float scale) {
        mouseX *= scale;
        mouseY *= scale;

        float posX = x * scale;
        float posY = y * scale;
        float w = width * scale;
        float h = height * scale;


        System.out.println("mouseX: " + mouseX + " posX: " + posX);
        System.out.println("mouseY: " + mouseY + " posY: " + posY);

        return mouseY > posY && mouseY < posY + h;
    }

    public void setTargetBounds(final float x, final float y, final float width, final float height) {
        startX = this.x;
        startY = this.y;
        startWidth = this.width;
        startHeight = this.height;

        targetX = x;
        targetY = y;
        targetWidth = width;
        targetHeight = height;

        lerpStartTime = System.currentTimeMillis();
    }

    public void setBounds(final float x, final float y, final float width, final float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.targetX = x;
        this.targetY = y;
        this.targetWidth = width;
        this.targetHeight = height;
    }

    public float[] getBounds() {
        float x = this.getX();
        float y = this.getY();
        float width = this.getWidth();
        float height = this.getHeight();

        return new float[] { x, y, width, height };
    }

    public float[] getTargetBounds() {
        float x = this.getTargetX();
        float y = this.getTargetY();
        float width = this.getTargetWidth();
        float height = this.getTargetHeight();

        return new float[] { x, y, width, height };
    }

    protected void updateLerp() {
        long elapsedTime = System.currentTimeMillis() - lerpStartTime;
        float progress = Math.min(1.0f, (float) elapsedTime / lerpDuration);

        if (progress < 1.0f) {
            this.x = lerp(startX, targetX, progress);
            this.y = lerp(startY, targetY, progress);
            this.width = lerp(startWidth, targetWidth, progress);
            this.height = lerp(startHeight, targetHeight, progress);
        } else {
            this.x = targetX;
            this.y = targetY;
            this.width = targetWidth;
            this.height = targetHeight;
        }
    }

    private float lerp(float start, float end, float alpha) {
        return start + (end - start) * alpha;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
    }
}
