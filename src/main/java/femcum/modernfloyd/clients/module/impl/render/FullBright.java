package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.render.fullbright.EffectFullBright;
import femcum.modernfloyd.clients.module.impl.render.fullbright.GammaFullBright;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.render.fullbright.name"}, description = "module.render.fullbright.description", category = Category.RENDER)
public final class FullBright extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new GammaFullBright("Gamma", this))
            .add(new EffectFullBright("Effect", this))
            .setDefault("Gamma");
}