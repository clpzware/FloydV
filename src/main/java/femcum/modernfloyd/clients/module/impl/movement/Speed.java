package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.speed.*;
import femcum.modernfloyd.clients.module.impl.movement.speed.*;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import lombok.Getter;

@ModuleInfo(aliases = {"module.movement.speed.name"}, description = "module.movement.speed.description", category = Category.MOVEMENT)
public class Speed extends Module {

    @Getter
    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaSpeed("Vanilla", this))
            .add(new StrafeSpeed("Strafe", this))
            .add(new InteractSpeed("Interact", this))
            .add(new VulcanSpeed("Vulcan", this))
            .add(new WatchdogSpeed("Watchdog", this))
            .add(new NCPSpeed("NCP", this))
            .add(new FuncraftSpeed("Funcraft", this))
            .add(new VerusSpeed("Verus", this))
            .add(new BlocksMCSpeed("BlocksMC", this))
            .add(new MineMenClubSpeed("MineMenClub", this))
            .add(new KoksCraftSpeed("KoksCraft", this))
            .add(new LegitSpeed("Legit", this))
            .add(new IntaveSpeed("Intave", this))
            .add(new SparkySpeed("Sparky", this))
            .add(new GrimSpeed("Grim", this))
            .setDefault("Vanilla");

    private final BooleanValue disableOnTeleport = new BooleanValue("Disable on Teleport", this, false);
    private final BooleanValue stopOnDisable = new BooleanValue("Stop on Disable", this, false);

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1.0F;

        if (stopOnDisable.getValue()) {
            MoveUtil.stop();
        }
    }

    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        if (disableOnTeleport.getValue()) {
            this.toggle();
        }
    };
}