package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.util.chat.ChatUtil;

public final class Say extends Command {

    public Say() {
        super("command.say.description", "say", "chat");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length <= 1) {
            error(String.format(".%s <message>", args[0]));
        } else {
            ChatUtil.send(String.join(" ", args).substring(3).trim());
            ChatUtil.display("command.say.sent");
        }
    }
}
