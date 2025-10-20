package fr.ambient.module.impl.combat;


import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.JumpEvent;
import fr.ambient.event.impl.player.move.StrafeEvent;
import fr.ambient.event.impl.render.Render3DEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.movement.Flight;
import fr.ambient.module.impl.movement.Speed;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.NumberProperty;
import fr.ambient.util.player.RotationUtil;
import fr.ambient.util.render.RenderUtil;
import fr.ambient.util.render.model.ESPUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;


public class TargetStrafe extends Module {

    private float yaw;
    private EntityLivingBase target;
    private int direction = 1;
    private boolean colliding;

    private final BooleanProperty onlyOnSpace = BooleanProperty.newInstance("Only on jump key press", false);
    private final BooleanProperty directionalInput = BooleanProperty.newInstance("Directional Input", true);
    private final BooleanProperty f5 = BooleanProperty.newInstance("Auto Third Person", false);
    private final BooleanProperty speed = BooleanProperty.newInstance("Only With Speed", false);
    public final NumberProperty radius = NumberProperty.newInstance("Radius", 0f, 2.8f, 6f, 0.1f);
    private final BooleanProperty render = BooleanProperty.newInstance("Draw Radius", true);

    public TargetStrafe() {
        super(63, "Strafes in a circle around the enemy at a desired radius",ModuleCategory.COMBAT);
        this.registerProperties(onlyOnSpace, directionalInput,render, f5,speed, radius);
    }

    @SubscribeEvent(value = EventPriority.VERY_HIGH)
    public void onStrafe(StrafeEvent e) {
        if (target != null) e.setYaw(yaw);
    }

    @SubscribeEvent(value = EventPriority.VERY_HIGH)
    public void onJump(JumpEvent e) {
        if (target != null) e.setYaw(yaw);
    }

    @SubscribeEvent(value = EventPriority.VERY_HIGH)
    public void onUpdate(UpdateEvent event) {
        target = KillAura.target;
        if (f5.getValue()) {
            if (canTS() && target != null) {
                mc.gameSettings.thirdPersonView = 1;
            }
        }
        if (f5.getValue()) {
            if (!canTS() && target == null) {
                mc.gameSettings.thirdPersonView = 0;
            }
        }

        if (speed.getValue() && !Ambient.getInstance().getModuleManager().getModule(Speed.class).isEnabled()) {
            target = null;
            mc.thePlayer.movementYaw = mc.thePlayer.rotationYaw;
            return;
        }

        Module scaffold = Ambient.getInstance().getModuleManager().getModule(Scaffold.class);
        if (scaffold == null || scaffold.isEnabled()){
            target = null;
            mc.thePlayer.movementYaw = mc.thePlayer.rotationYaw;
            return;
        }


        if (!canTS()) {
            target = null;
            mc.thePlayer.movementYaw = mc.thePlayer.rotationYaw;
            return;
        }

        if (directionalInput.getValue()) {
            if (mc.gameSettings.keyBindLeft.isPressed())
                this.direction = -1;
            else if (mc.gameSettings.keyBindRight.isPressed())
                this.direction = 1;
        }

        handleCollision();


        if (target == null){
            mc.thePlayer.movementYaw = mc.thePlayer.rotationYaw;
            return;
        }

        updatePlayerMovement();
    }

    private boolean canTS() {
        Module speed = Ambient.getInstance().getModuleManager().getModule(Speed.class);
        Module flight = Ambient.getInstance().getModuleManager().getModule(Flight.class);

        boolean isMovingForward = mc.gameSettings.keyBindForward.isKeyDown();
        boolean isFlightOrSpeedEnabled = (flight != null && flight.isEnabled()) || (speed != null && speed.isEnabled());

        return (!onlyOnSpace.getValue() || mc.gameSettings.keyBindJump.isKeyDown() && isMovingForward) && isFlightOrSpeedEnabled;
    }

    private void handleCollision() {
        if (mc.thePlayer.isCollidedHorizontally) {
            if (!colliding) {
                direction = -direction;
            }
            colliding = true;
        } else {
            colliding = false;
        }
    }

    private void updatePlayerMovement() {
        float[] rotations = RotationUtil.getRotation(target);
        float yaw = rotations[0] + 135 * direction;

        double range = radius.getValue();
        double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
        double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

        yaw = RotationUtil.getRotationsVector(mc.thePlayer.getPositionVector().addVector(0, mc.thePlayer.getEyeHeight(), 0), new Vec3(posX, target.posY,posZ))[0];

        this.yaw = yaw;
        mc.thePlayer.movementYaw = yaw;
    }

    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if(render.getValue() && KillAura.target != null){
            ESPUtil.drawCircle(KillAura.target, radius.getValue(), 80, 2,canTS() ? Ambient.getInstance().getHud().getCurrentTheme().getColor2().darker() : Ambient.getInstance().getHud().getCurrentTheme().getColor2());
        }
    }
}
