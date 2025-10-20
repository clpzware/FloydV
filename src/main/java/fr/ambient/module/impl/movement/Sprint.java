package fr.ambient.module.impl.movement;

import fr.ambient.Ambient;
import fr.ambient.event.EventPriority;
import fr.ambient.event.annotations.SubscribeEvent;
import fr.ambient.event.impl.player.PreMotionEvent;
import fr.ambient.event.impl.player.UpdateEvent;
import fr.ambient.event.impl.player.move.JumpEvent;
import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.module.impl.combat.WTap;
import fr.ambient.module.impl.player.Scaffold;
import fr.ambient.property.impl.BooleanProperty;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.player.MoveUtil;
import net.minecraft.client.settings.KeyBinding;


public class Sprint extends Module {


    public final ModeProperty sprintMode = ModeProperty.newInstance("Mode", new String[]{"Vanilla", "Legit", "Omni"}, "Legit");
    public BooleanProperty hypixel = BooleanProperty.newInstance("Hypixel", true, () -> sprintMode.is("Omni"));

    public Sprint() {
        super(18,"Automatically makes you sprint without holding the key.", ModuleCategory.MOVEMENT);

        this.registerProperties(sprintMode,hypixel);
        this.setSuffix(sprintMode::getValue);
    }


    @SubscribeEvent
    private void onPlayerTick(UpdateEvent event) {
        switch (sprintMode.getValue().toLowerCase()) {
            case "legit" -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), mc.thePlayer.ticksExisted - mc.thePlayer.getLastAttackerTime() != 1 || !Ambient.getInstance().getModuleManager().getModule(WTap.class).isEnabled());
            case "vanilla" -> mc.thePlayer.setSprinting(true);
        }
    }

    @SubscribeEvent(EventPriority.VERY_HIGH)
    private void onPlayerNetwork(PreMotionEvent event){
        if(sprintMode.is("Omni")){
            if (hypixel.getValue()) {
                if (!Ambient.getInstance().getModuleManager().getModule(Scaffold.class).isEnabled()) {
                    event.setYaw(((float) Math.toDegrees(MoveUtil.getDirection())));
                    //mc.thePlayer.rotationYawHead = event.getYaw();
                }
            }
            mc.gameSettings.keyBindSprint.pressed = true;
        }
    }

    @SubscribeEvent
    private void onJumpEvent(JumpEvent event){
        if(sprintMode.is("Omni")){
            event.setYaw((float) (Math.toDegrees(MoveUtil.getDirection())));
        }
    }
}