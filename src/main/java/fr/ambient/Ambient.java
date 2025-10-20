package fr.ambient;

import com.viaversion.viaversion.api.connection.UserConnection;
import fr.ambient.anticheat.AnticheatManager;
import fr.ambient.command.CommandManager;
import fr.ambient.command.irc.IRC;
import fr.ambient.component.ComponentManager;
import fr.ambient.component.impl.misc.AltsComponent;
import fr.ambient.component.impl.packet.OutgoingPacketComponent;
import fr.ambient.component.impl.player.ClickPatternComponent;
import fr.ambient.component.impl.player.RotationComponent;
import fr.ambient.component.impl.player.RotationPatternComponent;
import fr.ambient.config.ConfigManager;
import fr.ambient.config.client.ClientConfig;
import fr.ambient.event.EventBus;
import fr.ambient.external.value.ExternalValueManager;
import fr.ambient.module.ModuleManager;

import fr.ambient.module.impl.render.hud.HUD;
import fr.ambient.theme.CustomThemeManager;
import fr.ambient.theme.ThemeManager;
import fr.ambient.util.DiscordRP;
import fr.ambient.util.math.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.File;

@Getter
@Setter
public final class Ambient {
    private static final Ambient INSTANCE = new Ambient();
    private String uid = "67",
            username = "FloydCEO",
            token,
            rank,
            discord;

    private String version = "3.0";

    private final EventBus eventBus = new EventBus();
    private ModuleManager moduleManager;
    private AnticheatManager checkManager;
    private CommandManager commandManager;
    private ComponentManager componentManager;
    private ConfigManager configManager;
    private ThemeManager themeManager;
    private HUD hud;
    private ExternalValueManager externalValueManager = new ExternalValueManager();
    private IRC irc;
    private CustomThemeManager customThemeManager = new CustomThemeManager();
    private TimeUtil msSinceLast = new TimeUtil();
    private TimeUtil flashbang = new TimeUtil();
    private String serverIp = "";
    private boolean isTryingToReconnect = false;
    private RotationComponent rotationComponent;
    private AltsComponent altsComponent;
    private ClickPatternComponent clickPatternComponent;
    private RotationPatternComponent rotationPatternComponent;
    private GuiScreen mainMenuScreen;
    private UserConnection userConnection;
    private OutgoingPacketComponent outgoingPacketComponent;
    private DiscordRP discordRP = new DiscordRP();
    private File chattemp = new File(Minecraft.getMinecraft().mcDataDir, "/ambient/chattemp/");

//    private WSBackend wsBackend;
    private ClientConfig config = new ClientConfig();

    public static synchronized Ambient getInstance() {
        return INSTANCE;
    }
}
