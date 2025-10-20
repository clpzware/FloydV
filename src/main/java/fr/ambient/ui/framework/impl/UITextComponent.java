package fr.ambient.ui.framework.impl;

import fr.ambient.ui.framework.TextAlignment;
import fr.ambient.ui.framework.UIComponent;
import fr.ambient.util.render.font.TTFFontRenderer;

public class UITextComponent extends UIComponent {

    private String text = "";
    private TTFFontRenderer font;
    private boolean shadow;
    private TextAlignment alignment = TextAlignment.LEFT;

    public UITextComponent text(String text) {
        this.text = text;
        return this;
    }

    public UITextComponent alignment(TextAlignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public UITextComponent font(TTFFontRenderer font) {
        this.font = font;
        return this;
    }

    public UITextComponent shadow() {
        this.shadow = true;
        return this;
    }

    @Override
    public void render() {
        if (alignment == TextAlignment.LEFT) {
            font.drawString(text, x, y, color1.getRGB());
        }

        if (alignment == TextAlignment.CENTER) {
            font.drawCenteredString(text, x, y, color1.getRGB());
        }

        if (alignment == TextAlignment.RIGHT) {
            font.drawString(text, x + width - font.getWidth(text), y, color1.getRGB());
        }
    }
}
