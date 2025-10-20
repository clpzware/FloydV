package fr.ambient.module.impl.combat;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.velocity.*;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.property.impl.wrappers.ModuleModeProperty;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;


public class Velocity extends Module {

    private final ModuleModeProperty mode = new ModuleModeProperty(this, "Mode", "Watchdog",
            new OldGrim18Velocity("Grim TPCancel", this),
            new MushmcReverseVelocity("Mushmc Reverse", this),
            new MushMcCancelVelocity("Mushmc Cancel", this),
            new OldGrimVelocity("Old Grim 1.17", this),
            new S32CancelVelocity("S32Cancel", this),
            new WatchdogVelocity("Watchdog", this),
            new WatchdogGroundBoostVelocity("Watchdog 2", this),
            new BlockmcVelocity("BlocksMC", this),
            new CancelVelocity("Cancel", this),
            new ReduceVelocity("Reduce", this),
            new NormalVelocity("Normal", this),
            new JumpVelocity("Jump", this),
            new GhostBlockVelocity("GhostBlock", this),
            new DelayVelocity("DelayPred", this),
            new KoderaVelocity("Kodera", this)
    );


    public final BooleanProperty noExplosion = BooleanProperty.newInstance("No Explosion", true);
    public final BooleanProperty boosts = BooleanProperty.newInstance("Boost", false, ()-> mode.getModeProperty().is("Watchdog 2"));
    public final BooleanProperty breaker = BooleanProperty.newInstance("0/0 While Breaking", false, ()-> mode.getModeProperty().is("Watchdog") || mode.getModeProperty().is("Normal") || mode.getModeProperty().is("Watchdog 2"));

    public final NumberProperty h = NumberProperty.newInstance("Horizontal", -100F, 0F, 100F, 1f, ()-> mode.getModeProperty().is("Normal") || mode.getModeProperty().is("Cancel"));
    public final NumberProperty v = NumberProperty.newInstance("Vertical", 0F, 100F, 100F, 1f, ()-> mode.getModeProperty().is("Normal") || mode.getModeProperty().is("Cancel"));

    public final NumberProperty jumpVeloWhenHappenOng = NumberProperty.newInstance("Jump Velocity Chance", 0f, 100f, 100f, 1f, ()-> mode.getModeProperty().is("Jump"));
    public final BooleanProperty forceJumpVeloOnTarget = BooleanProperty.newInstance("Force on Target", false, ()-> mode.getModeProperty().is("Jump"));


    public Velocity() {
        super(6,"Reduces the amount of knock back you take when hit", ModuleCategory.COMBAT);
        this.registerProperties(mode.getModeProperty(),h,v,noExplosion,breaker,jumpVeloWhenHappenOng,forceJumpVeloOnTarget);
        this.moduleModeProperties.add(mode);
        this.setSuffix(() -> mode.getModeProperty().is("Cancel") ||  mode.getModeProperty().is("Normal")
            ? String.format("%d%% %d%%", h.getValue().intValue(), v.getValue().intValue()) :  mode.getModeProperty().getValue()
        );
    }



    public float getDistanceToGround() {
        for (float y = (float) mc.thePlayer.posY; y > 0; y -= 1) {
            BlockPos bp = new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ);
            if (!(mc.theWorld.getBlockState(bp).getBlock() instanceof BlockAir)) {
                return (float) (mc.thePlayer.posY - y);
            }
        }
        return 0;
    }
}