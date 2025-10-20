package fr.ambient.util;

import fr.ambient.util.math.TimeUtil;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.arikia.dev.drpc.DiscordUser;
import net.arikia.dev.drpc.callbacks.ReadyCallback;

public class DiscordRP {


    public long created = System.currentTimeMillis();
    private boolean running = true;
    private TimeUtil delay = new TimeUtil();
    public void start(){
        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().setReadyEventHandler(new ReadyCallback() {
            @Override
            public void apply(DiscordUser arg0) {
                update("Booting Up...","");
            }
        }).build();

        DiscordRPC.discordInitialize("1350265158114738206", handlers, running);

        new Thread("Discord RPC Callback") {
            public void run() {
                while(running) {
                    DiscordRPC.discordRunCallbacks();

                    if(delay.finished(500)){
                        // update here niglet
                        delay.reset();
                    }

                }
            };
        }.start();
    }
    public void update(String firstLine, String secondline) {
        DiscordRichPresence.Builder b = new DiscordRichPresence.Builder(secondline);
        b.setBigImage("logo2", "Hi");
        b.setDetails(firstLine);
        b.setStartTimestamps(created);

        DiscordRPC.discordUpdatePresence(b.build());
    }
    public void shutdown(){
        running = false;
        DiscordRPC.discordShutdown();
    }
}
