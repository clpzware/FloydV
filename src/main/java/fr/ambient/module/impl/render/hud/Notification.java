package fr.ambient.module.impl.render.hud;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.render.Render2DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.notification.NotificationManager;
import fr.ambient.property.impl.ModeProperty;

public class Notification extends Module {

    private final ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"Modern"}, "Modern");
    public Notification() {
        super(56,"Displays pop-up alerts for specific events or actions.", ModuleCategory.RENDER);
        this.registerProperties(mode);
    }

    @SubscribeEvent
    private void onRender2D(Render2DEvent event) {
        NotificationManager.update(mode.getValue());
    }
}