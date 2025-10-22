package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.player.TargetComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.render.Render3DEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.render.ColorUtil;
import femcum.modernfloyd.clients.util.render.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

import java.awt.*;

@ModuleInfo(aliases = {"module.render.tracers.name"}, description = "module.render.tracers.description", category = Category.RENDER)
public final class Tracers extends Module {

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        if (mc.gameSettings.hideGUI) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        mc.entityRenderer.orientCamera(mc.timer.renderPartialTicks);

        for (final Entity player : TargetComponent.getTargets(ProjectionESP.class, true, true, false, false, false)) {
            if (player == mc.thePlayer || player.isDead || Floyd.INSTANCE.getBotManager().contains(player)) {
                continue;
            }

            final double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.getPartialTicks();
            final double y = (player.lastTickPosY + (player.posY - player.lastTickPosY) * event.getPartialTicks()) + 1.62F;
            final double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.getPartialTicks();

            final Color color = ColorUtil.withAlpha(
                    ColorUtil.mixColors(getTheme().getSecondColor(), getTheme().getFirstColor(), Math.min(1, mc.thePlayer.getDistanceToEntity(player) / 50)),
                    128);

            RenderUtil.drawLine(mc.getRenderManager().renderPosX, mc.getRenderManager().renderPosY + mc.thePlayer.getEyeHeight(), mc.getRenderManager().renderPosZ, x, y, z, color, 1.5F);
        }

        GlStateManager.resetColor();
        GlStateManager.popMatrix();
    };
}