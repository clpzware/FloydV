package femcum.modernfloyd.clients.command.impl;

import femcum.modernfloyd.clients.command.Command;
import femcum.modernfloyd.clients.util.chat.ChatUtil;
import femcum.modernfloyd.clients.util.packet.PacketUtil;
import net.minecraft.network.play.client.C03PacketPlayer;

public final class Stuck extends Command {

    public Stuck() {
        super("command.stuck.description", "stuck");
    }

    @Override
    public void execute(final String[] args) {
        PacketUtil.sendNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, -1, mc.thePlayer.posZ, false));
        ChatUtil.display("command.stuck.sent");
    }
}