package femcum.modernfloyd.clients.module.impl.movement;

import femcum.modernfloyd.clients.component.impl.player.TargetComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.Priorities;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.JumpEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.module.impl.combat.KillAura;
import femcum.modernfloyd.clients.module.impl.player.Scaffold;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.PlayerUtil;
import femcum.modernfloyd.clients.util.rotation.RotationUtil;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.value.impl.BooleanValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

import java.util.List;

@ModuleInfo(aliases = {"module.movement.targetstrafe.name"}, description = "module.movement.targetstrafe.description", category = Category.MOVEMENT)
public class TargetStrafe extends Module {

    private final NumberValue range = new NumberValue("Range", this, 1, 0.2, 6, 0.1);

    public final BooleanValue holdJump = new BooleanValue("Hold Jump", this, true);
    private float yaw;
    private EntityLivingBase target;
    private boolean left, colliding;
    private boolean active;

    @EventLink(value = Priorities.HIGH)
    public final Listener<JumpEvent> onJump = event -> {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    };

    @EventLink(value = Priorities.HIGH)
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    };

    @EventLink(value = Priorities.HIGH)
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        // Disable if scaffold is enabled
        Module scaffold = getModule(Scaffold.class);
        KillAura killaura = getModule(KillAura.class);

        if (scaffold == null || scaffold.isEnabled() || killaura == null || !killaura.isEnabled()) {
            active = false;
            return;
        }

        active = true;

        /*
         * Getting targets and selecting the nearest one
         */
        Module speed = getModule(Speed.class);
        Module test = null;
        Module flight = getModule(Flight.class);

        if (holdJump.getValue() && !mc.gameSettings.keyBindJump.isKeyDown() || !(mc.gameSettings.keyBindForward.isKeyDown() &&
                ((flight != null && flight.isEnabled()) || ((speed != null && speed.isEnabled()) || (test != null && test.isEnabled()))))) {
            target = null;
            return;
        }

        final List<EntityLivingBase> targets = TargetComponent.getTargets(this.range.getValue().doubleValue() + 6);

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (mc.thePlayer.isCollidedHorizontally || !PlayerUtil.isBlockUnder(5, false)) {
            if (!colliding) {
                MoveUtil.strafe();
                left = !left;
            }
            colliding = true;
        } else {
            colliding = false;
        }

        target = targets.get(0);

        if (target == null) {
            return;
        }

        float yaw = RotationUtil.calculate(target).getX() + (90 + 45) * (left ? -1 : 1);

        final double range = this.range.getValue().doubleValue() + Math.random() / 100f;
        final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
        final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

        yaw = RotationUtil.calculate(new Vector3d(posX, target.posY, posZ)).getX();

        this.yaw = yaw;
        mc.thePlayer.movementYaw = this.yaw;
    };
}