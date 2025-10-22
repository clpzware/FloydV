package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.render.sessioninfo.RueSessionStats;
import femcum.modernfloyd.clients.util.vector.Vector2d;
import femcum.modernfloyd.clients.value.impl.DragValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.render.sessionstats.name"}, description = "module.render.sessionstats.description", category = Category.RENDER)
public final class SessionStats extends Module {
    private final DragValue position = new DragValue("", this, new Vector2d(100, 200), true);

    private final ModeValue modeValue = new ModeValue("Mode", this)
            .add(new RueSessionStats("Rue", this))
            .setDefault("Standard");

    public DragValue getPosition() {
        return this.position;
    }
}