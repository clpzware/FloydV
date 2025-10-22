package femcum.modernfloyd.clients.module.impl.movement.longjump;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.Slot;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.module.impl.movement.LongJump;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.player.SlotUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.ModeValue;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import femcum.modernfloyd.clients.value.impl.SubMode;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class FireBallLongJump extends Mode<LongJump> {

    public ModeValue mode = new ModeValue("Mode", this)
            .add(new SubMode("Custom"))
            .add(new SubMode("Hypixel"))
            .setDefault("Hypixel");

    private final NumberValue height = new NumberValue("Height", this, 1, 0.42, 9, 0.1, () -> !mode.getValue().getName().equals("Custom"));
    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 3, 0.1, () -> !mode.getValue().getName().equals("Custom"));
    private int tick;
    private double moveSpeed = 0;
    private float yawAtDamage;

    public FireBallLongJump(String name, LongJump parent) {
        super(name, parent);
    }

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        switch (mode.getValue().getName()) {
            case "Hypixel":
                if (mc.thePlayer.hurtTime == 10) {
                    yawAtDamage = mc.thePlayer.rotationYaw;
                    mc.thePlayer.motionY = 1.5;
                    moveSpeed = 1.4;
                }

                if (mc.thePlayer.hurtTime == 9) {
                    moveSpeed = 2 - Math.random() / 100f;
                }

                if (mc.thePlayer.ticksSinceVelocity <= 11) {
                    MoveUtil.strafe(moveSpeed, yawAtDamage);
                    moveSpeed -= (moveSpeed / 249.9) + Math.random() / 100f;
                }
                break;

            case "Custom":
                if (mc.thePlayer.ticksSinceVelocity <= 1) {
                    mc.thePlayer.motionY = height.getValue().doubleValue();
                    MoveUtil.strafe(speed.getValue().doubleValue());
                }
                break;
        }

        int item = SlotUtil.findItem(Items.fire_charge);

        if (mc.thePlayer.onGroundTicks == 1) {
            MoveUtil.stop();
        }

        if (item == -1) return;

        tick++;

        getComponent(Slot.class).setSlot(item);

        if (tick == 2) {
            PacketUtil.send(new C08PacketPlayerBlockPlacement(getComponent(Slot.class).getItemStack()));
        }

        RotationComponent.setRotations(new Vector2f(mc.thePlayer.rotationYaw, 90), 10, MovementFix.OFF);
    };

    @Override
    public void onEnable() {
        tick = 0;

        if (mode.getValue().getName().equals("Hypixel")) {
            ChatUtil.display("Don't enable on bridges, the ac falses, also make sure you take the velocity when on the ground.");
        }
    }
}