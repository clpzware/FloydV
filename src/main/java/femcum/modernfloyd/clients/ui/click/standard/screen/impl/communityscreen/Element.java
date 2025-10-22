package femcum.modernfloyd.clients.ui.click.standard.screen.impl.communityscreen;

import femcum.modernfloyd.clients.font.Fonts;
import femcum.modernfloyd.clients.font.Weight;
import femcum.modernfloyd.clients.ui.click.standard.screen.Colors;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.animation.Animation;
import femcum.modernfloyd.clients.util.animation.Easing;
import femcum.modernfloyd.clients.util.dragging.Mouse;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.util.gui.GUIUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.StringUtils;
import rip.vantage.commons.util.time.StopWatch;

import static femcum.modernfloyd.clients.ui.click.standard.screen.impl.CommunityScreen.PADDING;

@Getter
@Setter
public class Element implements Accessor {
    private String title, action;
    private Runnable runnable;
    private Vector2f scale = new Vector2f(65 * 1.33f, 65 * 1.33f);
    private Animation alpha = new Animation(Easing.LINEAR, 200);
    private Animation hover = new Animation(Easing.EASE_OUT_EXPO, 500);
    private Vector2f position;
    private StopWatch update;
    private Font titleFont = Fonts.MAIN.get(20, Weight.REGULAR);

    public Element(String action, String title) {
        this.title = StringUtils.getToFit(titleFont, title, 65 * 1.33f - PADDING * 2);
        this.action = action;
        this.runnable = null;
    }

    public Element(String action, String title, Runnable runnable) {
        title = org.apache.commons.lang3.StringUtils.capitalize(title);
        this.title = StringUtils.getToFit(titleFont, title, 65 * 1.33f - PADDING * 2);
        this.runnable = runnable;
        this.action = action;
    }

    public void render(Vector2f position) {
        this.position = new Vector2f(position.x, position.y);

        if (this.position.x + this.scale.x < getClickGUI().position.x + getClickGUI().sidebar.sidebarWidth
                || this.position.x > getClickGUI().position.x + getClickGUI().sidebar.sidebarWidth + getClickGUI().scale.x) {
            return;
        }

        alpha.run(over() ? 75 : 0);
        hover.run(over() ? 5 : 0);

        RenderUtil.roundedRectangle(this.position.x, this.position.y, scale.x, scale.y, 8, Colors.OVERLAY.get());

        RenderUtil.roundedRectangle(position.x, position.y, scale.x, scale.y,
                8, Colors.OVERLAY.getWithAlpha((int) alpha.getValue()));

        this.position.y += scale.y / 2 - titleFont.height() / 2f + 1 - PADDING / 4f;
        titleFont.drawCentered(title, this.position.x + this.scale.x / 2f, this.position.y, Colors.SECONDARY_TEXT.getRGB());

//        this.position.y += titleFont.height() + PADDING / 2f;
//        Fonts.SF_ROUNDED.get(16, Weight.REGULAR).drawString(subtitle, this.position.x, this.position.y, Colors.TRINARY_TEXT.getRGB());

        this.position.y += titleFont.height() + PADDING / 2f;
        renderAction(this.position);

        this.position = new Vector2f(position.x, position.y);
//        position.y += Fonts.SF_ROUNDED.get(16, Weight.REGULAR).height() + PADDING / 2f + 4;
//        RenderUtil.roundedRectangle(position.x, position.y, PADDING / 8f * 2 + Fonts.SF_ROUNDED.get(16, Weight.REGULAR).width("Install ->"), PADDING / 8f * 2 + Fonts.SF_ROUNDED.get(16, Weight.REGULAR).height(), 5, getTheme().getFirstColor());
//        Fonts.SF_ROUNDED.get(16, Weight.REGULAR).drawString("Install ->", position.x + PADDING / 8, position.y + PADDING / 8, Colors.TEXT.getRGB());
    }

    public void onClick(int mouseX, int mouseY, int mouseButton) {
        if (position == null) return;

        if (GUIUtil.mouseOver(position, scale, mouseX, mouseY)) {
            if (runnable != null) runnable.run();
        }
    }

    public boolean over() {
        return GUIUtil.mouseOver(this.position, this.scale, Mouse.getMouse().getX(), Mouse.getMouse().getY());
    }

    public void renderAction(Vector2f position) {
        Fonts.MAIN.get(16, Weight.REGULAR).drawCentered(action, position.x + scale.x / 2f, position.y, Colors.TRINARY_TEXT.getRGB());
    }
}
