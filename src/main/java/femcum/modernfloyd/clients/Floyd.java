package femcum.modernfloyd.clients;

import femcum.modernfloyd.clients.bindable.BindableManager;
import femcum.modernfloyd.clients.bots.BotManager;
import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.command.CommandManager;
import femcum.modernfloyd.clients.component.Component;
import femcum.modernfloyd.clients.component.ComponentManager;
import femcum.modernfloyd.clients.creative.RiseTab;
import femcum.modernfloyd.clients.event.Event;
import femcum.modernfloyd.clients.event.bus.impl.EventBus;
import femcum.modernfloyd.clients.layer.LayerManager;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.manager.ModuleManager;
import femcum.modernfloyd.clients.packetlog.Check;
import femcum.modernfloyd.clients.packetlog.api.manager.PacketLogManager;
import femcum.modernfloyd.clients.script.ScriptManager;
import femcum.modernfloyd.clients.security.SecurityFeatureManager;
import femcum.modernfloyd.clients.ui.click.standard.RiseClickGUI;
import femcum.modernfloyd.clients.ui.theme.ThemeManager;
import femcum.modernfloyd.clients.util.ReflectionUtil;
import femcum.modernfloyd.clients.util.constants.ConstantsManager;
import femcum.modernfloyd.clients.util.discordutil.DiscordRPCUtil;
import femcum.modernfloyd.clients.util.file.FileManager;
import femcum.modernfloyd.clients.util.file.alt.AltManager;
import femcum.modernfloyd.clients.util.file.config.ConfigManager;
import femcum.modernfloyd.clients.util.file.data.DataManager;
import femcum.modernfloyd.clients.util.file.insult.InsultFile;
import femcum.modernfloyd.clients.util.file.insult.InsultManager;
import femcum.modernfloyd.clients.util.localization.Locale;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.florianmichael.viamcp.ViaMCP;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The main class where the client is loaded up.
 * Anything related to the client will start from here, and managers and instances will be stored in this class.
 */
@Getter
public enum Floyd {
    /**
     * Singleton enum instance for the client, ensuring immutability and ease of use.
     */
    INSTANCE;

    public static String NAME = "Floyd";
    public static final String VERSION = "5";
    public static final String VERSION_FULL = "5.0.0";
    public static final String COPYRIGHT = """
            © Floyd 2025. All Rights Reserved
            """;
    public static final String CREDITS = """
            FloydCEO and Clpz
            """;
    public static final String THAGANG = "";
    public static boolean DEVELOPMENT_SWITCH = true;
    DiscordRPCUtil discordRPC = new DiscordRPCUtil();

