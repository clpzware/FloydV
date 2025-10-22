package femcum.modernfloyd.clients.command;

import femcum.modernfloyd.clients.Floyd;
import femcum.modernfloyd.clients.command.impl.*;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.input.ChatInputEvent;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
public final class CommandManager {

    @Getter
    private final List<Command> commandList = new ArrayList<>();

    /**
     * Called on client start
     */
    public void init() {
        this.add(new Bind());
        this.add(new Clip());
        this.add(new Config());
        this.add(new Friend());
        this.add(new Help());
        this.add(new Insults());
        this.add(new Name());
        this.add(new Panic());
        this.add(new Say());
        this.add(new Script());
        this.add(new Stuck());
        this.add(new Toggle());
        this.add(new SpotifyCmd());
        this.add(new Target());

        Floyd.INSTANCE.getEventBus().register(this);
    }

    public void add(Command command) {
        this.commandList.add(command);
    }

    public <T extends Command> T get(final String name) {
        // noinspection unchecked
        return (T) this.commandList.stream()
                .filter(command -> Arrays.stream(command.getExpressions())
                        .anyMatch(expression -> expression.equalsIgnoreCase(name))
                ).findAny().orElse(null);
    }

    @EventLink
    public final Listener<ChatInputEvent> onChatInput = event -> {
        String message = event.getMessage();

        if (!message.startsWith(".")) return;

        message = message.substring(1);
        final String[] args = message.split(" ");

        final AtomicBoolean commandFound = new AtomicBoolean(false);

        try {
            this.commandList.stream()
                    .filter(command ->
                            Arrays.stream(command.getExpressions())
                                    .anyMatch(expression ->
                                            expression.equalsIgnoreCase(args[0])))
                    .forEach(command -> {
                        commandFound.set(true);
                        command.execute(args);
                    });
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        if (!commandFound.get()) {
            ChatUtil.display("command.unknown");
        }

        event.setCancelled();
    };
}