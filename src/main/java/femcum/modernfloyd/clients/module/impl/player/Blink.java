package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.component.impl.player.PingSpoofComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.math.MathUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.BoundsNumberValue;
import net.minecraft.client.entity.EntityOtherPlayerMP;

@ModuleInfo(aliases = {"module.player.blink.name"}, description = "module.player.blink.description", category = Category.PLAYER)
public class Blink extends Module {

    public BooleanValue pulse = new BooleanValue("Pulse", this, false);
    public BoundsNumberValue delay = new BoundsNumberValue("Delay", this, 2, 2, 2, 40, 1, () -> !pulse.getValue());
    public int next;
    private EntityOtherPlayerMP blinkEntity;

    @Override
    public void onEnable() {
        getNext();
    }

    @Override
    public void onDisable() {
        deSpawnEntity();
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> PingSpoofComponent.blink();

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        if (mc.thePlayer.ticksExisted > next && pulse.getValue()) {
            getNext();
            PingSpoofComponent.dispatch();

            deSpawnEntity();
        }
    };

    @EventLink
    public final Listener<WorldChangeEvent> onWorldChange = event -> {
        getNext();
    };

    public void getNext() {
        if (mc.thePlayer == null) return;
        next = mc.thePlayer.ticksExisted + (int) MathUtil.getRandom(delay.getValue().intValue(), delay.getSecondValue().intValue());
    }

    public void deSpawnEntity() {
        if (blinkEntity != null) {
            Floyd.INSTANCE.getBotManager().remove(this, blinkEntity);
            mc.theWorld.removeEntityFromWorld(blinkEntity.getEntityId());
            blinkEntity = null;
        }
    }
}