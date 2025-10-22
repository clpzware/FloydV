package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.bindable.Bindable;
import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;
public final class Bind extends Command {

    public Bind() {
        super("command.bind.description", "bind", "binds", "keybind");
    }

    @Override
    public void execute(final String[] args) {
        if (args.length == 3) {
            final Bindable bindable = Floyd.INSTANCE.getBindableManager().get(args[1]);

            if (bindable == null) {
                ChatUtil.display("command.bind.invalidmodule");
                return;
            }

            final String inputCharacter = args[2].toUpperCase();
            final int keyCode = Keyboard.getKeyIndex(inputCharacter);

            bindable.setKey(keyCode);
            ChatUtil.display("Bound " + bindable.getName() + " to " + Keyboard.getKeyName(keyCode) + ".");
            ChatUtil.display("Remember you can bind configs by doing .bind configname key");
        } else if (args.length == 2 && args[1].equalsIgnoreCase("list")) {

            ChatUtil.display("Displaying all active binds");

            Floyd.INSTANCE.getBindableManager().getBinds().forEach(module -> {
                if (module.getKey() != 0) {
                    final String color = getTheme().getChatAccentColor().toString();

                    final ChatComponentText chatText = new ChatComponentText(color + "> " + module.getAliases()[0] + "§f " + Keyboard.getKeyName(module.getKey()));
                    final ChatComponentText hoverText = new ChatComponentText("Click to remove " + module.getAliases()[0] + " bind");

                    chatText.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".bind " + module.getName().replace(" ", "") + " none"))
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));

                    mc.thePlayer.addChatMessage(chatText);
                }
            });

        } else {
            error(".bind <list/module/config> (KEY)");
        }
    }
}