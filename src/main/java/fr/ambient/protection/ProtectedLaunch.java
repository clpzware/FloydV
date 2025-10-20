package fr.ambient.protection;

import cc.polymorphism.annot.IncludeReference;
import cc.polymorphismj2c.annot.Native;
import com.google.gson.JsonObject;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.viamcp.ViaMCP;
import fr.ambient.Ambient;
import fr.ambient.anticheat.AnticheatManager;
import fr.ambient.command.CommandManager;
import fr.ambient.command.impl.*;
import fr.ambient.command.irc.IRC;
import fr.ambient.component.Component;
import fr.ambient.component.ComponentManager;
import fr.ambient.component.impl.misc.AltsComponent;
import fr.ambient.component.impl.misc.BreakerWhitelistComponent;
import fr.ambient.component.impl.misc.CosmeticComponent;
import fr.ambient.component.impl.packet.AntiBadPacketComponent;
import fr.ambient.component.impl.packet.BlinkComponent;
import fr.ambient.component.impl.packet.OutgoingPacketComponent;
import fr.ambient.component.impl.packet.PacketOrderComponent;
import fr.ambient.component.impl.player.ClickPatternComponent;
import fr.ambient.component.impl.player.HypixelComponent;
import fr.ambient.component.impl.player.RotationComponent;
import fr.ambient.component.impl.player.RotationPatternComponent;
import fr.ambient.component.impl.ui.ItemRenderComponent;
import fr.ambient.config.ConfigManager;
import fr.ambient.module.ModuleManager;
import fr.ambient.module.impl.combat.*;
import fr.ambient.module.impl.misc.*;
import fr.ambient.module.impl.skyblock.AutoGift;
import fr.ambient.module.impl.movement.*;
import fr.ambient.module.impl.player.*;
import fr.ambient.module.impl.render.*;
import fr.ambient.module.impl.render.hud.*;
import fr.ambient.module.impl.render.misc.Animations;
import fr.ambient.module.impl.render.misc.Camera;
import fr.ambient.module.impl.render.misc.Indicators;
import fr.ambient.module.impl.render.player.*;
import fr.ambient.module.impl.render.widgets.ArmorDisplay;
import fr.ambient.module.impl.render.widgets.Effects;
import fr.ambient.module.impl.render.widgets.ImageRender;
import fr.ambient.module.impl.render.widgets.InventoryDisplay;
import fr.ambient.module.impl.render.world.Ambience;
import fr.ambient.module.impl.render.world.Breadcrumbs;
import fr.ambient.module.impl.render.world.BreakProgress;
import fr.ambient.module.impl.render.world.KillEffects;
import fr.ambient.module.impl.skyblock.Test;
import fr.ambient.protection.backend.api.HWID;
import fr.ambient.theme.ThemeManager;
import fr.ambient.ui.mainmenu.wouf.WoufMainMenuScreen;
import fr.ambient.util.Reconnect;
import fr.ambient.util.player.ChatUtil;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import org.lwjglx.opengl.Display;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URISyntaxException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Native
@IncludeReference
public class ProtectedLaunch {
    public static Cosmetics CUSTOM_CAPE;

