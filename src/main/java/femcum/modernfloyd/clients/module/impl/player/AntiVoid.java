package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.player.antivoid.*;
import femcum.modernfloyd.clients.module.impl.player.antivoid.*;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.player.antivoid.name"}, description = "module.player.antivoid.description", category = Category.PLAYER)
public class AntiVoid extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new PacketAntiVoid("Packet", this))
            .add(new PositionAntiVoid("Position", this))
            .add(new VulcanAntiVoid("Vulcan", this))
            .add(new CollisionAntiVoid("Collision", this))
            .add(new WatchdogAntiVoid("Watchdog", this))
            .add(new BlinkAntiVoid("Blink", this))
            .setDefault("Packet");
}