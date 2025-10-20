package fr.ambient.anticheat;

import fr.ambient.Ambient;
import fr.ambient.component.impl.player.HypixelComponent;
import fr.ambient.module.impl.misc.Anticheat;
import fr.ambient.module.impl.render.hud.Overlay;
import fr.ambient.notification.NotificationManager;
import fr.ambient.util.packet.PacketUtil;
import fr.ambient.util.packet.RequestUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.player.PlayerUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.BlockPos;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Check {
    protected Anticheat anticheat;
    public String checkName;
    public HashMap<UUID, Integer> flags = new HashMap<>();
    public Minecraft mc = Minecraft.getMinecraft();

    public Check(String checkName, Anticheat anticheat) {
        this.checkName = checkName;
        this.anticheat = anticheat;
    }

    public void flagPlayer(EntityPlayer player, int vl) {
        if (!anticheat.isEnabled()) {
            return;
        }

        boolean whitelistTeam = anticheat.team.getValue();
        if (whitelistTeam && PlayerUtil.isEntityTeamSameAsPlayer(player)) return;
        if (player == mc.thePlayer) return;


        UUID playerId = player.getGameProfile().getId();
        int newFlags = flags.getOrDefault(playerId, 0) + vl;
        flags.put(playerId, newFlags);

        if (anticheat.notif.isSelected("Chat Message")) {
            ChatUtil.display(player.getDisplayName().getFormattedText() + " §7flagged §7for§c " + checkName);
        }

        if (anticheat.autowdr.getValue()) {
            PacketUtil.sendPacketNoEvent(new C01PacketChatMessage("/wdr " + player.getGameProfile().getName() + " cheating"));
        }

        handleFlaggedPlayer(player, newFlags);
    }

    private void handleFlaggedPlayer(EntityPlayer player, int newFlags) {
        if (checkName.equals("NoSlowB") && newFlags == 15) {
            addPlayerToCheaterList(player, "NoSlowB");
        }

        if (checkName.equals("Autoblock") && (newFlags == 9 || newFlags == 15)) {
            String reason = newFlags == 9 ? "AutoBlock" : "Scaffold";
            addPlayerToCheaterList(player, reason);
        }
    }

    private void addPlayerToCheaterList(EntityPlayer player, String reason) {
        Runnable task = () -> {
            if (!HypixelComponent.known_cheaters_or_alts.contains(player.getGameProfile().getId())) {
                ChatUtil.display("Adding " + player.getName() + " into cheater list due to anticheat flags. (" + reason + ")");
//                RequestUtil.requestResult.apply("/addCheater?user=" + player.getGameProfile().getId());
                Overlay overlay = Ambient.getInstance().getModuleManager().getModule(Overlay.class);
                overlay.playerData.remove(player.getGameProfile().getId());
            }
        };

        CompletableFuture.runAsync(task)
                .exceptionally(ex -> {
                    System.out.println("Task failed: " + ex.getMessage());
                    return null;
                });
    }

    public void onUpdate() {
    }

    public void onPacket(Packet packet) {
    }

    public void onBlockMod(BlockPos pos, IBlockState state) {

    }
}
