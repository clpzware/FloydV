package cheadleware.module.modules.Render;

import cheadleware.event.EventTarget;
import cheadleware.event.types.EventType;
import cheadleware.events.Render3DEvent;
import cheadleware.module.Module;
import cheadleware.event.types.EventType;
import cheadleware.property.properties.IntProperty;
import net.minecraft.client.Minecraft;

public class Ambience extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final IntProperty time = new IntProperty("Time", 0, 0, 22999, () -> true);

    public Ambience() {
        super("Ambience", true, true);
    }

    @EventTarget
    public void onTick(Render3DEvent event) {
        if (this.isEnabled()) {
            if (mc.theWorld != null) {
                long worldTime = time.getValue();
                mc.theWorld.setWorldTime(worldTime);
            }
        }
    }

    @Override
    public void onEnabled() {
        // Module enabled
    }

    @Override
    public void onDisabled() {
        // Module disabled - server will resync time naturally
    }
}