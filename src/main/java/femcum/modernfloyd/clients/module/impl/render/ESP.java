package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.render.esp.ChamsESP;
import femcum.modernfloyd.clients.module.impl.render.esp.GlowESP;
import femcum.modernfloyd.clients.value.impl.BooleanValue;

@ModuleInfo(aliases = {"module.render.esp.name"}, description = "module.render.esp.description", category = Category.RENDER)
public final class ESP extends Module {

    private final BooleanValue glowESP = new BooleanValue("Glow", this, false, new GlowESP("", this));
    private final BooleanValue chamsESP = new BooleanValue("Chams", this, false, new ChamsESP("", this));
    public final BooleanValue staticColorESP = new BooleanValue("Static Color", this, false, () -> !chamsESP.getValue());

}