    // Authentication-related fields for IntroSequence
    @Setter
    public static String DISCUSER = "Processing"; // Stores the authenticated Discord username
    @Setter
    public static String UID = "Processing"; // Stores the authenticated user ID

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Floyd-Main-Thread");
        t.setDaemon(true);
        return t;
    });

    @Setter
    private Locale locale = Locale.EN_US;

    private EventBus<Event> eventBus;
    @Setter
    private ModuleManager moduleManager;
    @Setter
    private ComponentManager componentManager;
    @Setter
    private CommandManager commandManager;
    @Setter
    private SecurityFeatureManager securityManager;
    private BotManager botManager;
    private ThemeManager themeManager;
    @Setter
    private ScriptManager scriptManager;
    private DataManager dataManager;
    private FileManager fileManager;
    private ConfigManager configManager;
    private AltManager altManager;
    private InsultManager insultManager;
    private PacketLogManager packetLogManager;
    private BindableManager bindableManager;
    private LayerManager layerManager;
    @Setter
    private RiseClickGUI clickGUI;
    private RiseTab creativeTab;

    private final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Initializes the client when Minecraft's startGame method is nearly complete.
     * Loads all managers, modules, and configurations.
     */
    public void init() {
        Minecraft mc = Minecraft.getMinecraft();

        // Compatibility settings
        mc.gameSettings.guiScale = 2;
        mc.gameSettings.ofFastRender = false;
        mc.gameSettings.ofShowGlErrors = DEVELOPMENT_SWITCH;

        // Performance settings
        mc.gameSettings.ofSmartAnimations = true;
        mc.gameSettings.ofSmoothFps = false;

        // Initialize managers
        this.moduleManager = new ModuleManager();
        this.componentManager = new ComponentManager();
        this.commandManager = new CommandManager();
        this.fileManager = new FileManager();
        this.configManager = new ConfigManager();
        this.altManager = new AltManager();
        this.insultManager = new InsultManager();
        this.dataManager = new DataManager();
        this.securityManager = new SecurityFeatureManager();
        this.botManager = new BotManager();
        this.themeManager = new ThemeManager();
        this.scriptManager = new ScriptManager();
        this.eventBus = new EventBus<>();
        this.packetLogManager = new PacketLogManager();
        this.bindableManager = new BindableManager();
        this.layerManager = new LayerManager();
        new ConstantsManager();

        this.fileManager.init();

        // Determine development mode
        DEVELOPMENT_SWITCH = !ReflectionUtil.dirExist("hackclient.") && ReflectionUtil.dirExist("femcum.modernfloyd.clients");

        // Initialize managers
        this.dataManager.init();
        this.moduleManager.init();
        this.securityManager.init();
        this.botManager.init();
        this.componentManager.init();
        this.commandManager.init();
        this.altManager.init();
        this.insultManager.init();
        this.scriptManager.init();
        this.packetLogManager.init();

        this.clickGUI = new RiseClickGUI();
        this.clickGUI.initGui();

        this.insultManager.update();
        this.insultManager.forEach(InsultFile::read);

        this.creativeTab = new RiseTab();

        // Initialize ViaMCP asynchronously
        executor.submit(() -> {
            try {
                ViaMCP.create();
                ViaMCP.INSTANCE.initAsyncSlider();
                ViaMCP.INSTANCE.getAsyncVersionSlider().setVersion(ViaMCP.NATIVE_VERSION);
            } catch (Exception e) {
                System.err.println("""
                        [Floyd] Failed to initialize ViaMCP: %s
                        """.formatted(e.getMessage()));
            }
        });

        this.configManager.init();
        this.bindableManager.init();

        Display.setTitle(NAME + " " + VERSION_FULL.replace(".0", ""));
        discordRPC.start();
        discordRPC.update();
    }

    /**
     * Registers components, modules, commands, and packet checks using reflection.
     */
    public void register() {
        String[] paths = {
                "hackclient.",
                "femcum.modernfloyd.clients."
        };

        for (String path : paths) {
            if (!ReflectionUtil.dirExist(path)) {
                continue;
            }

            if ("hackclient.".equals(path)) {
                DEVELOPMENT_SWITCH = false;
            }

            Class<?>[] classes = ReflectionUtil.getClassesInPackage(path);

            for (Class<?> clazz : classes) {
                if (Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                try {
                    if (Component.class.isAssignableFrom(clazz)) {
                        this.componentManager.add((Component) clazz.getConstructor().newInstance());
                    } else if (Module.class.isAssignableFrom(clazz)) {
                        this.moduleManager.put(clazz, (Module) clazz.getConstructor().newInstance());
                    } else if (Command.class.isAssignableFrom(clazz)) {
                        this.commandManager.getCommandList().add((Command) clazz.getConstructor().newInstance());
                    } else if (Check.class.isAssignableFrom(clazz)) {
                        this.packetLogManager.add((Check) clazz.getConstructor().newInstance());
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    System.err.println("""
                            [Floyd] Failed to register class %s: %s
                            """.formatted(clazz.getName(), e.getMessage()));
                }
            }

            break;
        }
    }

    /**
     * Cleans up resources when the Minecraft client shuts down.
     */
    public void terminate() {
        if (this.configManager != null && this.configManager.getLatestConfig() != null) {
            this.configManager.getLatestConfig().write();
        }
        if (!executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Reloads the client by terminating and reinitializing.
     */
    public void reload() {
        terminate();
        init();
        this.configManager.setupLatestConfig();
    }
}