    @SneakyThrows
    public static void init(String uid) throws Throwable {

        Ambient.getInstance().getConfig().load();

        ViaMCP.create();
        ViaMCP.INSTANCE.initAsyncSlider();
        ViaMCP.INSTANCE.getAsyncVersionSlider().setVersion(ProtocolVersion.v1_8.getVersion());

        if (Ambient.getInstance().getConfig().getValue("discord-rp").equals("true")) {
            Ambient.getInstance().getDiscordRP().start();
        }

        final boolean developmentSwitch = true;

        { // Blacklisted argument check
            if (!developmentSwitch) {
                final String[] naughtyFlags = {
                        "-agentlib:jdwp",
                        "-XBootclasspath",
                        "-javaagent",
                        "-Xdebug",
                        "-agentlib",
                        "-Xrunjdwp",
                        "-Xnoagent",
                        "-verbose",
                        "-DproxySet",
                        "-DproxyHost",
                        "-DproxyPort",
                        "-Djavax.net.ssl.trustStore",
                        "-Djavax.net.ssl.trustStorePassword"
                };

                RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
                String jvmArgs = runtimeBean.getInputArguments().toString();

                boolean dashes = true, counter = true, detected = false;

                int i = 0;
                for (String str : naughtyFlags) {
                    if (!str.contains("-")) {
                        dashes = false;
                        break;
                    }
                    ++i;
                }

                if (i != 13) {
                    counter = false;
                }

                for (String arg : naughtyFlags) {
                    if (jvmArgs.contains(arg)) {
                        detected = true;
                        break;
                    }
                }

                if (!dashes || !counter || detected) {
                    System.out.println("Environment Integrity Issue 0x01");

                    for (long l = Long.MIN_VALUE; l < Long.MAX_VALUE; ++l) {
                        --l;
                    }

                    return;
                }
            }
        }

        System.err.println("DONE ????");

        /*
        JsonObject sent = new JsonObject();
        sent.addProperty("id", "login");
        sent.addProperty("uid", uid);
        sent.addProperty("hwid", HWID.getHWID());
        sent.addProperty("clientid", "ambient");
        sent.addProperty("clientVersion", "Release");

        while (Ambient.getInstance().getToken() == null) {
            Thread.sleep(50);
        }

        JsonObject object1 = new JsonObject();
        object1.addProperty("id", "userinfo");
        object1.addProperty("token", Ambient.getInstance().getToken());

        JsonObject object2 = new JsonObject();
        object2.addProperty("id", "clientdata");
        object2.addProperty("token", Ambient.getInstance().getToken());

        while (Ambient.getInstance().getExternalValueManager().get("names") == null ||
               Ambient.getInstance().getExternalValueManager().get("names").values == null ||
               Ambient.getInstance().getExternalValueManager().get("names").values.isEmpty()) {
            Thread.sleep(500);
        }
        */

        CUSTOM_CAPE = new Cosmetics();

        { // Modules
            ModuleManager moduleManager = new ModuleManager();

            moduleManager.register(
                    // Combat
                    new AimAssist(),
                    new AntiBot(),
                    new AutoClicker(),
                    new AutoGoldenHead(),
                    new BackTrack(),
                    new Criticals(),
                    new FakeLag(),
                    new KeepSprint(),
                    new KillAura(),
                    new Reach(),
                    new SumoBot(),
                    new TargetStrafe(),
                    new TickBase(),
                    new Velocity(),
                    new WTap(),

                    // Movement
                    new Flight(),
                    new InvMove(),
                    new Jesus(),
                    new LongJump(),
                    new NoSlowdown(),
                    new QuickStop(),
                    new SafeWalk(),
                    new SaveMoveKey(),
                    new Speed(),
                    new Spider(),
                    new Sprint(),
                    new Step(),

                    // Player
                    new AntiFireBall(),
                    new AntiVoid(),
                    new AutoBed(),
                    new AutoPot(),
                    new AutoSoup(),
                    new AutoTool(),
                    new Blink(),
                    new Breaker(),
                    new ChestStealer(),
                    new DelayRemover(),
                    new FastBreak(),
                    new FastPlace(),
                    new InvManager(),
                    new LegitScaffold(),
                    new NoFall(),
                    new NoRotate(),
                    new Phase(),
                    new Refill(),
                    new Scaffold(),
                    new Timer(),

                    // Render
                    new Ambience(),
                    new Animations(),
                    new ArmorDisplay(),
                    new Breadcrumbs(),
                    new BreakProgress(),
                    new Camera(),
                    new Chams(),
                    new Chat(),
                    CUSTOM_CAPE,
                    new ClickGui(),
                    new Effects(),
                    new ESP(),
                    new HUD(),
                    new HurtColor(),
                    new ImageRender(),
                    new Indicators(),
                    new InventoryDisplay(),
                    new KillEffects(),
                    new NameTags(),
                    new Notification(),
                    new Overlay(),
                    new PlayerRender(),
                    new PostProcessing(),
                    new Scoreboard(),
                    new SessionStats(),
                    new TargetHUD(),

                    // Exploits
                    new AutoDisable(),
                    new Anticheat(),
                    new AutoPlay(),
                    new BedwarsUtils(),
                    new ClientSpoofer(),
                    new ChatBypass(),
                    new Crasher(),
                    new Disabler(),
                    new FastUse(),
                    new Freelook(),
                    new MCF(),
                    new NickHider(),
                    new NoDisconnect(),
                    new Regen(),
                    new Respawn(),
                    new SmoothReset(),
                    new TransactionLogger()
            );

            if (Ambient.getInstance().getConfig().getValue("skyblock").equals("true")) {
                System.out.println("Enabled SkyBlock Addons !");
                moduleManager.register(new AutoGift());
                moduleManager.register(new Test());
            }

            Ambient.getInstance().getEventBus().register(moduleManager);
            Ambient.getInstance().setModuleManager(moduleManager);
            Ambient.getInstance().setHud(moduleManager.getModule(HUD.class));
        }

        { // Commands
            CommandManager commandManager = new CommandManager();

            commandManager.register(new BindCommand(), new ConfigCommand(), new ToggleCommand(), new ThemeCommand(),
                    new QueueCommand(), new HypixelAPICommand(), new HelpCommand(), new KeyConfigCommand(),
                    new CustomNameCommand(), new IgnCommand(), new HideCommand(), new ClipboardCommand(),
                    new ClickGuiStuck(), new ReloadCommand(), new ScriptCommand(),
                    new ClickPatternCommand(), new PartyCommand(), new RotationPatternCommand(), new HClipCommand(), new ESPCommand(),
                    new CosmeticCommand());

            Ambient.getInstance().getEventBus().register(commandManager);
            Ambient.getInstance().setCommandManager(commandManager);
        }

        { // Components
            ComponentManager componentManager = new ComponentManager();
            Ambient.getInstance().setRotationComponent(new RotationComponent());
            Ambient.getInstance().setRotationPatternComponent(new RotationPatternComponent());
            Ambient.getInstance().setOutgoingPacketComponent(new OutgoingPacketComponent());
            Ambient.getInstance().setAltsComponent(new AltsComponent());
            componentManager.register(Ambient.getInstance().getRotationComponent(), Ambient.getInstance().getOutgoingPacketComponent(), Ambient.getInstance().getRotationPatternComponent(), Ambient.getInstance().getAltsComponent(), new BlinkComponent(), new HypixelComponent(),
                    new PacketOrderComponent(), new CosmeticComponent(), new BreakerWhitelistComponent(), new ItemRenderComponent(), new AntiBadPacketComponent());

            for (Component component : componentManager.getObjects()) {
                Ambient.getInstance().getEventBus().register(component);
            }

            Ambient.getInstance().setComponentManager(componentManager);
        }

        Ambient.getInstance().setConfigManager(new ConfigManager());

        Ambient.getInstance().getConfigManager().init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Ambient.getInstance().getConfigManager().stop();
        }));

        Ambient.getInstance().setCheckManager(new AnticheatManager());
        Ambient.getInstance().setThemeManager(new ThemeManager());
        Ambient.getInstance().setIrc(new IRC());
        Display.setTitle("Ambient " + Ambient.getInstance().getVersion() + " | " + Ambient.getInstance().getUsername() + " : " + Ambient.getInstance().getUid());
        HypixelComponent.loadCheaters();
        HUD hud = Ambient.getInstance().getModuleManager().getModule(HUD.class);
        hud.setEnabled(true);

        Ambient.getInstance().getModuleManager().getModule(Scoreboard.class).setEnabled(true);
        Ambient.getInstance().getModuleManager().getModule(Chat.class).setEnabled(true);

        Ambient.getInstance().getCustomThemeManager().load();
        Ambient.getInstance().getCustomThemeManager().save();

        Ambient.getInstance().setClickPatternComponent(new ClickPatternComponent());

        /*
        ScheduledExecutorService scheduler0 = Executors.newScheduledThreadPool(1);
        Runnable task2 = () -> {
            if (Ambient.getInstance().getMsSinceLast().finished(7500)) {
                if (!Ambient.getInstance().isTryingToReconnect()) {
                    Reconnect.reco();
                }else{
                    ChatUtil.display("Failed to reconnect : Already Trying. Why is this taking so long ?");
                }
            }
        };
        Ambient.getInstance().getMsSinceLast().reset();
        scheduler0.scheduleAtFixedRate(task2, 5, 1, TimeUnit.SECONDS);
        */

        if (Ambient.getInstance().getConfig().getValue("auto-default-config").equals("true")) {
            /*
            JsonObject object5 = new JsonObject();
            object5.addProperty("id", "config");
            object5.addProperty("action", "load");
            object5.addProperty("name", "default");
            */
        }

        Ambient.getInstance().setMainMenuScreen(new WoufMainMenuScreen());
    }
}