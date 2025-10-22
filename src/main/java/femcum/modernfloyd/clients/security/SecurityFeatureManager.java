package femcum.modernfloyd.clients.security;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.packet.PacketReceiveEvent;
import femcum.modernfloyd.clients.module.impl.other.AntiCrash;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import org.reflections.Reflections;

import java.util.ArrayList;

public final class SecurityFeatureManager extends ArrayList<SecurityFeature> {

    private AntiCrash features;

    public SecurityFeatureManager() {
        super();
    }

    public void init() {
        Floyd.INSTANCE.getEventBus().register(this);

        this.features = Floyd.INSTANCE.getModuleManager().get(AntiCrash.class);

        if (this.features == null) return;

        final Reflections reflections = new Reflections("femcum.modernfloyd.clients.security.impl");

        reflections.getSubTypesOf(SecurityFeature.class).forEach(clazz -> {
            try {
                this.add(clazz.getConstructor().newInstance());
            } catch (final Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public boolean isInsecure(final Packet<?> packet) {
        // Notification
        return this.features != null && this.features.isEnabled()
                && !Minecraft.getMinecraft().isSingleplayer()
                && this.stream().anyMatch(feature -> feature.handle(packet));
    }

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = event -> {
        event.setCancelled(isInsecure(event.getPacket()));
    };
}
