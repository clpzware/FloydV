package fr.ambient.command.impl;

import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.theme.Theme;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;

public class ThemeCommand extends Command {

    public ThemeCommand() {
        super("theme", "th");
    }

    @Override
    public void execute(String[] args, String message) {
        String[] words = message.split(" ");

        if (words.length < 1 || words.length > 3) {
            ChatUtil.display("Invalid arguments! Usage: .theme <list|set|reload> [name]");
            return;
        }

        String option = words[1].toLowerCase();

        switch (option) {
            case "list":
                ChatUtil.display("-- Themes --");
                for (Theme t : Ambient.getInstance().getThemeManager()) {
                    ChatComponentText themeComponent = new ChatComponentText("§7[§9Ambient§7]§f » §7" + t.getChatFormatting() + t.getName());
                    themeComponent.setChatStyle(new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".theme set " + t.getName())));
                    themeComponent.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(t.getChatFormatting() + t.getName())));
                    Minecraft.getMinecraft().thePlayer.addChatMessage(themeComponent);
                }
                break;
            case "set":
                if (words.length < 3) {
                    ChatUtil.display("Please specify a theme name!");
                    return;
                }
                String themeName = words[2].toLowerCase();
                Theme t = Ambient.getInstance().getThemeManager().getThemeByName(themeName);

                if (t != null) {
                    Ambient.getInstance().getHud().setCurrentTheme(t);
                    ChatUtil.display("Theme set to: " + t.getName());
                } else {
                    ChatUtil.display("Theme not found: " + themeName);
                }
                break;
            case "reload":
                ChatUtil.display("Reloading...");
                Ambient.getInstance().getCustomThemeManager().load();
                ChatUtil.display("Reloaded from file dog/customthemes.json");
                break;
            default:
                ChatUtil.display("Invalid option! Usage: .theme <list|set> [name]");
                break;
        }
    }
}
