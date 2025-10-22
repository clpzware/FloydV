package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.command.Command;

public final class Panic extends Command {

    public Panic() {
        super("command.panic.description", "panic", "p");
    }

    @Override
    public void execute(final String[] args) {
        Floyd.INSTANCE.getModuleManager().getAll().stream().filter(module ->
                !module.getModuleInfo().autoEnabled()).forEach(module -> module.setEnabled(false));
    }
}