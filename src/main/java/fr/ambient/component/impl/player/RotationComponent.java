package fr.ambient.component.impl.player;

import fr.ambient.Ambient;
import fr.ambient.component.Component;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.PlayerUpdateActionStateEvent;
import fr.ambient.event.impl.player.move.MoveInputEvent;
import fr.ambient.event.impl.world.TickEvent;
import fr.ambient.module.impl.misc.Freelook;
import fr.ambient.util.player.MoveUtil;
import fr.ambient.util.player.movecorrect.MoveCorrect;
import lombok.Setter;
import net.minecraft.util.MathHelper;
import org.lwjglx.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;

public class RotationComponent extends Component {
    private MoveCorrect moveCorrect;
    public boolean active, smoothReset;

    public int smoothResetTicks = 5;
    public int ticksPassed = 0;

    public float rotationYaw;
    public float rotationPitch;

    public float renderedYaw;
    public float prevRenderedYaw;
    public float prevRotationYaw;
    public float prevRotationPitch;

    public float activeYaw;
    public float prevActiveYaw = 0;
    public float activePitch;

    public ArrayList<Class<?>> classes = new ArrayList<>();

    public void setActive(boolean enabled, Class<?> classf){
        if(classes.contains(classf) && !enabled){
            classes.remove(classf);
        }
        if(!classes.contains(classf) && enabled){
            classes.add(classf);
        }
        this.active = !classes.isEmpty();

        if(!this.active){
            if (!smoothReset) {
                Ambient.getInstance().getRotationComponent().fullyDisable();

            }else{
                ticksPassed--;
            }
        }else{
            ticksPassed = smoothResetTicks;
        }
    }


    @Setter
    public Float[] toFix = new Float[]{0f,0f};

    public boolean isSmoothResetting(){
        return smoothReset && ticksPassed != smoothResetTicks && ticksPassed > 0;
    }

    public RotationComponent(){
        smoothReset = false;
    }
    public void setRotations(float yaw, float pitch, MoveCorrect move) {
        moveCorrect = move;

        activeYaw = yaw;
        activePitch = pitch;


        if(!canUpdateRotations()){
            mc.thePlayer.rotationYaw = yaw;
            mc.thePlayer.rotationPitch = pitch;
        }
    }

    public boolean canUpdateRotations() {
        return (!active && !isSmoothResetting())  || (moveCorrect == MoveCorrect.OFF || moveCorrect == MoveCorrect.SPRINT);
    }


    public float[] getRotation() {
        if ((active || isSmoothResetting()) && (moveCorrect == MoveCorrect.SILENT || moveCorrect == MoveCorrect.STRICT)){
            return new float[]{rotationYaw, rotationPitch, prevRotationYaw, prevRotationPitch};
        }else{
            return new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.prevRotationYaw, mc.thePlayer.prevRotationPitch};
        }
    }


    @SubscribeEvent
    private void onTick(TickEvent event) {
        if(canUpdateRotations()) {
            updateRotations();
        }

        if(Math.abs(activeYaw + 720 - rotationYaw + 720) > 45 && active && moveCorrect == MoveCorrect.SPRINT){
            mc.thePlayer.setSprinting(false);
        }
        if(isSmoothResetting() && !active){
            float differenceY = MathHelper.wrapAngleTo180_float(rotationYaw - activeYaw);
            float differenceP = rotationPitch - activePitch;

            activeYaw += (float) (differenceY / (1.5 + Math.random()));
            activePitch += (float) (differenceP / (1.5 + Math.random()));

            activePitch = MathHelper.clamp_float(activePitch, -90, 90);
            mc.thePlayer.rotationYaw = activeYaw;
            mc.thePlayer.rotationPitch = activePitch;
        }

        prevRotationYaw = rotationYaw;
        prevRotationPitch = rotationPitch;

        ticksPassed--;
    }

    public void updateRotations(){
        if(!isSmoothResetting()){
            mc.thePlayer.rotationYaw = rotationYaw;
            mc.thePlayer.rotationPitch = rotationPitch;
            mc.thePlayer.prevRotationYaw = prevRotationYaw;
            mc.thePlayer.prevRotationPitch = prevRotationPitch;
        }
    }

    public void fullyDisable(){
        updateRotations();
    }

    private int rotationCounter = 0;


    @SubscribeEvent(EventPriority.LOW)
    private void onRotate(PreMotionEvent event){
        if((active || isSmoothResetting()) && (moveCorrect == MoveCorrect.OFF || moveCorrect == MoveCorrect.SPRINT)){
            event.setYaw(activeYaw);
            event.setPitch(activePitch);
            mc.thePlayer.renderPitchHead = activePitch;
        }



        if(active && (moveCorrect == MoveCorrect.SPRINT)){
            float lookAt = getRotation()[0];
            float real = activeYaw;

            if(MathHelper.wrapAngleTo180_float(Math.abs(real) + 360) - MathHelper.wrapAngleTo180_float(Math.abs(lookAt)) > 45){
                mc.gameSettings.keyBindSprint.pressed = false;
                mc.gameSettings.keyBindSprint.unpressKey();
            }else {
                if(Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode())){
                    mc.gameSettings.keyBindSprint.pressed = true;
                }
            }

        }

        prevRenderedYaw = renderedYaw;
    }

    @SubscribeEvent
    private void onPlayerUpdateAction(PlayerUpdateActionStateEvent event){
        if(active || isSmoothResetting()){
            mc.thePlayer.rotationYawHead = activeYaw;
            event.setCancelled(true);
        }
    }


    @SubscribeEvent(EventPriority.LOW)
    private void onMoveFix(MoveInputEvent event){
        if((active ||isSmoothResetting()) && moveCorrect == MoveCorrect.SILENT){
            Freelook freelook = Ambient.getInstance().getModuleManager().getModule(Freelook.class);
            if(Arrays.equals(toFix, new Float[]{0f, 0f})){
                MoveUtil.fixMovement(event, mc.thePlayer.rotationYaw);
            }else if(event.getForward() != 0 || event.getStrafe() != 0){
                event.setForward(toFix[0]);
                event.setStrafe(toFix[1]);
            }
        }
    }
}
