package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.GameEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

@ModuleInfo(aliases = {"Bed Plates"}, description = "bedplates", category = Category.RENDER)
public class BedPlates extends Module {

    @EventLink
    private final Listener<GameEvent> onGameEvent = event -> {
        for (int x = 0; x < mc.theWorld.getActualHeight(); x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < mc.theWorld.getWorldBorder().getSize(); z++) {
                    final BlockPos pos = new BlockPos(x, y, z);

                    if (mc.theWorld.getBlockState(pos).getBlock() == Blocks.bed) {
                        ChatUtil.display("Found bed at: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                    }
                    int posX = pos.getX();

                    for (int i = 0; i < 3; i++) {
                        posX++;
                    }
                }
            }
        }
    };
}
