package fr.ambient.module.impl.player;

import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.LongJump;
import fr.ambient.module.impl.player.nofall.*;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import fr.ambient.util.player.ChatUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;


public class NoFall extends Module {



    public boolean blinking = false;

    public NoFall() {
        super(35, "Prevents you from taking fall damage.", ModuleCategory.PLAYER);
        this.registerProperties(mode.getModeProperty(),wdmode,blinkindicator);
        this.setSuffix(mode.getModeProperty()::getValue);
        this.moduleModeProperties.add(mode);
    }



    public void onEnable() {
        if (mode.getModeProperty().is("Old Grim")) {
            ChatUtil.display("Will only work if the server is natively 1.20");
        }
    }


    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog",
            new WatchdogNofall("Watchdog", this),
            new FastFallNofall("FastFall", this),
            new MotionSetNofall("Collide", this),
            new Grim117Nofall("Old Grim", this),
            new MinibloxNofall("Miniblox", this),
            new MosPixelNofall("MosPixel", this),
            new NoGroundNofall("Noground", this),
            new DamageNofall("Damage", this),
            new VerusNofall("Verus", this),
            new BlinkNofall("Blink", this)
    );

    public ModeProperty wdmode = ModeProperty.newInstance("WD Mode", new String[]{"C08 Water", "Watchdog Packet", "Dynamic"}, "Watchdog Packet", () -> mode.getModeProperty().is("Watchdog"));
    public ModeProperty blinkindicator = ModeProperty.newInstance("Blink Indicator", new String[]{"Legit", "Raven", "Number", "None"}, "Number", () -> mode.getModeProperty().is("Blink") ||
            mode.getModeProperty().is("Watchdog") && wdmode.is("Dynamic"));




    public boolean CanNofall() {
        return mc.thePlayer.fallDistance > 3 && (mc.thePlayer.hurtTime == 0 || Ambient.getInstance().getModuleManager().getModule(LongJump.class).isEnabled())
                && !mc.thePlayer.isSpectator() && getDistanceToGround() != 0;
    }

    public float getDistanceToGround() {
        for (float y = (float) mc.thePlayer.posY; y > 0; y--) {
            BlockPos bp = new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ);
            if (!(mc.theWorld.getBlockState(bp).getBlock() instanceof BlockAir)) {
                return (float) (mc.thePlayer.posY - y);
            }
        }
        return 0;
    }
}
