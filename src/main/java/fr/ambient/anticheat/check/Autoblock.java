package fr.ambient.anticheat.check;

import fr.ambient.anticheat.Check;
import fr.ambient.module.impl.misc.Anticheat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0BPacketAnimation;


public class Autoblock extends Check {
    public Autoblock(Anticheat anticheat) {
        super("Autoblock", anticheat);
    }

    @Override
    public void onPacket(Packet packet) {
        if (!anticheat.isEnabled() || !anticheat.checks.isSelected("Autoblock")) {
            return;
        }

        if (packet instanceof S0BPacketAnimation animation) {

            Entity e = Minecraft.getMinecraft().theWorld.getEntityByID(animation.getEntityID());

            if (e instanceof EntityPlayer player) {
                if (player.blockTicks > 5) {
                    flagPlayer(player, 1);
                }
            }
        }
    }

}
