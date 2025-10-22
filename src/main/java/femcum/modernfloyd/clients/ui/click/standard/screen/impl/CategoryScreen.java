package femcum.modernfloyd.clients.ui.click.standard.screen.impl;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.ui.click.standard.RiseClickGUI;
import femcum.modernfloyd.clients.ui.click.standard.components.ModuleComponent;
import femcum.modernfloyd.clients.ui.click.standard.screen.Screen;
import femcum.modernfloyd.clients.util.Accessor;
import femcum.modernfloyd.clients.util.gui.ScrollUtil;
import femcum.modernfloyd.clients.util.vector.Vector2d;
import lombok.Getter;
import lombok.Setter;
import rip.vantage.commons.util.time.StopWatch;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
public final class CategoryScreen implements Screen, Accessor {

    private final StopWatch stopwatch = new StopWatch();

    public ScrollUtil scrollUtil = new ScrollUtil();
    public ArrayList<ModuleComponent> relevantModules;
    public Category category;
    private double endOfList, startOfList;

    @Override
    public void onRender(final int mouseX, final int mouseY, final float partialTicks) {
        if (this.category == null) return;

        final RiseClickGUI clickGUI = this.getClickGUI();

        /* Scroll */
        scrollUtil.onRender();

        /* Draws modules in search */
        double positionY = clickGUI.position.y + 7 + scrollUtil.getScroll();
        startOfList = positionY;

        /* Draws all modules */
        double height = 0;

        for (final ModuleComponent module : this.relevantModules) {
            module.draw(new Vector2d(clickGUI.position.x + clickGUI.sidebar.sidebarWidth + 8, positionY), mouseX, mouseY, partialTicks);
            positionY += module.scale.y + 7;
            height += module.scale.y + 7;
        }

        endOfList = positionY;

        double padding = 7;
        double scrollX = clickGUI.getPosition().getX() + clickGUI.getScale().getX() - 4;
        double scrollY = clickGUI.getPosition().getY() + padding;

        scrollUtil.renderScrollBar(new Vector2d(scrollX, scrollY), getClickGUI().scale.y - padding * 2);

        scrollUtil.setMax(-height + clickGUI.scale.y - 7);
        stopwatch.reset();
    }

    @Override
    public void onKey(final char typedChar, final int keyCode) {
        for (final ModuleComponent module : this.getRelevantModules()) {
            module.key(typedChar, keyCode);
        }
    }

    @Override
    public void onClick(final int mouseX, final int mouseY, final int mouseButton) {
        if (relevantModules == null) return;

        for (final ModuleComponent moduleComponent : relevantModules) {
            moduleComponent.click(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void onMouseRelease() {
        if (this.category == null) return;

        for (final ModuleComponent module : this.getRelevantModules()) {
            module.released();
        }
    }

    @Override
    public void onBloom() {
        if (this.category == null) return;

        for (final ModuleComponent module : this.getRelevantModules()) {
            module.bloom();
        }
    }

    @Override
    public void onInit() {
        this.category = this.getCategory();
        if (this.category == null) return;

        this.relevantModules = Floyd.INSTANCE.getClickGUI().getModuleList().stream()
                .filter((module) -> module.getModule().getModuleInfo().category() == this.category)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Category getCategory() {
        for (final Category category : Category.values()) {
            if (category.getClickGUIScreen() == getClickGUI().getSelectedScreen()) {
                return category;
            }
        }

        return null;
    }
}
