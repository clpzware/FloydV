package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.movement.step.*;
import femcum.modernfloyd.clients.module.impl.movement.step.*;
import femcum.modernfloyd.clients.value.impl.ModeValue;

@ModuleInfo(aliases = {"module.movement.step.name"}, description = "module.movement.step.description", category = Category.MOVEMENT)
public class Step extends Module {

    private final ModeValue mode = new ModeValue("Mode", this)
            .add(new VanillaStep("Vanilla", this))
            .add(new NCPStep("NCP", this))
            .add(new NewNCPStep("New NCP", this))
            .add(new NCPPacketLessStep("NCP Packetless", this))
            .add(new VulcanStep("Vulcan", this))
            .add(new MatrixStep("Matrix", this))
            .add(new JumpStep("Jump", this))
            .add(new WatchdogStep("Watchdog", this))
            .setDefault("Vanilla");
}