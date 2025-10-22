package femcum.modernfloyd.clients.ui.click.standard.screen.impl;

import femcum.modernfloyd.clients.font.Fonts;
import femcum.modernfloyd.clients.font.Weight;
import femcum.modernfloyd.clients.ui.click.standard.components.language.LanguageComponent;
import femcum.modernfloyd.clients.ui.click.standard.screen.Colors;
import femcum.modernfloyd.clients.ui.click.standard.screen.Screen;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.gui.ScrollUtil;
import femcum.modernfloyd.clients.util.localization.Locale;
import femcum.modernfloyd.clients.util.localization.Localization;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;

public class LanguageScreen implements Screen, Accessor {

    private final ArrayList<LanguageComponent> languages = new ArrayList<>();
    private final ScrollUtil scrollUtil = new ScrollUtil();

    public LanguageScreen() {
        for (Locale locale : Locale.values()) {
            languages.add(new LanguageComponent(locale, Localization.get("language_local", locale),
                    Localization.get("language_english", locale)));
        }
    }

    @Override
    public void onRender(int mouseX, int mouseY, float partialTicks) {
        scrollUtil.onRender();

        final Vector2f position = getClickGUI().getPosition();
        final Vector2f scale = getClickGUI().getScale();
        final double sidebar = getClickGUI().getSidebar().sidebarWidth;

        for (int i = 0; i < this.languages.size(); i++) {
            this.languages.get(i).draw((i + 1) * 46 + scrollUtil.getScroll());
        }

        RenderUtil.roundedRectangle(position.getX() + sidebar, position.getY(), scale.x - sidebar, 40, getClickGUI().round, Colors.BACKGROUND.get(), true, true, false, false);
//        RenderUtil.rectangle(position.getX() + sidebar, position.getY() + 20, scale.x - sidebar, 20, Colors.BACKGROUND.get());

//        FontManager.getIconsThree(28).drawString("4",
//                position.getX() + sidebar + 14, position.getY() + 16, -1);
        Fonts.MAIN.get(16, Weight.REGULAR).drawRight(Localization.get("ui.language.text"),
                position.getX() + scale.getX() - 20, position.getY() + 20, new Color(255, 255, 255, 128).getRGB());

        scrollUtil.setMax(-2000);
    }

    @Override
    public void onClick(int mouseX, int mouseY, int mouseButton) {
        for (LanguageComponent component : this.languages) {
            component.click(mouseX, mouseY);
        }
    }
}