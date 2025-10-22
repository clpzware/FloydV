package femcum.modernfloyd.clients.module.impl.render.targetinfo;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.render.Render2DEvent;
import femcum.modernfloyd.clients.module.impl.render.TargetInfo;
import femcum.modernfloyd.clients.util.math.MathUtil;
import femcum.modernfloyd.clients.util.render.ColorUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import femcum.modernfloyd.clients.util.vector.Vector2d;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;

import java.awt.*;

public class WurstTargetInfo extends Mode<TargetInfo> {
    public WurstTargetInfo(String name, TargetInfo parent) {
        super(name, parent);
    }

    private TargetInfo targetInfoModule;

    @EventLink
    public final Listener<Render2DEvent> onRender2D = event -> {
        if (this.targetInfoModule == null) {
            this.targetInfoModule = this.getModule(TargetInfo.class);
        }

        Entity target = this.targetInfoModule.target;
        boolean out = (!this.targetInfoModule.inWorld || this.targetInfoModule.stopwatch.finished(1000));

        if (target == null || out) return;

        String name = target.getCommandSenderName();

        double x = this.targetInfoModule.position.x;
        double y = this.targetInfoModule.position.y;

        RenderUtil.rectangle(x, y, 185, 34, ColorUtil.withAlpha(Color.WHITE, 100));
        mc.fontRendererObj.draw("Name: " + name, x + 4, y + 4, Color.BLACK.getRGB());

        this.targetInfoModule.positionValue.scale = new Vector2d(185, 50);
        double health = Math.min(!this.targetInfoModule.inWorld ? 0 : MathUtil.round(((AbstractClientPlayer) target).getHealth(), 1), ((AbstractClientPlayer) target).getMaxHealth());
        RenderUtil.rectangle(x + 4, y + 16, (185 - 8) * (health / ((AbstractClientPlayer) target).getMaxHealth()), 10, Color.ORANGE);
    };
}
