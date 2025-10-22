package femcum.modernfloyd.clients.module.impl.movement.flight;

import femcum.modernfloyd.clients.component.impl.player.RotationComponent;
import femcum.modernfloyd.clients.component.impl.player.rotationcomponent.MovementFix;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PostMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.event.impl.motion.PreUpdateEvent;
import femcum.modernfloyd.clients.event.impl.motion.StrafeEvent;
import femcum.modernfloyd.clients.event.impl.other.TeleportEvent;
import femcum.modernfloyd.clients.event.impl.packet.PacketSendEvent;
import femcum.modernfloyd.clients.module.impl.movement.Flight;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import femcum.modernfloyd.clients.util.player.MoveUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.value.Mode;
import femcum.modernfloyd.clients.value.impl.NumberValue;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class BlockDropFlight extends Mode<Flight> {

    private final NumberValue speed = new NumberValue("Speed", this, 1, 0.1, 9.5, 0.1);
    private Vector3d position;
    private Vector2f rotation;

    public BlockDropFlight(String name, Flight parent) {
        super(name, parent);
    }

    @Override
    public void onDisable() {
        MoveUtil.stop();

        PacketUtil.sendNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(position.getX(), position.getY(), position.getZ(), rotation.getX(), rotation.getY(), false));
    }

    @Override
    public void onEnable() {
        if (mc == null || mc.thePlayer == null) return;
        this.position = new Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        this.rotation = new Vector2f(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotionEvent = event -> {
        mc.thePlayer.motionY = mc.gameSettings.keyBindJump.isKeyDown() ? speed.getValue().floatValue() : mc.gameSettings.keyBindSneak.isKeyDown() ? -speed.getValue().floatValue() : 0;
    };

    @EventLink
    public final Listener<PostMotionEvent> onPostMotion = event -> {
        PacketUtil.sendNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(position.getX(), position.getY(), position.getZ(), rotation.getX(), rotation.getY(), false));
        PacketUtil.sendNoEvent(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, rotation.getX(), rotation.getY(), false));
    };

    @EventLink
    public final Listener<StrafeEvent> onStrafe = event -> {
        final float speed = this.speed.getValue().floatValue();

        event.setSpeed(speed);
    };

    @EventLink
    public final Listener<TeleportEvent> onTeleport = event -> {
        if (!mc.getNetHandler().doneLoadingTerrain) return;

        event.setCancelled();
        this.position = new Vector3d(event.getPosX(), event.getPosY(), event.getPosZ());
        this.rotation = new Vector2f(event.getYaw(), event.getPitch());
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (!mc.getNetHandler().doneLoadingTerrain) return;

        Packet packet = event.getPacket();

        if (packet instanceof C03PacketPlayer) {
            event.setCancelled();
        }
    };

    @EventLink
    public final Listener<PreUpdateEvent> onPreUpdate = event -> {
        RotationComponent.setRotations(rotation, 10, MovementFix.OFF);
    };
}