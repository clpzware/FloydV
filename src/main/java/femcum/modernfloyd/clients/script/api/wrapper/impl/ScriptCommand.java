package femcum.modernfloyd.clients.script.api.wrapper.impl;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.script.api.wrapper.ScriptHandlerWrapper;

public final class ScriptCommand extends ScriptHandlerWrapper<Command> {

    public ScriptCommand(final Command wrapped) {
        super(wrapped);
    }

    public void unregister() {
        Floyd.INSTANCE.getCommandManager().getCommandList().remove(this.wrapped);
    }

    // TODO: Make command execution again

    public String getName() {
        return this.wrapped.getExpressions()[0];
    }

    public String getDescription() {
        return this.wrapped.getDescription();
    }
}
