package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.other.clientspoofer.LabyMod;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.other.clientspoofer.name"}, description = "module.other.clientspoofer.description", category = Category.PLAYER)
public final class ClientSpoofer extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new LabyMod("LabyMod", this))
            .setDefault("LabyMod");

}
