package femcum.modernfloyd.clients.module.impl.other;

import femcum.modernfloyd.clients.component.impl.player.TargetComponent;
import femcum.modernfloyd.clients.event.Listener;
import femcum.modernfloyd.clients.event.annotations.EventLink;
import femcum.modernfloyd.clients.event.impl.motion.PreMotionEvent;
import femcum.modernfloyd.clients.module.Module;
import femcum.modernfloyd.clients.module.api.Category;
import femcum.modernfloyd.clients.module.api.ModuleInfo;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.vector.Vector2f;
import femcum.modernfloyd.clients.util.vector.Vector2i;
import femcum.modernfloyd.clients.util.vector.Vector3d;
import femcum.modernfloyd.clients.util.vector.Vector3i;
import net.minecraft.entity.Entity;

import java.util.HashMap;

@ModuleInfo(aliases = {"Sampler (Dev)"}, description = "module.other.antiafk.description", category = Category.PLAYER)
public final class Sampler extends Module {
    private final HashMap<String, Vector2f> samples = new HashMap<>();
    private Vector2f previousRotations;

    @Override
    public void onEnable() {
        if (mc.gameSettings.keyBindSneak.isKeyDown()) samples.clear();
    }

    @EventLink
    public final Listener<PreMotionEvent> onPreMotion = event -> {
        Entity nearest = TargetComponent.getTarget(6);
        Entity player = mc.thePlayer;
        Vector2f rotations = new Vector2f(player.rotationYaw % 360, player.rotationPitch);

        Main:
        {
            if (nearest == null || previousRotations == null) break Main;

            Sample sample = new Sample(
                    new Vector3d(nearest.posX - player.posX, nearest.posY - player.posY,
                            nearest.posZ - player.posZ),
                    new Vector2f(rotations.getX(), rotations.getY())
            );

            String id = sample.getID();

            if (samples.containsKey(id)) {
                Vector2f saved = samples.get(sample.getID());
//
                mc.thePlayer.rotationYaw += saved.getX();
                mc.thePlayer.rotationPitch += saved.getY();
//
                ChatUtil.display("Contained: " + id);
                ChatUtil.display("Yaw: " + saved.getX() + " Pitch: " + saved.getY());
            } else {
                ChatUtil.display("None " + id);
//                Vector2f delta = new Vector2f(rotations.getX() - previousRotations.getX(), rotations.getY() - previousRotations.getY());
//                samples.put(id, delta);
//
//                ChatUtil.display(delta.getX() + " " + delta.getY());
//                ChatUtil.display("Put: " + id);
            }
        }

        previousRotations = new Vector2f(rotations.getX(), rotations.getY());
    };

    private static final class Sample {
        Vector3i offset;
        Vector2i rotations;

        public Sample(Vector3d offset, Vector2f rotations) {
            this.offset = new Vector3i((int) (offset.x), (int) (offset.y), (int) (offset.z));
            this.rotations = new Vector2i((int) (rotations.getX() / 15), (int) (rotations.getY() / 20));
        }

        private String getID() {
            return offset.x + " " + offset.y + " " + offset.z + " " + rotations.x + " " + rotations.y;
        }
    }
}