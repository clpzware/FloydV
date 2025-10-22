package femcum.modernfloyd.clients.module.impl.movement.jesus;

import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.module.impl.movement.Jesus;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.value.Mode;
import net.minecraft.potion.Potion;

public class VulcanJesus extends Mode<Jesus> {
    private int waterTicks = 0;

    public VulcanJesus(String name, Jesus parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        if (mc.thePlayer.isInWater()) {

                waterTicks = 0;
                MoveUtil.strafe(.3 - Math.random() / 1000);

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe((.03 * (1 + (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier())) + .34 - Math.random() / 1000));
                }
                waterTicks++;
            }

        waterTicks++;
    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        if (mc.thePlayer.isInWater() && waterTicks < 10) {

            mc.thePlayer.motionY = .99 - Math.random() / 1000;
            waterTicks++;
        }
    };
}
