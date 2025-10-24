package femcum.modernfloyd.clients.util.discordutil;

import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class DiscordRPCUtil {
    private boolean running = false;
    private long created = 0;
    private Thread callbackThread;
    private Thread updateThread;
    private boolean initialized = false;

    // Track last state to avoid unnecessary updates
    private String lastDetails = "";
    private String lastState = "";

    public String dcUsername = "";

    // Settings
    private boolean showServer = true;
    private boolean showTimestamp = true;
    private boolean showDetails = true;

    public void start() {
        if (initialized) {
            System.out.println("[Floyd RPC] Already initialized, skipping...");
            return;
        }

        this.created = System.currentTimeMillis();
        this.running = true;

        DiscordEventHandlers handlers = new DiscordEventHandlers.Builder()
                .setReadyEventHandler(discordUser -> {
                    dcUsername = discordUser.username;
                    startAutoUpdate();
                })
                .setDisconnectedEventHandler((errorCode, message) -> {
                    System.err.println("[Floyd RPC] Disconnected: " + errorCode + " - " + message);
                })
                .setErroredEventHandler((errorCode, message) -> {
                    System.err.println("[Floyd RPC] Error: " + errorCode + " - " + message);
                })
                .build();

        System.out.println("[Floyd RPC] Initializing...");

        try {
            DiscordRPC.discordInitialize("1431059463011438711", handlers, true);
            initialized = true;

            callbackThread = new Thread("Discord RPC Callback") {
                @Override
                public void run() {
                    while (running) {
                        try {
                            DiscordRPC.discordRunCallbacks();
                        } catch (Exception e) {
                            System.err.println("[Floyd RPC] Callback error: " + e.getMessage());
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            };
            callbackThread.setDaemon(true);
            callbackThread.start();

            // Start update thread immediately (don't wait for Discord ready event)
            // This ensures updates work even if Discord connection is slow
            Thread startupThread = new Thread("Discord-RPC-Startup") {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000); // Wait 3 seconds for Discord to connect
                        if (updateThread == null || !updateThread.isAlive()) {
                            startAutoUpdate();
                        }
                    } catch (Exception e) {
                        System.err.println("[Floyd RPC] Manual update start failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            };
            startupThread.setDaemon(true);
            startupThread.start();
        } catch (Exception e) {
            System.err.println("[Floyd RPC] Failed to initialize: " + e.getMessage());
            e.printStackTrace();
            running = false;
            initialized = false;
        }
    }

    /**
     * Starts automatic presence updates every tick (50ms) for instant updates
     */
    private void startAutoUpdate() {
        if (updateThread != null && updateThread.isAlive()) {
            return; // Already running
        }

        updateThread = new Thread("Discord RPC Auto-Update") {
            @Override
            public void run() {
                // Initial update
                try {
                    Thread.sleep(500); // Wait 500ms before first update
                } catch (InterruptedException e) {
                    return;
                }

                int updateCount = 0;
                while (running && initialized) {
                    try {
                        update();
                        updateCount++;
                    } catch (Exception e) {
                        System.err.println("[Floyd RPC] Auto-update error: " + e.getMessage());
                        e.printStackTrace();
                    }

                    try {
                        Thread.sleep(50); // Update every 50ms (20 times per second - Minecraft tick rate)
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        };
        updateThread.setDaemon(true);
        updateThread.start();
    }

    public void shutdown() {
        if (!initialized) return;

        running = false;

        // Stop update thread
        if (updateThread != null && updateThread.isAlive()) {
            updateThread.interrupt();
            try {
                updateThread.join(2000);
            } catch (InterruptedException ignored) {}
        }

        // Stop callback thread
        if (callbackThread != null && callbackThread.isAlive()) {
            callbackThread.interrupt();
            try {
                callbackThread.join(2000);
            } catch (InterruptedException ignored) {}
        }

        try {
            DiscordRPC.discordShutdown();
        } catch (Exception e) {
            System.err.println("[Floyd RPC] Shutdown error: " + e.getMessage());
        }

        initialized = false;
    }

    /**
     * Updates Discord presence based on current game state
     */
    public void update() {
        if (!running || !initialized) {
            System.out.println("[Floyd RPC] Update skipped - running: " + running + ", initialized: " + initialized);
            return;
        }

        try {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc == null) {
                updateWithDefaults();
                return;
            }

            String state = "";
            String details = "";

            // Check game state
            if (mc.theWorld != null && mc.thePlayer != null) {
                ServerData server = mc.getCurrentServerData();

                if (server != null) {
                    // Playing on multiplayer
                    if (showDetails) {
                        details = "Playing Multiplayer";
                    }

                    if (showServer && server.serverIP != null && !server.serverIP.isEmpty()) {
                        state = maskServerIP(server.serverIP); // Mask the IP
                    } else {
                        state = "On a server";
                    }

                } else {
                    // Playing singleplayer
                    if (showDetails) {
                        details = "Playing Singleplayer";
                    }

                    try {
                        String worldName = mc.theWorld.getWorldInfo().getWorldName();
                        state = (worldName != null && !worldName.isEmpty()) ? worldName : "In a world";
                    } catch (Exception e) {
                        state = "In a world";
                    }
                }
            } else {
                // In menu
                details = "In Menu";
                state = "Idle";
            }

            // Only update if something changed (reduces API calls)
            if (details.equals(lastDetails) && state.equals(lastState)) {
                // State hasn't changed, skip update
                return;
            }

            lastDetails = details;
            lastState = state;

            // Build presence - state cannot be null or empty
            if (state == null || state.trim().isEmpty()) {
                state = "Playing";
            }

            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(state);
            builder.setBigImage("icon", "Floyd Client");

            if (details != null && !details.isEmpty()) {
                builder.setDetails(details);
            }

            // Set timestamp if enabled
            if (showTimestamp && created > 0) {
                builder.setStartTimestamps(created);
            }

            DiscordRPC.discordUpdatePresence(builder.build());

        } catch (Exception e) {
            System.err.println("[Floyd RPC] Update failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates with default values when Minecraft isn't ready yet
     */
    private void updateWithDefaults() {
        try {
            String details = "Loading";
            String state = "Starting up...";

            // Only update if changed
            if (details.equals(lastDetails) && state.equals(lastState)) {
                return;
            }

            lastDetails = details;
            lastState = state;

            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(state);
            builder.setBigImage("icon", "Floyd Client");
            builder.setDetails(details);

            if (showTimestamp && created > 0) {
                builder.setStartTimestamps(created);
            }

            DiscordRPC.discordUpdatePresence(builder.build());
            System.out.println("[Floyd RPC] Updated with defaults (MC not ready)");
        } catch (Exception e) {
            System.err.println("[Floyd RPC] Default update failed: " + e.getMessage());
        }
    }

    /**
     * Custom update with manual details and state
     */
    public void update(String details, String state) {
        if (!running || !initialized) {
            return;
        }

        try {
            // Ensure state is not null or empty
            if (state == null || state.trim().isEmpty()) {
                state = "Playing";
            }

            // Update tracking
            lastDetails = details != null ? details : "";
            lastState = state;

            DiscordRichPresence.Builder builder = new DiscordRichPresence.Builder(state);
            builder.setBigImage("icon", "Floyd Client");

            if (showTimestamp && created > 0) {
                builder.setStartTimestamps(created);
            }

            if (details != null && !details.isEmpty()) {
                builder.setDetails(details);
            }

            DiscordRPC.discordUpdatePresence(builder.build());
        } catch (Exception e) {
            System.err.println("[Floyd RPC] Custom update failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Masks server IP for privacy (hides liquidproxy completely)
     */
    private String maskServerIP(String serverIP) {
        if (serverIP == null || serverIP.isEmpty()) {
            return "Private Server";
        }

        // Check if the IP contains "liquidproxy.net" - show generic message
        if (serverIP.toLowerCase().contains("liquidproxy.net")) {
            return "On a LiquidProxy";
        }

        // For all other IPs, show them fully
        return serverIP;
    }

    // Getters and setters for settings
    public void setShowServer(boolean showServer) {
        this.showServer = showServer;
        if (initialized) {
            // Force update by clearing last state
            lastDetails = "";
            lastState = "";
            update();
        }
    }

    public void setShowTimestamp(boolean showTimestamp) {
        this.showTimestamp = showTimestamp;
        if (initialized) {
            lastDetails = "";
            lastState = "";
            update();
        }
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
        if (initialized) {
            lastDetails = "";
            lastState = "";
            update();
        }
    }

    public boolean isRunning() {
        return running && initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }
}