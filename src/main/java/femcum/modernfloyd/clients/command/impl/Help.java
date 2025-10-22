package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.localization.Localization;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public final class Help extends Command {

    public Help() {
        super("command.help.description", "help", "?");
    }

    @Override
    public void execute(final String[] args) {
        Floyd.INSTANCE.getCommandManager().getCommandList()
                .forEach(command -> ChatUtil.display(StringUtils.capitalize(command.getExpressions()[0]) + " " + Arrays.toString(command.getExpressions()) + " \2478» \2477" + Localization.get(command.getDescription())));
    }
}