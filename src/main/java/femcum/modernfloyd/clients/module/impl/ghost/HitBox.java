package femcum.modernfloyd.clients.module.impl.ghost;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.RightClickEvent;
import femcum.modernfloyd.clients.event.impl.render.MouseOverEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.RayCastUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;

@ModuleInfo(aliases = {"module.ghost.hitbox.name"}, description = "module.ghost.hitbox.description", category = Category.GHOST)
public class HitBox extends Module {
    public final NumberValue expand = new NumberValue("Expand Amount", this, 0, 0, 6, 0.01);
    private final BooleanValue effectRange = new BooleanValue("Effect range", this, true);

    @EventLink
    public final Listener<MouseOverEvent> onMouseOver = event -> {
        event.setExpand(this.expand.getValue().floatValue());

        if (!this.effectRange.getValue()) {
            event.setRange(event.getRange() - expand.getValue().doubleValue());
        }
    };

    @EventLink
    public final Listener<RightClickEvent> onRightClick = event ->
            mc.objectMouseOver = RayCastUtil.rayCast(RotationComponent.rotations, 4.5);
}