package fr.ambient.module.impl.render.misc;

import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.camera.CameraHurtEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.BooleanProperty;
import net.minecraft.potion.Potion;

public class Camera extends Module {

    public Camera() {
        super(43, "Adjusts the camera, like preventing shake when taking damage.", ModuleCategory.RENDER);
        this.registerProperties(noblindness,noShake, trw, nofire, fullbright);
    }

    private final BooleanProperty noShake = BooleanProperty.newInstance("No Shake", true);
    public final BooleanProperty trw = BooleanProperty.newInstance("Through Wall", false);
    public final BooleanProperty nofire = BooleanProperty.newInstance("No Fire", true);
    public final BooleanProperty fullbright = BooleanProperty.newInstance("FullBright", false);
    public final BooleanProperty noblindness = BooleanProperty.newInstance("Remove Blindness", false);

    @SubscribeEvent
    private void onHurt(CameraHurtEvent event) {
        if (noShake.getValue()) {
            event.setCancelled(true);
        }
    }

    @SubscribeEvent
    private void event(UpdateEvent event) {
        if (noblindness.getValue()) {
            if (mc.thePlayer != null && mc.thePlayer.isPotionActive(Potion.blindness)) {
                mc.thePlayer.removePotionEffect(Potion.blindness.getId());
            }
        }
    }

}
