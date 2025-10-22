package femcum.modernfloyd.clients.module.impl.player;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.player.nofall.*;
import femcum.modernfloyd.clients.module.impl.player.nofall.*;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.player.nofall.name"}, description = "module.player.nofall.description", category = Category.PLAYER)
public class NoFall extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new SpoofNoFall("Spoof", this))
            .add(new FlagNoFall("Flag", this))
            .add(new NoGroundNoFall("No Ground", this))
            .add(new RoundNoFall("Round", this))
            .add(new PlaceNoFall("Place", this))
            .add(new PacketNoFall("Packet", this))
            .add(new InvalidNoFall("Invalid", this))
            .add(new ChunkLoadNoFall("Chunk Load", this))
            .add(new ClutchNoFall("Clutch", this))
            .add(new MatrixNoFall("Matrix", this))
            .add(new WatchdogNoFall("Watchdog Combo", this))
            .add(new VulcanAndVerusNoFall("Watchdog Packet", this))
            .setDefault("Spoof");
}
