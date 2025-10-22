package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.render.Render3DEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;

import java.util.ConcurrentModificationException;

import static femcum.modernfloyd.clients.layer.Layers.BLOOM;

@ModuleInfo(aliases = {"module.render.chestesp.name"}, description = "module.render.chestesp.description", category = Category.RENDER)
public final class ChestESP extends Module {

    @EventLink
    public final Listener<Render3DEvent> onRender3D = event -> {
        final Runnable runnable = () -> {
            try {
                mc.theWorld.loadedTileEntityList.forEach(entity -> {
                    if (entity instanceof TileEntityChest || entity instanceof TileEntityEnderChest) {
                        RendererLivingEntity.setShaderBrightness(getTheme().getFirstColor());
                        TileEntityRendererDispatcher.instance.renderBasicTileEntity(entity, event.getPartialTicks());
                        RendererLivingEntity.unsetShaderBrightness();
                    }
                });
            } catch (final ConcurrentModificationException ignored) {
            }
        };

        getLayer(BLOOM).add(runnable);
    };
}