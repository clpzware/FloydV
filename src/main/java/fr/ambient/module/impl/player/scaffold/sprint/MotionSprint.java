package fr.ambient.module.impl.player.scaffold.sprint;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.move.MovementEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.util.player.MoveUtil;

public class MotionSprint extends ModuleMode {
    public MotionSprint(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onUpdate(MovementEvent event) {
        if (!MoveUtil.moving()) return;
        MoveUtil.strafeWithEvent(event, ((Scaffold) this.getSuperModule()).sprintMotion.getValue());
    }
}
