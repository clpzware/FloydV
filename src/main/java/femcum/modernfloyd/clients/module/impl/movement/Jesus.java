package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.JumpEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.jesus.*;
import femcum.modernfloyd.clients.module.impl.movement.jesus.*;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.jesus.name"}, description = "module.movement.jesus.description", category = Category.MOVEMENT)
public class Jesus extends Module {

    public final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaJesus("Vanilla", this))
            .add(new GravityJesus("Vulcan Gravity", this))
            .add(new KarhuJesus("Karhu", this))
            .add(new NCPJesus("NCP", this))
            .add(new WatchdogJesus("Watchdog", this))
            .add(new VulcanJesus("Vulcan Dolphin", this))
            .setDefault("Vanilla");

    private final BooleanValue allowJump = new BooleanValue("Allow Jump", this, true);

    @EventLink
    public final Listener<JumpEvent> onJump = event -> {
        if (!allowJump.getValue() && PlayerUtil.onLiquid()) {
            event.setCancelled();
        }
    };
}