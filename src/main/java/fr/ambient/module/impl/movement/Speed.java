package fr.ambient.module.impl.movement;

import fr.ambient.Ambient;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.speed.*;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.MultiProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.block.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.util.BlockPos;

public class Speed extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog",
            new WatchdogSbSpeed("Watchdog SkyBlock", this),
            new WatchdogGroundSpeed("Watchdog Ground", this),
            new CustomSpeed("CustomSpeed", this),
            new HypixelSpeed("Hypixel Dynamic", this),
            new MinibloxSpeed("Miniblox", this),
            new VulcanSpeed("Vulcan", this),
            new VerusSpeed("Verus", this),
            new BlockMcSpeed("BlocksMC", this),
            new MushmcSpeed("Mushmc", this),
            new WatchdogSpeed("Watchdog", this),
            new KarhuSpeed("Karhu", this),
            new PacketSpeed("Packet", this),
            new VanillaSpeed("Vanilla", this),
            new StrafeSpeed("Strafe", this),
            new heephSpeed("Heeph", this),
            new NcpSpeed("NCP", this),
            new CacSpeed("CAC", this),
            new MinemenSpeed("MMC", this),
            new AntiCheatPlusSpeed("ACP", this),
            new KoderaSpeed("Kodera", this)
    );

    //Custom Speed bs
    public BooleanProperty jump = BooleanProperty.newInstance("Legit Jump", true, () -> mode.getModeProperty().is("CustomSpeed"));
    public BooleanProperty offset = BooleanProperty.newInstance("Offset", false, () -> mode.getModeProperty().is("CustomSpeed"));
    public BooleanProperty cfallmotion = BooleanProperty.newInstance("Custom Fall Motion", false, () -> mode.getModeProperty().is("CustomSpeed"));
    public BooleanProperty applytimer = BooleanProperty.newInstance("Timer", false, () -> mode.getModeProperty().is("CustomSpeed"));
    public BooleanProperty teleport = BooleanProperty.newInstance("Teleport", false, () -> mode.getModeProperty().is("CustomSpeed"));
    public ModeProperty spoof = ModeProperty.newInstance("Spoof Ground State", new String[]{"Off Ground", "On Ground", "None"}, "None", () -> mode.getModeProperty().is("CustomSpeed"));
    public ModeProperty whentoapply = ModeProperty.newInstance("When Speed", new String[]{"On Ground", "On Fall", "On Jump", "Always"}, "Always", () -> mode.getModeProperty().is("CustomSpeed"));
    public NumberProperty timerspeed = NumberProperty.newInstance("Timer Amount", 0.1f, 1f, 9.5f, 0.1f, () -> mode.getModeProperty().is("CustomSpeed") && applytimer.getValue());
    public NumberProperty fallmotion = NumberProperty.newInstance("Fall Motion", 0.1f, 1f, 9.5f, 0.1f, () -> mode.getModeProperty().is("CustomSpeed") && cfallmotion.getValue());
    public NumberProperty airtick = NumberProperty.newInstance("Fall Trigger AirTick", 1f, 1f, 20f, 1f, () -> mode.getModeProperty().is("CustomSpeed") && cfallmotion.getValue());
    public NumberProperty cmotiony = NumberProperty.newInstance("Custom Motion Y", 0f, 1f, 3f, 0.1f, () -> mode.getModeProperty().is("CustomSpeed") && !jump.getValue());
    public NumberProperty cspeed = NumberProperty.newInstance("Custom Speed", 0.1f, 1f, 9.5f, 0.1f, () -> mode.getModeProperty().is("CustomSpeed"));
    public NumberProperty teleporttick = NumberProperty.newInstance("Teleport Ticks", 1f, 5f, 50f, 1f, () -> mode.getModeProperty().is("CustomSpeed") && teleport.getValue());
    public NumberProperty tpdistance = NumberProperty.newInstance("Distance Per Teleport", 0.05f, 0.3f, 8f, 0.05f, () -> mode.getModeProperty().is("CustomSpeed") && teleport.getValue());

    public ModeProperty verusmode = ModeProperty.newInstance("Verus Mode", new String[]{"Ground", "Ground 2","Low"}, "Ground", () -> mode.getModeProperty().is("Verus"));
    public ModeProperty vulcanmode = ModeProperty.newInstance("Vulcan Mode", new String[]{"2.9.2.4+", "Old Ground", "Old Strafe", "Old Glide"}, "Old Ground", () -> mode.getModeProperty().is("Vulcan"));
    public MultiProperty watchdogmulti = MultiProperty.newInstance("Options", new String[]{"LowHop On Scaffold", "Speed On Hurtime", "Disable On Lagback", "FastFall", "Strafe","AirStrafe"}, () -> mode.getModeProperty().is("Watchdog") || mode.getModeProperty().is("Hypixel Dynamic"));

    public BooleanProperty autof5 = BooleanProperty.newInstance("Auto Third Person", false);
    public BooleanProperty fastfall = BooleanProperty.newInstance("Fastfall", true, () -> mode.getModeProperty().is("Miniblox"));
    public BooleanProperty minibloxglide = BooleanProperty.newInstance("Glide", true, () -> mode.getModeProperty().is("Miniblox"));
    public BooleanProperty timerboost = BooleanProperty.newInstance("NCP Timer", false, () -> mode.getModeProperty().is("NCP"));

    public NumberProperty speed = NumberProperty.newInstance("Speed", 0.35f, 0.35f, 1F, 0.01F, () -> mode.getModeProperty().is("Vanilla"));
    public NumberProperty damageboost = NumberProperty.newInstance("Damage Boost", 1f, 1.1f, 3f, 0.1F, () -> mode.getModeProperty().is("Vulcan"));
    public NumberProperty packetAmount = NumberProperty.newInstance("Ticks", 1f, 5f, 50f, 1f, () -> mode.getModeProperty().is("Packet"));
    public NumberProperty distancePerPacket = NumberProperty.newInstance("Distance Per Packet", 0.05f, 0.3f, 8f, 0.05f, () -> mode.getModeProperty().is("Packet"));

    public boolean slab = false;

    public Speed() {
        super(17, "Increases your overall movement speed.", ModuleCategory.MOVEMENT);
        this.registerProperties(mode.getModeProperty(), vulcanmode, watchdogmulti, verusmode, whentoapply, spoof, applytimer, teleport, jump, offset, cfallmotion, fastfall, minibloxglide,
                timerboost, speed, damageboost, packetAmount, distancePerPacket, teleporttick, tpdistance, airtick, timerspeed, fallmotion, cmotiony, cspeed, autof5);
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }


    public void onEnable() {
        if (mode.getModeProperty().is("Hypixel")) {
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = .42;
            }
        }
        if (autof5.getValue()) {
            mc.gameSettings.thirdPersonView = 1;
        }
        if (mode.getModeProperty().is("Vulcan") && vulcanmode.is("Old Ground")) {
            ChatUtil.display("Take Damage From anything but fall and it will work on Latest");
        }
    }


    public void onDisable() {
        if (mode.getModeProperty().is("Hypixel")) {
            if (mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = .42;
            }
        }
        if (autof5.getValue()) {
            mc.gameSettings.thirdPersonView = 0;
        }
    }


    @SubscribeEvent
    private void onUpdate(PreMotionEvent event) {
        if (mc.thePlayer.onGround && Math.abs(event.getPosY() % 1) > 0.03) {
            slab = true;
        } else if (mc.thePlayer.onGround) {
            slab = false;
        }
    }


    public boolean isNearnonfullblock2() {
        for (float y = -1; y < 1; y++) {
            if (isNonFullBlock2(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + y, mc.thePlayer.posZ))) {
                return true;
            }
        }
        return false;
    }

    public boolean isNonFullBlock2(BlockPos blockPos) {
        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
        return (block instanceof BlockFence || block instanceof BlockFenceGate);
    }




    public boolean invmovefix() {
        return mc.currentScreen instanceof GuiChest || (mc.currentScreen instanceof GuiInventory);
    }

    public boolean towerfix() {
        return Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled() && mc.gameSettings.keyBindJump.pressed;
    }

    public boolean fastFallConditions() {
        return !mc.thePlayer.isInWater() && !mc.thePlayer.isInLava() && !isNearnonfullblock2() && (!mc.thePlayer.isCollidedVertically || mc.thePlayer.onGround);
    }
}