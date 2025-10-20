package fr.ambient.command;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.input.InputChatEvent;
import fr.ambient.structure.Manager;
import fr.ambient.util.player.ChatUtil;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CommandManager extends Manager<Command> {
    @SubscribeEvent
    private void onInputChatEvent(InputChatEvent event) {
        String message = event.getMessage();

        if (message.startsWith(".")) {
            message = message.substring(1);
            final String[] args = message.split(" ");

            final AtomicBoolean commandFound = new AtomicBoolean(false);

            try {
                String finalMessage = message;
                getObjects().stream().filter(command ->
                                Arrays.stream(command.getAliases())
                                        .anyMatch(expression -> expression.equalsIgnoreCase(args[0])))
                        .forEach(cmd -> {
                            commandFound.set(true);
                            cmd.execute(args, finalMessage);
                        });
            } catch (final Exception ignored) { /* */
                ignored.printStackTrace();
            }

            if (!commandFound.get())
                ChatUtil.display("command not found.");

            event.setCancelled(true);
        }
        if(message.startsWith("#")){
            message = message.substring(1);
            Ambient.getInstance().getIrc().sendMessageTo("thisdoesntmatter", message);
            event.setCancelled(true);
        }
    }

    public <T extends Command> T getCommand(final Class<T> clazz) {
        return (T) this.getBy(command -> Objects.equals(command.getClass(), clazz));
    }
}