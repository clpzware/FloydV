package femcum.modernfloyd.clients.module.impl.render.interfaces;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.TickEvent;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.module.impl.render.Interface;
import femcum.modernfloyd.clients.module.impl.render.interfaces.api.ModuleComponent;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.util.interfaces.ThreadAccess;
import femcum.modernfloyd.clients.util.render.ColorUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class WurstInterface extends Mode<Interface> implements ThreadAccess {

    private Font font;
    private final ResourceLocation resourceLocation = new ResourceLocation("floyd/logo/wurst.png");

    public WurstInterface(String name, Interface parent) {
        super(name, parent);
        font = mc.fontRendererObj;
    }

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (mc == null || mc.gameSettings.showDebugInfo || mc.theWorld == null || mc.thePlayer == null) {
            return;
        }

        this.getParent().setModuleSpacing(this.font.height() + 1);
        this.getParent().setWidthComparator(this.font);
        this.getParent().setEdgeOffset(4);

        // modules in the top right corner of the screen
        for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
            if (moduleComponent.animationTime == 0) {
                continue;
            }

            final double x = moduleComponent.getPosition().getX();
            final double y = moduleComponent.getPosition().getY();
            final Color finalColor = Color.WHITE;

            font.drawWithShadow(moduleComponent.getTranslatedName(), x, y, finalColor.getRGB());
        }

        RenderUtil.rectangle(0, 10, 185, 12, ColorUtil.withAlpha(Color.WHITE, 100));
        RenderUtil.image(resourceLocation, 2, 5.5, 758 / 8.5f, 192 / 8.5f);

        font.draw("v" + Floyd.VERSION + " MC 1.8.9", 95, 14, Color.BLACK.getRGB());
    };

    @EventLink
    public final Listener<TickEvent> onTick = event -> {
        threadPool.execute(() -> {
            // modules in the top right corner of the screen
            for (final ModuleComponent moduleComponent : this.getParent().getActiveModuleComponents()) {
                if (moduleComponent.animationTime == 0) {
                    continue;
                }

                String name = moduleComponent.getTranslatedName();

                moduleComponent.setNameWidth(font.width(name));
                moduleComponent.setTagWidth(0);
                moduleComponent.setTag("");
            }
        });
    };
}
