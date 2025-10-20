package fr.ambient.anticheat;

import fr.ambient.Ambient;
import fr.ambient.anticheat.check.*;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.NetworkBlockPlaceEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.module.impl.misc.Anticheat;

import java.util.ArrayList;

public class AnticheatManager {

    private final ArrayList<Check> checks = new ArrayList<>();

    public AnticheatManager() {
        Anticheat anticheat = Ambient.getInstance().getModuleManager().getModule(Anticheat.class);

        checks.add(new Autoblock(anticheat));
        checks.add(new NoslowA(anticheat));
        checks.add(new NoslowB(anticheat));
        checks.add(new ScaffoldA(anticheat));
        checks.add(new ScaffoldB(anticheat));


        Ambient.getInstance().getEventBus().register(this);
    }

    @SubscribeEvent
    private void onEventPlayerTickBULLSHITUpdate(UpdateEvent event) {
        if (!Ambient.getInstance().getModuleManager().getModule(Anticheat.class).isEnabled()) {
            return;
        }
        checks.forEach(Check::onUpdate);
    }

    @SubscribeEvent
    private void onPacketEvent(PacketReceiveEvent event) {
        if (!Ambient.getInstance().getModuleManager().getModule(Anticheat.class).isEnabled()) {
            return;
        }
        checks.forEach(p -> p.onPacket(event.getPacket()));
    }

    @SubscribeEvent
    private void onBlockPlace(NetworkBlockPlaceEvent event) {
        if (!Ambient.getInstance().getModuleManager().getModule(Anticheat.class).isEnabled()) {
            return;
        }
        checks.forEach(e -> e.onBlockMod(event.getPos(), event.getState()));
    }
}
