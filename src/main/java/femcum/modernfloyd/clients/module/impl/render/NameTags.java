package femcum.modernfloyd.clients.module.impl.render;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.impl.other.WorldChangeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.render.nametags.ClassicNameTags;
import femcum.modernfloyd.clients.module.impl.render.nametags.ModernNameTags;
import femcum.modernfloyd.clients.module.impl.render.nametags.VanillaNameTags;
import femcum.modernfloyd.clients.util.font.Font;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@ModuleInfo(aliases = {"module.render.nametags.name"}, description = "module.render.nametags.description", category = Category.RENDER)
public final class NameTags extends Module {

    @Getter
    public final Map<String, Integer> nameWidths = new HashMap<>();
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new ModernNameTags("Modern", this))
            .add(new VanillaNameTags("Vanilla", this)) // This will be added back when I add a system to change modes when interface changes, likeewise with the targethuds
            .add(new ClassicNameTags("Classic", this))
            .setDefault("Modern");
    private final BooleanValue showTargets = new BooleanValue("Targets", this, false);
    public final BooleanValue player = new BooleanValue("Player", this, true, () -> !showTargets.getValue());
    public final BooleanValue invisibles = new BooleanValue("Invisibles", this, false, () -> !showTargets.getValue());
    public final BooleanValue animals = new BooleanValue("Animals", this, false, () -> !showTargets.getValue());
    public final BooleanValue mobs = new BooleanValue("Mobs", this, false, () -> !showTargets.getValue());
    public final BooleanValue teams = new BooleanValue("Player Teammates", this, true, () -> !showTargets.getValue());

    public final Listener<WorldChangeEvent> onWorldChange = event -> nameWidths.clear();

    public float getWidth(String name, Font font) {
        String id = name + font.hashCode();
        if (!nameWidths.containsKey(id)) nameWidths.put(id, font.width(name));
        return nameWidths.get(id);
    }
}