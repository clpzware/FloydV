package fr.ambient.module.impl.player;


import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.player.antivoid.BlinkAntivoid;
import fr.ambient.module.impl.player.antivoid.PacketAntivoid;
import fr.ambient.module.impl.player.antivoid.PositionAntivoid;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;

public class AntiVoid extends Module {

    public static final int CHECK_DEPTH = 60;

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Blink",
            new BlinkAntivoid("Blink", this),
            new PacketAntivoid("Packet", this),
            new PositionAntivoid("Position", this)
    );

    public NumberProperty distance = NumberProperty.newInstance("Distance", 0f, 5f, 10f, 1f, () -> !mode.getModeProperty().is("Blink"));
    public ModeProperty blinkindicator = ModeProperty.newInstance("Blink Indicator", new String[]{"Legit", "Raven", "Number", "None"}, "Number", () -> mode.getModeProperty().is("Blink"));


    public AntiVoid() {
        super(21, "Prevents you from falling into the void by teleporting you back.", ModuleCategory.PLAYER);
        this.registerProperties(mode.getModeProperty(),blinkindicator);
        this.moduleModeProperties.add(mode);
    }


    public boolean isAboveVoid(double x, double y, double z) {
        BlockPos playerPos = new BlockPos(x, y - 1, z);
        for (int i = 0; i < CHECK_DEPTH; i++) {
            BlockPos posToCheck = playerPos.down(i);
            Block blockBelow = mc.theWorld.getBlockState(posToCheck).getBlock();
            if (!(blockBelow instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }
}
