package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import net.minecraft.client.gui.GuiScreen;
public final class Name extends Command {

    public Name() {
        super("command.name.description", "name", "ign", "username", "nick", "nickname");
    }

    @Override
    public void execute(final String[] args) {
        final String name = PlayerUtil.name();

        GuiScreen.setClipboardString(name);
        ChatUtil.display("command.name.copied", name);
    }
}
