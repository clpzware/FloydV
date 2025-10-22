package femcum.modernfloyd.clients.module.impl.combat;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.combat.antibot.*;
import femcum.modernfloyd.clients.value.impl.BooleanValue;

@ModuleInfo(aliases = {"module.combat.antibot.name"}, description = "module.combat.antibot.description", category = Category.COMBAT)
public final class AntiBot extends Module {

    private final BooleanValue funcraftAntiBot = new BooleanValue("Funcraft Check", this, false,
            new FuncraftAntiBot("", this));

    private final BooleanValue ncps = new BooleanValue("NPC Detection Check", this, false,
            new NPCAntiBot("", this));

    private final BooleanValue duplicate = new BooleanValue("Duplicate Name Check", this, false,
            new DuplicateNameCheck("", this));

    private final BooleanValue ping = new BooleanValue("No Ping Check", this, false,
            new PingCheck("", this));

    private final BooleanValue negativeIDCheck = new BooleanValue("Negative Unique ID Check", this, false,
            new NegativeIDCheck("", this));

    private final BooleanValue duplicateIDCheck = new BooleanValue("Duplicate Unique ID Check", this, false,
            new DuplicateIDCheck("", this));

    private final BooleanValue ticksVisible = new BooleanValue("Time Visible Check", this, false,
            new TicksVisibleCheck("", this));

    private final BooleanValue middleClick = new BooleanValue("Middle Click Bot", this, false,
            new MiddleClickBot("", this));

    @Override
    public void onDisable() {
        Floyd.INSTANCE.getBotManager().clear();
    }
}
