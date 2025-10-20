package fr.ambient.module.impl.combat.velocity;


import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.network.PacketReceiveEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleMode;
import fr.ambient.module.impl.combat.Velocity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.server.*;


public class JumpVelocity extends ModuleMode {


    private final Velocity velocity = (Velocity) this.getSuperModule();

    public JumpVelocity(String modeName, Module module) {
        super(modeName, module);
    }

    @SubscribeEvent
    private void onMove(MoveInputEvent event) {
        if (mc.thePlayer.hurtTime == 9 && (Math.random() * 100 < velocity.jumpVeloWhenHappenOng.getValue() || (velocity.forceJumpVeloOnTarget.getValue() && mc.pointedEntity instanceof EntityLivingBase))) {
            event.setJumping(true);
        }
    }
}





