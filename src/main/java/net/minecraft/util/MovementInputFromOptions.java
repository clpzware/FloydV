package net.minecraft.util;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import fr.ambient.Ambient;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.module.impl.movement.NoSlowdown;
import net.minecraft.client.settings.GameSettings;

import static fr.ambient.util.InstanceAccess.mc;

public class MovementInputFromOptions extends MovementInput {
    private final GameSettings gameSettings;

    public MovementInputFromOptions(GameSettings gameSettingsIn) {
        this.gameSettings = gameSettingsIn;
    }

    public void updatePlayerMoveState() {
        this.moveStrafe = 0.0F;
        this.moveForward = 0.0F;

        if (this.gameSettings.keyBindForward.isKeyDown()) {
            ++this.moveForward;
        }

        if (this.gameSettings.keyBindBack.isKeyDown()) {
            --this.moveForward;
        }

        if (this.gameSettings.keyBindLeft.isKeyDown()) {
            ++this.moveStrafe;
        }

        if (this.gameSettings.keyBindRight.isKeyDown()) {
            --this.moveStrafe;
        }

        this.jump = this.gameSettings.keyBindJump.isKeyDown();
        this.sneak = this.gameSettings.keyBindSneak.isKeyDown();

        final MoveInputEvent moveInputEvent = new MoveInputEvent(moveForward, moveStrafe, jump, sneak, 0.3D);
        Ambient.getInstance().getEventBus().post(moveInputEvent);

        final double sneakMultiplier = Ambient.getInstance().getModuleManager().getModule(NoSlowdown.class).sneak.getValue() ? 1.0f : moveInputEvent.getSneakMultiplier();
        this.moveForward = moveInputEvent.getForward();
        this.moveStrafe = moveInputEvent.getStrafe();
        this.jump = moveInputEvent.isJumping();
        this.sneak = moveInputEvent.isSneaking();


        if (this.sneak)
        {
            mc.thePlayer.ticksSinceSneakingReal++;
            mc.thePlayer.ticksSinceUnSneakingReal = 0;
            if(ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_14)){
                if(mc.thePlayer.ticksSinceSneakingReal >= 2){
                    this.moveStrafe = (float)((double)this.moveStrafe * sneakMultiplier);
                    this.moveForward = (float)((double)this.moveForward * sneakMultiplier);
                }
            }else{
                this.moveStrafe = (float)((double)this.moveStrafe * sneakMultiplier);
                this.moveForward = (float)((double)this.moveForward * sneakMultiplier);
            }
        }else{
            mc.thePlayer.ticksSinceUnSneakingReal++;
            if(ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_14)){
                if(mc.thePlayer.ticksSinceUnSneakingReal <= 1){
                    this.moveStrafe = (float)((double)this.moveStrafe * sneakMultiplier);
                    this.moveForward = (float)((double)this.moveForward * sneakMultiplier);
                }
            }



            mc.thePlayer.ticksSinceSneakingReal = 0;
        }
    }
}
