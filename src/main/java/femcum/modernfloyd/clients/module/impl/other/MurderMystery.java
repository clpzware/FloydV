package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(aliases = {"module.other.murdermystery.name"}, description = "module.other.murdermystery.description", category = Category.PLAYER)
public final class MurderMystery extends Module {

    private EntityPlayer murderer;

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        // no need to waste performance so every second tick is enough
        if (mc.thePlayer.ticksExisted % 2 == 0 || this.murderer != null) {
            return;
        }

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player.getHeldItem() != null) {
                if (player.getHeldItem().getDisplayName().contains("Knife")) { // TODO: add other languages
                    ChatUtil.display(PlayerUtil.name(player) + " is The Murderer.");
                    this.murderer = player;
                }
            }
        }
    };

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> this.murderer = null;
}
